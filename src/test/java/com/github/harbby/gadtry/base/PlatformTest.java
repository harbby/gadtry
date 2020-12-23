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
package com.github.harbby.gadtry.base;

import com.github.harbby.gadtry.function.PrivilegedAction;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * ./gradlew clean test -Pjdk=java11 --tests *PlatformTest --info
 */
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
        Assert.assertNotNull(byteBuffer);
    }

    @Test
    public void defineClassTestReturnSetProxyClass()
            throws NotFoundException, IOException, CannotCompileException
    {
        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.getCtClass(PlatformTest.class.getName());
        ctClass.setName(PlatformTest.class.getName() + "$TestDomeDefineClass");
        byte[] bytes = ctClass.toBytecode();

        Class<?> newClass = Platform.defineClass(bytes, ClassLoader.getSystemClassLoader());
        Assert.assertNotNull(newClass);

        newClass = Platform.defineAnonymousClass(PlatformTest.class, bytes, new Object[0]);
        Assert.assertNotNull(newClass);
    }

    @Test
    public void doPrivilegedTest()
            throws Exception
    {
        String log = "hello;";
        OutputStream out = Platform.doPrivileged(FilterOutputStream.class, new PrivilegedAction<OutputStream>()
        {
            @Override
            public OutputStream run()
            {
                System.out.println(log);
                try {
                    Field field = FilterOutputStream.class.getDeclaredField("out");
                    field.setAccessible(true);
                    return (OutputStream) field.get(System.out);
                }
                catch (Exception e) {
                    unsafe.throwException(e);
                    throw new IllegalStateException("unchecked");
                }
            }
        });
        Assert.assertNotNull(out);
    }

    @Test
    public void defineClassByUnsafeTestReturnSetProxyClass()
            throws NotFoundException, IOException, CannotCompileException
    {
        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.getCtClass(PlatformTest.class.getName());
        ctClass.setName(PlatformTest.class.getName() + "$TestDomeDefineClassByUnsafe");
        byte[] bytes = ctClass.toBytecode();

        Class<?> newClass = Platform.defineClassByUnsafe(bytes, ClassLoader.getSystemClassLoader());
        Assert.assertNotNull(newClass);

        newClass = Platform.defineAnonymousClass(PlatformTest.class, bytes, new Object[0]);
        Assert.assertNotNull(newClass);
    }

    @Test
    public void throwExceptionTest()
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
