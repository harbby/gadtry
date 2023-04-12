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

import com.github.harbby.gadtry.jcodec.codecs.VarLongSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

public class VarLongEncoderTest
{
    @Test
    public void encoderTest()
            throws IOException
    {
        EncoderChecker<Long> checker = new EncoderChecker<>(new VarLongSerializer(false), long.class);
        long value = System.currentTimeMillis();
        byte[] bytes = checker.encoder(value);
        long rs = checker.decoder(bytes);
        Assertions.assertEquals(value, rs);
    }

    @Test
    public void encoderTest2()
            throws IOException
    {
        EncoderChecker<Long> checker = new EncoderChecker<>(new VarLongSerializer(true), long.class);
        long value = -1;
        byte[] bytes = checker.encoder(value);
        long rs = checker.decoder(bytes);
        Assertions.assertEquals(value, rs);
    }

    @Test
    public void random10WDataTest()
            throws IOException
    {
        EncoderChecker<Long> checker1 = new EncoderChecker<>(new VarLongSerializer(true), long.class);
        Random random = new Random();
        for (int i = 0; i < 10_0000; i++) {
            long value = random.nextLong();
            byte[] bytes = checker1.encoder(value);
            long rs = checker1.decoder(bytes);
            Assertions.assertEquals(rs, value);
        }

        EncoderChecker<Long> checker2 = new EncoderChecker<>(new VarLongSerializer(false), long.class);
        for (int i = 0; i < 10_0000; i++) {
            long value = random.nextLong();
            byte[] bytes = checker2.encoder(value);
            long rs = checker2.decoder(bytes);
            Assertions.assertEquals(rs, value);
        }
    }
}
