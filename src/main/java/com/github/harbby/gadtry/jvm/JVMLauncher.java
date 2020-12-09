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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UncheckedIOException;

public interface JVMLauncher<R extends Serializable>
{
    public R startAndGet()
            throws JVMException;

    public R startAndGet(VmCallable<R> task)
            throws JVMException;

    /**
     * @since 1.4
     */
    public VmFuture<R> startAsync()
            throws JVMException;

    /**
     * @since 1.4
     */
    public VmFuture<R> startAsync(VmCallable<R> task)
            throws JVMException;

    public static DataOutputStream mockSystemOutErr()
    {
        DataOutputStream outputStream = new DataOutputStream(System.out);
        PrintStream outStream = new PrintStream(outputStream)
        {
            @Override
            public void write(byte[] buf, int off, int len)
            {
                if ((len - off) == 1 && buf[0] == 10) { //filter '\n'
                    return;
                }

                int length = len;
                if (buf[buf.length - 1] == 10) {  //use trim()
                    length = len - 1;
                }

                try {
                    outputStream.writeByte(1);
                    outputStream.writeInt(length - off);
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                super.write(buf, off, length);
            }
        };

        System.setOut(outStream);
        System.setErr(outStream);
        return outputStream;
    }

    public static void main(String[] args)
            throws Exception
    {
        boolean debug = Boolean.parseBoolean(args[0]);
        DataOutputStream outputStream = mockSystemOutErr();
        if (debug) {
            System.out.println("vm starting ...");
        }
        VmResult<? extends Serializable> future;

        try (ObjectInputStreamProxy ois = new ObjectInputStreamProxy(System.in)) {
            if (debug) {
                System.out.println("vm start init ...");
            }
            VmCallable<? extends Serializable> task = (VmCallable<? extends Serializable>) ois.readObject();
            future = new VmResult<>(task.call());
        }
        catch (Throwable e) {
            future = new VmResult<>(Throwables.getStackTraceAsString(e));
            if (debug) {
                System.out.println("vm task run error");
            }
        }

        byte[] result = Serializables.serialize(future);
        if (debug) {
            System.out.println("vm exiting ...");
        }

        outputStream.writeByte(2);
        outputStream.writeInt(result.length);
        outputStream.write(result);
        outputStream.flush();
    }
}
