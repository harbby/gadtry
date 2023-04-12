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

import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.collection.MutableMap;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.jcodec.codecs.ArraySerializers;
import com.github.harbby.gadtry.jcodec.codecs.MapSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EncoderTest
{
    private final Jcodec jcodec = Jcodec.of();

    @Test
    public void javaSerializeTest()
            throws IOException
    {
        Tuple2<Long, Long> tuple2 = Tuple2.of(1L, 2L);
        byte[] bytes = Serializables.serialize(tuple2);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputView dataOutput = new StreamOutputView(outputStream);
        Serializer<Tuple2<Long, Long>> tuple2Encoder = Jcodecs.tuple2(long.class, long.class, Jcodecs.jLong(), Jcodecs.jLong());
        tuple2Encoder.write(jcodec, dataOutput, tuple2);
        dataOutput.close();
        Assertions.assertEquals(outputStream.toByteArray().length, 16);
        Assertions.assertTrue(bytes.length > 16 * 10);
    }

    @Test
    public void Tuple2JavaSerializeThanJavaSerializeTest()
    {
        BiFunction<Tuple2<Long, Long>, Serializer<Tuple2<Long, Long>>, byte[]> checker = (tuple2, encoder) -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputView dataOutputView = new StreamOutputView(outputStream);
            encoder.write(jcodec, dataOutputView, tuple2);
            dataOutputView.close();
            Tuple2<Long, Long> checkObj = encoder.read(jcodec,
                    new StreamInputView(new ByteArrayInputStream(outputStream.toByteArray())),
                    JavaTypes.classTag(Tuple2.class));
            Assertions.assertEquals(tuple2, checkObj);
            return outputStream.toByteArray();
        };
        byte[] bytes = checker.apply(Tuple2.of(1L, 2L), Jcodecs.javaEncoder());
        byte[] bytes2 = checker.apply(Tuple2.of(1L, 2L), Jcodecs.tuple2(Long.class, Long.class, Jcodecs.javaEncoder(), Jcodecs.javaEncoder()));
        Assertions.assertTrue(bytes.length > bytes2.length);
    }

    @Test
    public void mapSerializeTest()
    {
        Serializer<Map<String, String>> mapEncoder = new MapSerializer<>();
        EncoderChecker<Map<String, String>> checker0 = new EncoderChecker<>(mapEncoder, JavaTypes.classTag(Map.class));
        Consumer<Map<String, String>> checker = map -> {
            byte[] bytes = checker0.encoder(map);
            Map<String, String> decoder = checker0.decoder(bytes);
            Assertions.assertEquals(decoder, map);
        };
        checker.accept(MutableMap.of(
                "weight", "1",
                "height", "2",
                null, "3",
                "ss", null));
        checker.accept(null);
    }

    @Test
    public void arrayEncoderTest()
    {
        Serializer<String[]> encoder = new ArraySerializers.StringArraySerializer();
        Consumer<String[]> checker = array -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputView dataOutput = new StreamOutputView(outputStream);
            encoder.write(jcodec, dataOutput, array);
            dataOutput.close();
            String[] out = encoder.read(jcodec, new StreamInputView(new ByteArrayInputStream(outputStream.toByteArray())), String[].class);
            Assertions.assertArrayEquals(out, array);
        };
        checker.accept(new String[] {"a1", "a2", "12345"});
        checker.accept(null);
    }

    @Test
    public void booleanSerializeTest()
    {
        EncoderChecker<Boolean> checker = new EncoderChecker<>(Jcodecs.jBoolean(), boolean.class);
        byte[] bytes = checker.encoder(true);
        Boolean rs = checker.decoder(bytes);
        Assertions.assertEquals(true, rs);
    }

    @Test
    public void stringSerializeTest()
    {
        EncoderChecker<String> checker = new EncoderChecker<>(Jcodecs.string(), String.class);
        byte[] bytes = checker.encoder("yes");
        String decoder = checker.decoder(bytes);
        Assertions.assertEquals("yes", decoder);
    }

    @Test
    public void byteSerializeTest()
    {
        EncoderChecker<Byte> checker = new EncoderChecker<>(Jcodecs.jByte(), Byte.class);
        byte a = (byte) 127;
        byte[] bytes = checker.encoder(a);
        Assertions.assertEquals(a, checker.decoder(bytes).byteValue());
    }

    @Test
    public void charSerializeTest()
    {
        EncoderChecker<Character> checker = new EncoderChecker<>(Jcodecs.jChar(), Character.class);
        char a = 'a';
        byte[] bytes = checker.encoder(a);
        Assertions.assertEquals(a, checker.decoder(bytes).charValue());
    }

    @Test
    public void shortSerializeTest()
    {
        EncoderChecker<Short> checker = new EncoderChecker<>(Jcodecs.jShort(), Short.class);
        short a = (short) 10;
        byte[] bytes = checker.encoder(a);
        Assertions.assertEquals(a, checker.decoder(bytes).shortValue());
    }

    @Test
    public void floatSerializeTest()
    {
        EncoderChecker<Float> checker = new EncoderChecker<>(Jcodecs.jFloat(), Float.class);
        float a = 10.0f;
        byte[] bytes = checker.encoder(a);
        Assertions.assertEquals(a, checker.decoder(bytes), 0.00001f);
    }
}
