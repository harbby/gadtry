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
import sun.misc.Unsafe;

import java.io.FilterOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public final class ForkVmProcess
{
    private ForkVmProcess() {}

    public static void main(String[] args)
            throws Exception
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
        ChildVMSystemOutputStream wrapperOutStream = new ChildVMSystemOutputStream(System.out);
        Unsafe unsafe = Platform.getUnsafe();
        System.setOut(wrapperOutStream);
        // plan3:
        Field field = FilterOutputStream.class.getDeclaredField("out");
        unsafe.putObject(System.err, unsafe.objectFieldOffset(field), wrapperOutStream);  //equals to: field.set(System.err, mock);

        //first write header
        wrapperOutStream.writeVmHeader();

        try (ObjectInputStream ois = new ObjectInputStream(System.in)) {
            VmCallable<?> task = (VmCallable<?>) ois.readObject();
            Object value = task.call();
            if (value != null && !(value instanceof Serializable)) {
                throw new NotSerializableException("not serialize result: " + value);
            }

            byte[] result = Serializables.serialize((Serializable) value);
            wrapperOutStream.release(false, result);
        }
        catch (Throwable e) {
            byte[] err = Throwables.getStackTraceAsString(e).getBytes(StandardCharsets.UTF_8);
            wrapperOutStream.release(true, err);
        }
        finally {
            System.exit(0);
        }
    }
}
