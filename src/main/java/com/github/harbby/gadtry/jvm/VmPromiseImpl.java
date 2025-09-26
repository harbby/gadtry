/*
 * Copyright (C) 2018 The GadTry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.harbby.gadtry.jvm;

import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VmPromiseImpl<T>
        implements VmPromise<T>
{
    private final Process process;
    private final Consumer<String> consoleHandler;
    private final ClassLoader classLoader;
    private final long timeoutNanos;

    VmPromiseImpl(Process process, Consumer<String> consoleHandler, ClassLoader classLoader, long timeoutNanos)
    {
        this.process = process;
        this.consoleHandler = consoleHandler;
        this.classLoader = classLoader;
        this.timeoutNanos = timeoutNanos;
    }

    @Override
    public T call(long timeout, TimeUnit timeUnit)
            throws JVMException, InterruptedException
    {
        return this.call0(timeUnit.toNanos(timeout));
    }

    private T call0(long nanos)
            throws JVMException, InterruptedException
    {
        long end = System.nanoTime() + nanos;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                InputStream callbackStream = process.getInputStream()) {
            T result = null;
            boolean isResult = false;
            while (true) {
                if (nanos > 0 && System.nanoTime() > end) {
                    throw new JVMTimeoutException("child vm timeout");
                }
                else if (reader.ready()) {
                    consoleHandler.accept(reader.readLine());
                }
                else if (!isResult && callbackStream.available() > 0) {
                    result = tryReadResult(callbackStream);
                    isResult = true;
                }
                else if (process.isAlive()) {
                    TimeUnit.MILLISECONDS.sleep(1);
                }
                else {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        consoleHandler.accept(line);
                    }
                    if (!isResult) {
                        result = tryReadResult(callbackStream);
                    }
                    return result;
                }
            }
        }
        catch (IOException e) {
            throw new JVMException("child jvm exec failed", e);
        }
        catch (ClassNotFoundException e) {
            throw new JVMException("child result decoder failed", e);
        }
        finally {
            process.destroy();
        }
    }

    @Override
    public T call()
            throws InterruptedException
    {
        return this.call0(timeoutNanos);
    }

    @Override
    public long pid()
    {
        return Platform.getProcessPid(process);
    }

    @Override
    public boolean isAlive()
    {
        return process.isAlive();
    }

    @Override
    public void cancel()
    {
        process.destroy();
    }

    private T tryReadResult(InputStream callbackStream)
            throws IOException, ClassNotFoundException, InterruptedException
    {
        int flag = callbackStream.read();
        switch (flag) {
            case -1:
                throw new JVMException("child process abnormal exit, exit code " + process.waitFor());
            case '0':
                return Serializables.byteToObject(callbackStream, classLoader);
            case '1':
                throw new JVMException(IOUtils.toString(callbackStream, UTF_8));
            default:
                throw new JVMException("child process unknown error");
        }
    }
}
