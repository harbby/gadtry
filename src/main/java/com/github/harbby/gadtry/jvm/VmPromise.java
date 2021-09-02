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
import com.github.harbby.gadtry.function.Promise;
import com.github.harbby.gadtry.function.exception.Function;

import java.io.EOFException;
import java.io.IOException;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public interface VmPromise<R>
        extends Promise<R>
{
    R call()
            throws InterruptedException, JVMException;

    long pid();

    public boolean isDone();

    void cancel();

    default <E> VmPromise<E> map(Function<R, E, InterruptedException> map)
    {
        requireNonNull(map, "func is null");
        return new VmPromise<E>()
        {
            @Override
            public long pid()
            {
                return VmPromise.this.pid();
            }

            @Override
            public boolean isDone()
            {
                return VmPromise.this.isDone();
            }

            @Override
            public E call()
                    throws InterruptedException
            {
                return map.apply(VmPromise.this.call());
            }

            @Override
            public void cancel()
            {
                VmPromise.this.cancel();
            }
        };
    }

    public static class VmPromiseImpl
            implements VmPromise<byte[]>
    {
        private final Process process;
        private final ChildVMChannelInputStream childVmReader;
        private final Consumer<String> consoleHandler;

        VmPromiseImpl(Process process, ChildVMChannelInputStream reader, Consumer<String> consoleHandler)
        {
            this.process = process;
            this.childVmReader = reader;
            this.consoleHandler = consoleHandler;
        }

        @Override
        public byte[] call()
                throws InterruptedException
        {
            try (ChildVMChannelInputStream reader = childVmReader) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consoleHandler.accept(line);
                }
                byte[] bytes = reader.readResult();
                if (reader.isSuccess()) {
                    process.destroy();
                    return bytes;
                }
                else {
                    throw new JVMException(new String(bytes, UTF_8));
                }
            }
            catch (EOFException e) {
                if (process.isAlive()) {
                    process.destroy();
                    process.waitFor();
                }
                throw new JVMException("Jvm child process abnormal exit, exit code " + process.exitValue());
            }
            catch (IOException e) {
                throw new JVMException("child jvm exec failed", e);
            }
        }

        @Override
        public long pid()
        {
            return Platform.getProcessPid(process);
        }

        public boolean isDone()
        {
            return !process.isAlive();
        }

        @Override
        public void cancel()
        {
            process.destroy();
        }
    }
}
