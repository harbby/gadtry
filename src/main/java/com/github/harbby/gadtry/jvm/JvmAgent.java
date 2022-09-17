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
import com.github.harbby.gadtry.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.lang.instrument.Instrumentation;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class JvmAgent
{
    private JvmAgent() {}

    public static void premain(String agentArgs, Instrumentation inst)
            throws Exception
    {
        String[] split = agentArgs.split(":");
        checkState(split.length == 2, "-javaagent:agent.jar=oldClass:newClassName");
        Class<?> oldMainClass = Class.forName(split[0]);
        String mainClass = (oldMainClass.getPackage().getName() + "." + split[1]).replace(".", "/");
        ClassReader classReader = new ClassReader(oldMainClass.getName());
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        classReader.accept(new ClassModify(Opcodes.ASM9, classWriter, mainClass), ClassReader.SKIP_DEBUG);
        byte[] byteCode = classWriter.toByteArray();
        IOUtils.write(byteCode, new File("out/1.class"));
        if (Platform.getJavaVersion() > 8) {
            Platform.defineClass(oldMainClass, byteCode);
        }
        else {
            Platform.defineClass(byteCode, ClassLoader.getSystemClassLoader());
        }
    }

    private static class ClassModify
            extends ClassVisitor
    {
        private final String mainClass;

        protected ClassModify(int api, ClassVisitor classVisitor, String mainClass)
        {
            super(api, classVisitor);
            this.mainClass = mainClass;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            super.visit(version, access, mainClass, signature, superName, interfaces);
        }
    }
}
