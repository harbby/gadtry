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

import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.collection.ImmutableList;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.util.List;

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
        String classCode = "public class DirectBufferCloseableImpl\n" +
                "            implements com.github.harbby.gadtry.compiler.JavaClassCompilerTest.DirectBufferCloseable \n" +
                "    {\n" +
                "        @Override\n" +
                "        public void free(sun.nio.ch.DirectBuffer buffer)\n" +
                "        {\n" +
                "            buffer.cleaner().clean();\n" +
                "        }\n" +
                "    }";
        List<String> ops = Platform.getJavaVersion() > 8 ? ImmutableList.of("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED") : ImmutableList.of();
        byte[] bytes = javaClassCompiler.doCompile(className, classCode, ops).getClassByteCodes().get(className);
        ByteClassLoader byteClassLoader = new ByteClassLoader(Platform.class.getClassLoader());
        Class<DirectBufferCloseable> directBufferCloseableClass = (Class<DirectBufferCloseable>) byteClassLoader.loadClass(className, bytes);
        DirectBufferCloseable cleaner = directBufferCloseableClass.newInstance();
        cleaner.free((DirectBuffer) ByteBuffer.allocateDirect(12));
    }
}
