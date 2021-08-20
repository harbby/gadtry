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

import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.IOException;
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
        ByteBuffer byteBuffer = Platform.allocateDirectBuffer(1024);
        Assert.assertNotNull(byteBuffer);
        Platform.freeDirectBuffer(byteBuffer);
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
