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

import static com.github.harbby.gadtry.base.Throwables.throwsException;
import static java.util.Objects.requireNonNull;

public class VmFuture<R extends Serializable>
{
    private final Process process;
    private final Future<VmResult<R>> future;
    private volatile JVMException error;

    public VmFuture(AtomicReference<Process> processAtomic, Callable<VmResult<R>> callable)
            throws JVMException, InterruptedException
    {
        requireNonNull(processAtomic, "process is null");
        ExecutorService service = Executors.newSingleThreadExecutor();
        this.future = service.submit(() -> {
            try {
                return callable.call();
            }
            catch (JVMException e) {
                this.error = e;
                throw error;
            }
            catch (Throwable e) {
                this.error = new JVMException(e);
                throw error;
            }
        });
        service.shutdown();

        while (processAtomic.get() == null) {
            if (future.isDone()) {
                throw error;   // this throws ExecutionException
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
        if ("java.lang.UNIXProcess".equals(system)) {
            try {
                Field field = process.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                int pid = (int) field.get(process);
                return pid;
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                throw throwsException(e);
            }
        }
        throw new UnsupportedOperationException("Only support for UNIX and Linux systems pid, Your " + system + " is Windows ?");
    }

    public R get()
            throws JVMException, InterruptedException
    {
        if (error != null) {
            throw error;
        }
        try {
            return future.get().get();
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof JVMException) {
                throw (JVMException) cause;
            }
            throw new JVMException(e);
        }
    }

    public R get(long timeout, TimeUnit unit)
            throws JVMException, InterruptedException, TimeoutException
    {
        if (error != null) {
            throw error;
        }
        try {
            return future.get(timeout, unit).get();
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof JVMException) {
                throw (JVMException) cause;
            }
            throw new JVMException(e);
        }
    }

    public boolean isRunning()
    {
        return !future.isDone() && getVmProcess().isAlive();
    }

    public void cancel()
    {
        getVmProcess().destroy();
    }
}
