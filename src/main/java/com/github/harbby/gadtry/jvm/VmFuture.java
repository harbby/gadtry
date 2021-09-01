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
import com.github.harbby.gadtry.base.Throwables;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class VmFuture<R>
{
    private final Process process;
    private final Future<R> future;

    public VmFuture(ExecutorService executor, AtomicReference<Process> processAtomic, Callable<R> callable)
            throws JVMException, InterruptedException
    {
        requireNonNull(processAtomic, "process is null");
        this.future = executor.submit(callable);

        while (processAtomic.get() == null) {
            if (future.isDone()) {
                try {
                    R r = future.get();
                    throw new JVMException("Async failed! future.isDone() result:" + r);
                }
                catch (ExecutionException e) {
                    // this throws ExecutionException
                    throw new JVMException(e.getCause());
                }
            }

            TimeUnit.MILLISECONDS.sleep(1);
        }
        this.process = processAtomic.get();
    }

    public Process getVmProcess()
    {
        return process;
    }

    public long getPid()
    {
        Process process = getVmProcess();
        return Platform.getProcessPid(process);
    }

    public R get()
            throws JVMException, InterruptedException
    {
        try {
            return future.get();
        }
        catch (ExecutionException e) {
            throw Throwables.throwsThrowable(e.getCause());
        }
    }

    public R get(long timeout, TimeUnit unit)
            throws JVMException, InterruptedException, TimeoutException, ExecutionException
    {
        return future.get(timeout, unit);
    }

    public boolean isRunning()
    {
        if (future.isDone()) {
            return false;
        }
        return getVmProcess().isAlive();
    }

    public void cancel()
    {
        getVmProcess().destroy();
    }
}
