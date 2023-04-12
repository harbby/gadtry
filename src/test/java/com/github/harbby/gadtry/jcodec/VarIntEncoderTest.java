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
package com.github.harbby.gadtry.jcodec;

import com.github.harbby.gadtry.jcodec.codecs.VarIntSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VarIntEncoderTest
{
    @Test
    public void encoder()
            throws IOException
    {
        EncoderChecker<Integer> checker = new EncoderChecker<>(new VarIntSerializer(true), int.class);
        byte[] bytes = checker.encoder(42354);  //42354
        Assertions.assertArrayEquals(bytes, new byte[] {-14, -54, 2});
    }

    @Test
    public void decoder()
            throws IOException
    {
        EncoderChecker<Integer> checker = new EncoderChecker<>(new VarIntSerializer(true), int.class);
        int value = checker.decoder(new byte[] {-14, -54, 2});
        Assertions.assertEquals(value, 42354);
    }

    @Test
    public void test()
            throws IOException
    {
        int value = 1073741824;
        EncoderChecker<Integer> checker = new EncoderChecker<>(new VarIntSerializer(false), int.class);
        byte[] bytes = checker.encoder(value);
        int rs = checker.decoder(bytes);
        Assertions.assertEquals(rs, value);
    }

    @Disabled
    @Test
    public void bigDataTest()
            throws IOException
    {
        EncoderChecker<Integer> checker1 = new EncoderChecker<>(new VarIntSerializer(false), int.class);
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            byte[] bytes = checker1.encoder(i);
            int v = checker1.decoder(bytes);
            Assertions.assertEquals(v, i);
        }

        EncoderChecker<Integer> checker2 = new EncoderChecker<>(new VarIntSerializer(true), int.class);
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            byte[] bytes = checker2.encoder(i);
            int v = checker2.decoder(bytes);
            Assertions.assertEquals(v, i);
        }
    }
}
