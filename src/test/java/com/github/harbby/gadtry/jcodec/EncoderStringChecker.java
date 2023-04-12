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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EncoderStringChecker
{
    private final EncoderChecker<String> checker = new EncoderChecker<>(Jcodecs.string(), String.class);

    @Test
    public void test1()
    {
        String s = "abc123";
        byte[] bytes = checker.encoder(s);
        String rs = checker.decoder(bytes);
        Assertions.assertEquals(s, rs);
    }

    @Test
    public void test2()
    {
        String s = "a";
        byte[] bytes = checker.encoder(s);
        String rs = checker.decoder(bytes);
        Assertions.assertEquals(s, rs);
    }

    @Test
    public void test3()
    {
        String s = "a齐1";
        byte[] bytes = checker.encoder(s);
        String rs = checker.decoder(bytes);
        Assertions.assertEquals(s, rs);
    }

    @Test
    public void test4()
    {
        List<String> stringList = Arrays.asList("a", "b", "c");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputView dataOutputView = new StreamOutputView(outputStream);
        stringList.forEach(dataOutputView::writeString);
        dataOutputView.close();
        byte[] bytes = outputStream.toByteArray();

        InputView dataInputView = new StreamInputView(new ByteArrayInputStream(bytes));
        String s1 = dataInputView.readString();
        String s2 = dataInputView.readString();
        String s3 = dataInputView.readString();
        Assertions.assertEquals(stringList, Arrays.asList(s1, s2, s3));
    }

    @Test
    public void randomAsciiStringTest()
    {
        Random random = new Random();
        byte[] buffer = new byte[64];
        for (int i = 0; i < 1000; i++) {
            int len = random.nextInt(65);
            random.nextBytes(buffer);
            for (int k = 0; k < len; k++) {
                buffer[k] &= 0x7F;  //Ascii is [0-127]
            }
            String str = new String(buffer, 0, len, StandardCharsets.US_ASCII);

            byte[] bytes = checker.encoder(str);
            String rs = checker.decoder(bytes);
            Assertions.assertEquals(str, rs);
        }
    }
}
