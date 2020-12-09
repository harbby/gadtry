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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Random;

public class JvmAgent
{
    private JvmAgent() {}

    private static final String CLASS_NAME = JVMLauncher.class.getName().replace(".", "/");

    public static void premain(String agentArgs, Instrumentation inst)
    {
        inst.addTransformer(new ClassFileTransformer()
        {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer)
                    throws IllegalClassFormatException
            {
                //System.out.println(className);
                System.exit(126);
                if (!CLASS_NAME.equals(className)) {
                    return null;
                }
                System.exit(126);

                //System.out.println("find main class " + className);
                ClassPool cp = ClassPool.getDefault();
                cp.appendClassPath(new LoaderClassPath(loader));

                try {
                    CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
                    cc.setName(CLASS_NAME + ".demo" + new Random().nextInt(10));
                    Arrays.stream(cc.getMethods()).forEach(m -> System.out.println(m.getName()));
                    return cc.toBytecode();
                }
                catch (IOException | CannotCompileException e) {
                    System.exit(126);
                    return null;
                }
            }
        });
    }
}
