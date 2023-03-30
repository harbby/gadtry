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
package com.github.harbby.gadtry.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public class ByteBufferInputStreamTest
{
    @Test
    public void coreFeaturesTest()
    {
        byte[] bytes = new byte[] {-1, 2};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ByteBufferInputStream inputStream = new ByteBufferInputStream(buffer, ByteBuffer.wrap(new byte[0]));

        Assertions.assertTrue(inputStream.available() > 0);
        Assertions.assertEquals(inputStream.read(), 255); //255 = -1 & 0xFF
        Assertions.assertTrue(inputStream.available() > 0);
        Assertions.assertEquals(inputStream.read(), 2);

        Assertions.assertEquals(inputStream.available(), 0);
        Assertions.assertEquals(inputStream.read(), -1);
        Assertions.assertEquals(inputStream.read(), -1);
        Assertions.assertEquals(inputStream.available(), 0);
    }

    @Test
    public void markResetTest()
    {
        ByteBuffer buffer1 = ByteBuffer.wrap(new byte[] {1, 2, 3});
        ByteBuffer buffer2 = ByteBuffer.wrap(new byte[] {4, 5, 6});
        ByteBufferInputStream inputStream = new ByteBufferInputStream(buffer1, buffer2);
        Assertions.assertTrue(inputStream.markSupported());
        Assertions.assertEquals(inputStream.read(), 1);
        Assertions.assertEquals(inputStream.read(), 2);
        inputStream.mark(1);
        Assertions.assertEquals(inputStream.read(), 3);
        Assertions.assertEquals(inputStream.read(), 4);
        inputStream.reset();
        Assertions.assertEquals(inputStream.read(), 3);
        Assertions.assertEquals(inputStream.read(), 4);
        inputStream.reset();
        Assertions.assertEquals(inputStream.read(), 3);
        Assertions.assertEquals(inputStream.read(), 4);
    }
}
