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

import com.github.harbby.gadtry.io.IOUtils;
import com.github.harbby.gadtry.jcodec.codecs.ArraySerializers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BooleanArrayEncoderTest
{
    private final EncoderChecker<boolean[]> checker = new EncoderChecker<>(new ArraySerializers.BooleanArraySerializer(), boolean[].class);

    @Test
    public void test1()
    {
        boolean[] booleans = new boolean[] {true, false, false, false, true, true, true, false, true};
        byte[] bytes = new byte[(booleans.length + 7) >> 3];
        IOUtils.zipBoolArray(booleans, 0, bytes, 0, booleans.length);
        Assertions.assertArrayEquals(bytes, new byte[] {-114, -128});
        //unzip
        boolean[] rs = new boolean[booleans.length];
        IOUtils.unzipBoolArray(bytes, 0, rs, 0, rs.length);
        Assertions.assertArrayEquals(booleans, rs);
    }

    @Test
    public void test2()
            throws IOException
    {
        boolean[] value = new boolean[] {true, false, false, false, true, true, true, false, true};
        byte[] bytes = checker.encoder(value);
        Assertions.assertArrayEquals(bytes, new byte[] {10, -114, -128});
        boolean[] rs = checker.decoder(bytes);
        Assertions.assertArrayEquals(value, rs);
    }
}
