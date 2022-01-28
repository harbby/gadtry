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
package com.github.harbby.gadtry.compiler;

import com.github.harbby.gadtry.aop.MockGo;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.collection.ImmutableList;
import org.junit.Assert;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;

import java.nio.ByteBuffer;
import java.util.List;

import static com.github.harbby.gadtry.aop.MockGo.when;
import static com.github.harbby.gadtry.aop.mockgo.MockGoArgument.any;

public class JavaClassCompilerTest
{
    public static interface DirectBufferCloseable
    {
        void free(sun.nio.ch.DirectBuffer buffer);
    }

    @Test
    public void doCompileTest()
            throws Exception
    {
        JavaClassCompiler javaClassCompiler = new JavaClassCompiler();
        String className = "DirectBufferCloseableImpl";
        String classCode = "public class " + className + "\n" +
                "            implements com.github.harbby.gadtry.compiler.JavaClassCompilerTest.DirectBufferCloseable \n" +
                "    {\n" +
                "        @Override\n" +
                "        public void free(sun.nio.ch.DirectBuffer buffer)\n" +
                "        {\n" +
                "            buffer.cleaner().clean();\n" +
                "        }\n" +
                "    }";
        byte[] bytes;
        if (Platform.getJavaVersion() > 8) {
            List<String> ops = ImmutableList.of("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
                    "--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
                    "-source", "11", "-target", "11");
            bytes = javaClassCompiler.doCompile(className, classCode, ops).getClassByteCodes().get(className);
        }
        else {
            List<String> ops = ImmutableList.of("-source", "1.7", "-target", "1.7");
            bytes = javaClassCompiler.doCompile(className, classCode, ops).getClassByteCodes().get(className);
        }
        ByteClassLoader byteClassLoader = new ByteClassLoader(Platform.class.getClassLoader());
        Class<DirectBufferCloseable> directBufferCloseableClass = (Class<DirectBufferCloseable>) byteClassLoader.loadClass(className, bytes);
        DirectBufferCloseable cleaner = directBufferCloseableClass.newInstance();
        cleaner.free((DirectBuffer) ByteBuffer.allocateDirect(12));
    }

    @Test
    public void failedTest()
    {
        JavaCompiler javaCompiler = MockGo.mock(JavaCompiler.class);
        JavaCompiler.CompilationTask task = MockGo.mock(JavaCompiler.CompilationTask.class);
        StandardJavaFileManager javaFileManager = MockGo.mock(StandardJavaFileManager.class);
        when(javaCompiler.getTask(any(), any(), any(), any(), any(), any())).thenReturn(task);
        when(javaCompiler.getStandardFileManager(any(), any(), any())).thenReturn(javaFileManager);
        when(task.call()).thenReturn(false);

        JavaClassCompiler javaClassCompiler = new JavaClassCompiler(javaCompiler, null, true);
        try {
            javaClassCompiler.doCompile("test", "");
            Assert.fail();
        }
        catch (CompileException e) {
            Assert.assertEquals(e.getMessage(), "Compilation failed");
        }

        when(task.call()).thenReturn(true);
        try {
            javaClassCompiler.doCompile("test", "");
            Assert.fail();
        }
        catch (CompileException e) {
            Assert.assertEquals(e.getMessage(), "test: Class file not created by compilation.");
        }
    }

    @Test
    public void failedTest2()
    {
        JavaClassCompiler javaClassCompiler = new JavaClassCompiler();
        try {
            javaClassCompiler.doCompile("test", "public class test {\n    public static void main(String[] args)\n" +
                    "    {\n" +
                    "        System.o.println();\n" +
                    "    }\n}");
            Assert.fail();
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof CompileException);
        }
    }
}
