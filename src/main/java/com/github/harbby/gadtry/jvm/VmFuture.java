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

import com.github.harbby.gadtry.base.Throwables;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;
import static java.util.Objects.requireNonNull;

public class VmFuture<R extends Serializable>
{
    private final Process process;
    private final Future<VmResult<R>> future;

    public VmFuture(AtomicReference<Process> processAtomic, Callable<VmResult<R>> callable)
            throws JVMException, InterruptedException
    {
        requireNonNull(processAtomic, "process is null");
        ExecutorService service = Executors.newSingleThreadExecutor();
        this.future = service.submit(callable);
        service.shutdown();

        while (processAtomic.get() == null) {
            if (future.isDone()) {
                try {
                    R r = future.get().get();
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

    public int getPid()
    {
        Process process = getVmProcess();
        String system = process.getClass().getName();
        try {
            Field field = process.getClass().getDeclaredField("pid");
            field.setAccessible(true);
            int pid = (int) field.get(process);
            return pid;
        }
        catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Only support for UNIX and Linux systems pid, Your " + system + " is Windows ?");
        }
        catch (IllegalAccessException e) {
            throw throwsThrowable(e);
        }
    }

    public R get()
            throws JVMException, InterruptedException
    {
        try {
            return future.get().get();
        }
        catch (ExecutionException e) {
            throw Throwables.throwsThrowable(e.getCause());
        }
    }

    public R get(long timeout, TimeUnit unit)
            throws JVMException, InterruptedException, TimeoutException, ExecutionException
    {
        return future.get(timeout, unit).get();
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
