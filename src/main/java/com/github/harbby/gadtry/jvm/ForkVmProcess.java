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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static com.github.harbby.gadtry.jvm.JVMLauncherImpl.VM_HEADER;

public final class ForkVmProcess
{
    private ForkVmProcess() {}

    public static void runMain(String[] args)
            throws Exception
    {
        boolean autoExit = Boolean.parseBoolean(args[0]);
        OutputStream sysErr = initSysError();
        doMain(autoExit, sysErr);
    }

    private static void doMain(boolean autoExit, OutputStream sysErr)
    {
        try {
            // first read sys.in
            Callable<?> task = Serializables.byteToObject(System.in, ClassLoader.getSystemClassLoader());
            // check parent
            if (autoExit) {
                Thread teakThread = new StdinListenerThread();
                teakThread.setName("StdinListenerThread");
                teakThread.start();
            }

            //2. write header to sys.err
            sysErr.write(VM_HEADER, 0, VM_HEADER.length);
            sysErr.flush();
            //3. call task
            Object value = task.call();
            byte[] result = Serializables.serialize(value);
            saveTarget(sysErr, result, true);
            System.exit(0);
        }
        catch (Throwable e) {
            byte[] err = Throwables.getStackTraceAsString(e).getBytes(StandardCharsets.UTF_8);
            saveTarget(sysErr, err, false);
            System.exit(1);
        }
    }

    private static class StdinListenerThread
            extends Thread
    {
        private StdinListenerThread() {}

        @Override
        public void run()
        {
            try {
                while (System.in.read() != -1) {
                    // pass
                }
                // Parent exits.
                System.exit(2);
            }
            catch (IOException e) {
                // e.printStackTrace();
            }
        }
    }

    private static OutputStream initSysError()
            throws Exception
    {
        Unsafe unsafe = Platform.getUnsafe();
        Field field = FilterOutputStream.class.getDeclaredField("out");
        long offset = unsafe.objectFieldOffset(field);
        OutputStream sysOut = (OutputStream) unsafe.getObject(System.out, offset);
        OutputStream sysErr = (OutputStream) unsafe.getObject(System.err, offset);
        // redirect Error Stream
        unsafe.putObject(System.err, offset, sysOut);
        return sysErr;
    }

    private static void saveTarget(OutputStream outputStream, byte[] bytes, boolean success)
    {
        try {
            outputStream.write(success ? '0' : '1');
            outputStream.write(bytes);
            outputStream.flush();
        }
        catch (IOException e) {
            // sys err failed
            e.printStackTrace();
        }
    }
}
