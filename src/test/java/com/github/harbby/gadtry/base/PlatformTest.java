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

import com.github.harbby.gadtry.democode.PlatFormOther;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.Driver;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * ./gradlew clean test -Pjdk=java11 --tests *PlatformTest --info
 */
public class PlatformTest
{
    private final Unsafe unsafe = Platform.getUnsafe();

    @Test
    public void getArrayComparatorTest()
    {
        if (Platform.getJavaVersion() < 9) {
            return;
        }
        Comparator<int[]> comparator = Platform.getArrayComparator(int[].class);
        int c = comparator.compare(new int[] {1}, new int[] {2});
        Assertions.assertEquals(-1, c);
    }

    @Test
    public void allocateAlignMemoryErrorTest()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Platform.allocateAlignMemory(10, 31));
    }

    @Test
    public void allocateDirectBufferTest()
    {
        ByteBuffer byteBuffer = Platform.allocateDirectBuffer(24, 64);
        try {
            byteBuffer.putLong(123);
            byteBuffer.flip();
            Assertions.assertEquals(byteBuffer.getLong(), 123);
        }
        finally {
            Platform.freeDirectBuffer(byteBuffer);
        }
    }

    @Test
    public void getDiskPageSizeTest()
    {
        int pageSize = Platform.pageSize();
        Assertions.assertTrue(pageSize > 0);
    }

    @Test
    public void allocateAlignMemoryTest()
    {
        long base = Platform.allocateAlignMemory(10, 32);
        try {
            long dataAddress = Platform.getAlignedDataAddress(base, 32);
            Assertions.assertEquals(0, base % 16);
            Assertions.assertEquals(0, dataAddress % 32);
        }
        finally {
            unsafe.freeMemory(base);
        }
    }

    @Test
    public void reallocateMemory()
    {
        long address = unsafe.allocateMemory(1024);
        long newAddress = Platform.reallocateMemory(address, 1024, 2048);

        try {
            Assertions.assertTrue(newAddress > 0);
        }
        finally {
            Platform.freeMemory(newAddress);
        }
    }

    @Test
    public void allocateDirectBuffer()
    {
        ByteBuffer b1 = ByteBuffer.allocateDirect(12);
        ByteBuffer byteBuffer = Platform.allocateDirectBuffer(1024);
        try {
            Assertions.assertNotNull(byteBuffer);
            byteBuffer.putLong(314);
            byteBuffer.flip();
            Assertions.assertEquals(byteBuffer.getLong(), 314);
        }
        finally {
            Platform.freeDirectBuffer(byteBuffer);
        }
    }

    @Test
    public void throwExceptionTest()
    {
        try {
            Platform.throwException(new IOException("IO_test"));
            Assertions.fail();
        }
        catch (Exception e) {
            Assertions.assertTrue(e instanceof IOException);
            Assertions.assertEquals("IO_test", e.getMessage());
        }
    }

    @Test
    public void getSystemClassLoaderJarsTest()
    {
        if (Platform.getJavaVersion() < 16) {
            List<URL> urlList = PlatFormOther.getSystemClassLoaderJars();
            Assertions.assertTrue(urlList.stream().anyMatch(x -> x.getPath().contains("junit")));
        }
    }

    @Disabled
    @Test
    public void loadExtJarToSystemClassLoaderTest()
            throws ClassNotFoundException
    {
        if (Platform.getJavaVersion() >= 16) {
            return;
        }
        Assertions.assertFalse(PlatFormOther.getSystemClassLoaderJars().stream().anyMatch(x -> x.getPath().contains("h2-1.4.191.jar")));
        Try.of(() -> ClassLoader.getSystemClassLoader().loadClass("org.h2.Driver"))
                .onSuccess(Assertions::fail)
                .matchException(ClassNotFoundException.class, e -> {})
                .doTry();

        URL url = this.getClass().getClassLoader().getResource("version1/h2-1.4.191.jar");
        PlatFormOther.loadExtJarToSystemClassLoader(Arrays.asList(url));

        Assertions.assertTrue(PlatFormOther.getSystemClassLoaderJars().stream().anyMatch(x -> x.getPath().contains("h2-1.4.191.jar")));
        Assertions.assertTrue(Driver.class.isAssignableFrom(ClassLoader.getSystemClassLoader().loadClass("org.h2.Driver")));
    }
}
