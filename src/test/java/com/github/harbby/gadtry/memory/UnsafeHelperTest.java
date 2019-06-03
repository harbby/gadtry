/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.memory;

import com.github.harbby.gadtry.base.Closeables;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.IOException;
import java.nio.ByteBuffer;

public class UnsafeHelperTest
{
    private final Unsafe unsafe = UnsafeHelper.getUnsafe();

    @Test
    public void reallocateMemory()
    {
        long address = unsafe.allocateMemory(1024);
        long newAddress = UnsafeHelper.reallocateMemory(address, 1024, 2048);

        try (Closeables<Long> closeables = Closeables.autoClose(newAddress, unsafe::freeMemory)) {
            Assert.assertTrue(newAddress > 0);
        }
    }

    @Test
    public void allocateDirectBuffer()
    {
        ByteBuffer byteBuffer = UnsafeHelper.allocateDirectBuffer(1024);
        Assert.assertNotNull(byteBuffer);
    }

    @Test
    public void defineClassTestReturnSetProxyClass()
            throws NotFoundException, IOException, CannotCompileException
    {
        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.getCtClass(UnsafeHelperTest.class.getName());
        ctClass.setName("com.github.harbby.gadtry.memory.TestDome");
        byte[] bytes = ctClass.toBytecode();

        Class<?> newClass = UnsafeHelper.defineClass(bytes, this.getClass().getClassLoader());
        Assert.assertNotNull(newClass);
    }

    @Test
    public void throwException()
    {
        try {
            UnsafeHelper.throwException(new IOException("IO_test"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
            Assert.assertEquals("IO_test", e.getMessage());
        }
    }
}
