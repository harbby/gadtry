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

import com.github.harbby.gadtry.base.ObjectInputStreamProxy;
import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.base.Throwables;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

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

    public static SystemOutputStream getOrCreate()
    {
        SystemOutputStream mock = new SystemOutputStream(System.out);
        System.setOut(mock);
        System.setErr(mock);
        return mock;
    }

    public static void main(String[] args)
            throws Exception
    {
        SystemOutputStream outputStream = JVMLauncher.getOrCreate();
        try (ObjectInputStreamProxy ois = new ObjectInputStreamProxy(System.in)) {
            VmCallable<?> task = (VmCallable<?>) ois.readObject();
            Object value = task.call();
            if (value != null && !(value instanceof Serializable)) {
                throw new NotSerializableException("not serialize result: " + value);
            }

            byte[] result = Serializables.serialize((Serializable) value);
            outputStream.release(false, result);
        }
        catch (Throwable e) {
            byte[] err = Throwables.getStackTraceAsString(e).getBytes(StandardCharsets.UTF_8);
            outputStream.release(true, err);
        }
    }
}
