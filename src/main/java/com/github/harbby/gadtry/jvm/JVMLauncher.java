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
import com.github.harbby.gadtry.base.Throwables;

import java.io.FilterOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public interface JVMLauncher<R>
{
    public R startAndGet()
            throws JVMException;

    public R startAndGet(VmCallable<R> task)
            throws JVMException;

    public VmPromise<R> start(VmCallable<R> task)
            throws JVMException;

    public VmPromise<R> start()
            throws JVMException;

    public static ChildVMSystemOutputStream initSystemOutErrChannel()
            throws NoSuchFieldException
    {
        /*
         * plan1:
         * System.err.close();
         * System.setErr(System.out);
         *
         * plan2:
         * if (Platform.getClassVersion() > 52) {
         *      ops.add("--add-opens=java.base/java.io=ALL-UNNAMED");
         * }
         */
        ChildVMSystemOutputStream mock = new ChildVMSystemOutputStream(System.out);
        System.setOut(mock);
        if (Platform.getJavaVersion() > 8) {
            Field field = FilterOutputStream.class.getDeclaredField("out");
            Platform.getUnsafe().putObject(System.err, Platform.getUnsafe().objectFieldOffset(field), mock);  //equals to: field.set(System.err, mock);
        }
        else {
            System.setErr(mock);
        }
        return mock;
    }

    public static void main(String[] args)
            throws Exception
    {
        ChildVMSystemOutputStream outputStream = JVMLauncher.initSystemOutErrChannel();
        //first write header
        outputStream.writeVmHeader();

        try (ObjectInputStream ois = new ObjectInputStream(System.in)) {
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
        finally {
            System.exit(0);
        }
    }
}
