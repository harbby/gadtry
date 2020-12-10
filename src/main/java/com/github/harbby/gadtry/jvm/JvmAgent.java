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
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.instrument.Instrumentation;
import java.util.function.Supplier;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class JvmAgent
{
    private JvmAgent() {}

    private static final Supplier<DataOutputStream> systemOutGetOrInit = Lazys.goLazy(() -> {
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
    });

    public static DataOutputStream systemOutGetOrInit()
    {
        return systemOutGetOrInit.get();
    }

    public static void premain(String agentArgs, Instrumentation inst)
            throws Exception
    {
        JvmAgent.systemOutGetOrInit();

        ClassPool cp = ClassPool.getDefault();
        String[] split = agentArgs.split(":");
        checkState(split.length == 2, "-javaagent:agent.jar=oldClass:newClass");
        CtClass cc = cp.get(split[0]);
        cc.setName(split[1]);
        Platform.defineClass(cc.toBytecode(), ClassLoader.getSystemClassLoader());
    }
}
