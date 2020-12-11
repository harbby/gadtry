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
import com.github.harbby.gadtry.memory.Platform;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JvmAgent
{
    private JvmAgent() {}

    private static final Supplier<DataOutputStream> systemOutGetOrInit = Lazys.goLazy(() -> {
        System.err.close();

        try {
            Field field = FilterOutputStream.class.getDeclaredField("out");
            field.setAccessible(true);
            OutputStream out = (OutputStream) field.get(System.out);
            DataOutputStream outputStream = new DataOutputStream(out);
            OutputStream mock = new OutputStream()
            {
                @Override
                public void write(int b)
                        throws IOException
                {
                    this.write(String.valueOf(b).getBytes(UTF_8));
                }

                @Override
                public void write(byte[] b)
                        throws IOException
                {
                    this.write(b, 0, b.length);
                }

                @Override
                public void write(byte[] buf, int off, int len)
                        throws IOException
                {
//                if ((len - off) == 1 && buf[0] == 10) { //filter '\n'
//                    return;
//                }

                    int length = len;
//                if (buf[buf.length - 1] == 10) {  //use trim()
//                    length = len - 1;
//                }

                    try {
                        outputStream.writeByte(1);
                        outputStream.writeInt(length - off);
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    outputStream.write(buf, off, length);
                }
            };

            field.set(System.out, mock);
            field.set(System.err, mock);

//            PrintStream outStream = new PrintStream(mock);
//            System.setOut(outStream);
//            System.setErr(outStream);
            return outputStream;
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    });

    public static DataOutputStream systemOutGetOrInit()
    {
        return systemOutGetOrInit.get();
    }

    public static void premain(String agentArgs, Instrumentation inst)
            throws Exception
    {
        JvmAgent.systemOutGetOrInit();
        System.err.write(123);

        ClassPool cp = ClassPool.getDefault();
        String[] split = agentArgs.split(":");
        checkState(split.length == 2, "-javaagent:agent.jar=oldClass:newClass");
        CtClass cc = cp.get(split[0]);
        cc.setName(split[1]);
        Platform.defineClass(cc.toBytecode(), ClassLoader.getSystemClassLoader());
    }
}
