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
package com.github.harbby.gadtry.memory;

import com.github.harbby.gadtry.base.Closeables;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PlatformTest
{
    private final Unsafe unsafe = Platform.getUnsafe();

    @Test(expected = IllegalArgumentException.class)
    public void allocateAlignMemoryErrorTest()
    {
        Platform.allocateAlignMemory(10, 31);
    }

    @Test
    public void allocateAlignMemoryTest()
    {
        long[] address = Platform.allocateAlignMemory(10, 32);
        long base = address[0];
        long dataAddress = address[1];

        Assert.assertEquals(0, base % 16);
        Assert.assertEquals(0, dataAddress % 32);
    }

    @Test
    public void putIntsTest()
    {
        int[] arr = new int[] {1, 2, 3, 4, 5};
        long intArr = unsafe.allocateMemory(10 << 2);
        Platform.putInts(intArr, arr);
        Assert.assertArrayEquals(Platform.getInts(intArr, 5), arr);
        //--------------------
        int[] arr2 = new int[3];
        Platform.getInts(intArr, arr2, 2);
        Assert.assertArrayEquals(arr2, new int[] {1, 2, 0});
    }

    @Test
    public void putCountInts()
    {
        long intArr = Platform.allocateMemory(10 << 2);
        Platform.putInts(intArr, new int[] {1, 1, 1, 1, 1, 1});
        int[] arr = new int[] {1, 2, 3, 4, 5};
        Platform.putInts(intArr, arr, 3);
        Assert.assertArrayEquals(Platform.getInts(intArr, 5), new int[] {1, 2, 3, 1, 1});
    }

    @Test
    public void reallocateMemory()
    {
        long address = unsafe.allocateMemory(1024);
        long newAddress = Platform.reallocateMemory(address, 1024, 2048);

        try (Closeables<Long> closeables = Closeables.autoClose(newAddress, Platform::freeMemory)) {
            Assert.assertTrue(newAddress > 0);
        }
    }

    @Test
    public void allocateDirectBuffer()
    {
        //jdk.internal.ref.Cleaner.create()
        ByteBuffer byteBuffer = Platform.allocateDirectBuffer(1024);
        ((DirectBuffer) byteBuffer).cleaner().clean();
        Assert.assertNotNull(byteBuffer);
    }

    @Test
    public void defineClassTestReturnSetProxyClass()
            throws NotFoundException, IOException, CannotCompileException
    {
        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.getCtClass(PlatformTest.class.getName());
        ctClass.setName("com.github.harbby.gadtry.memory.TestDome");
        byte[] bytes = ctClass.toBytecode();

        Class<?> newClass = Platform.defineClass(bytes, PlatformTest.class.getClassLoader());
        Assert.assertNotNull(newClass);

        newClass = Platform.defineAnonymousClass(PlatformTest.class, bytes, new Object[0]);
        Assert.assertNotNull(newClass);
    }

    @Test
    public void throwException()
    {
        try {
            Platform.throwException(new IOException("IO_test"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
            Assert.assertEquals("IO_test", e.getMessage());
        }
    }
}
