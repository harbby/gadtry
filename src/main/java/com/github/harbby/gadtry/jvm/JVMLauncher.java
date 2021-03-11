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

import com.github.harbby.gadtry.base.Lazys;
import com.github.harbby.gadtry.base.ObjectInputStreamProxy;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.base.Throwables;

import java.io.FilterOutputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface JVMLauncher<R>
{
    public R startAndGet()
            throws JVMException;

    public R startAndGet(VmCallable<R> task)
            throws JVMException;

    public VmFuture<R> startAsync(ExecutorService executor)
            throws JVMException;

    public VmFuture<R> startAsync(ExecutorService executor, VmCallable<R> task)
            throws JVMException;

    static final Supplier<SystemOutputStream> systemOutGetOrInit = Lazys.goLazy(() -> {
        if (Platform.getVmClassVersion() > 52) {
            Platform.addOpenJavaModules(FilterOutputStream.class, SystemOutputStream.class);
        }
        try {
            Field field = FilterOutputStream.class.getDeclaredField("out");
            field.setAccessible(true);
            SystemOutputStream mock = new SystemOutputStream((OutputStream) field.get(System.out));
            field.set(System.out, mock);
            field.set(System.err, mock);
            return mock;
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    });

    public static SystemOutputStream getOrCreate()
    {
        return systemOutGetOrInit.get();
    }

    public static void main(String[] args)
            throws Exception
    {
        SystemOutputStream outputStream = JVMLauncher.getOrCreate();
        VmResult<? extends Serializable> future;

        try (ObjectInputStreamProxy ois = new ObjectInputStreamProxy(System.in)) {
            VmCallable<?> task = (VmCallable<?>) ois.readObject();
            Object value = task.call();
            if (value != null && !(value instanceof Serializable)) {
                throw new NotSerializableException("not serialize result: " + value);
            }
            future = new VmResult<>((Serializable) value);
        }
        catch (Throwable e) {
            future = new VmResult<>(Throwables.getStackTraceAsString(e));
        }

        byte[] result = Serializables.serialize(future);

        outputStream.close();
        outputStream.release(result);
    }
}
