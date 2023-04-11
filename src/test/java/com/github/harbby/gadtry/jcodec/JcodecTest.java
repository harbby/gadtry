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

import com.github.harbby.gadtry.base.TypeWrapper;
import com.github.harbby.gadtry.collection.ImmutableMap;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class JcodecTest
{
    @Test
    public void replaceRegister()
    {
        Jcodec jcodec = Jcodec.of();
        Serializer<Integer> integerSerializer = new Serializer<Integer>()
        {
            @Override
            public void write(Jcodec jcodec, OutputView output, Integer value)
            {
                output.writeInt(value == null ? Integer.MAX_VALUE : value);
            }

            @Override
            public Integer read(Jcodec jcodec, InputView input, Class<? extends Integer> typeClass)
            {
                int v = input.readInt();
                return v == Integer.MAX_VALUE ? null : v;
            }
        };
        jcodec.register(int.class, integerSerializer);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputView outputView = new StreamOutputView(outputStream);
        jcodec.writeObject(outputView, 123);
        jcodec.writeObject(outputView, null, integerSerializer);
        outputView.close();
        // read
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        StreamInputView inputView = new StreamInputView(inputStream);
        Jcodec readJcodec = Jcodec.of();
        readJcodec.register(int.class, integerSerializer);
        Assertions.assertEquals(123, readJcodec.readObject(inputView, int.class).intValue());
        Assertions.assertNull(readJcodec.readObject(inputView, int.class));
    }

    @Test
    public void notRegisterTest()
    {
        Jcodec jcodec = Jcodec.of();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputView outputView = new StreamOutputView(outputStream);
        TestClass1 in = new TestClass1(23);
        jcodec.writeClassAndObject(outputView, in);
        outputView.close();
        //write read
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        StreamInputView inputView = new StreamInputView(inputStream);
        Jcodec readJcodec = Jcodec.of();
        TestClass1 out = readJcodec.readClassAndObject(inputView);
        Assertions.assertNotSame(in, out);
        Assertions.assertEquals(in, out);
    }

    @Test
    public void registerTest()
    {
        Class<? extends Tuple2> tp2Class = Tuple2.of(null, null).getClass();
        Jcodec jcodec = Jcodec.of();
        jcodec.register(TestClass1.class);
        jcodec.register(tp2Class);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputView outputView = new StreamOutputView(outputStream);
        TestClass1 in = new TestClass1(23);
        jcodec.writeClassAndObject(outputView, in);
        outputView.close();
        //write read
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        StreamInputView inputView = new StreamInputView(inputStream);
        Jcodec readJcodec = Jcodec.of();
        readJcodec.register(TestClass1.class);
        readJcodec.register(tp2Class);
        TestClass1 out = readJcodec.readClassAndObject(inputView);
        Assertions.assertNotSame(in, out);
        Assertions.assertEquals(in, out);
    }

    @Test
    public void mapTest()
    {
        Jcodec jcodec = Jcodec.of();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputView outputView = new StreamOutputView(outputStream);
        jcodec.writeObject(outputView, ImmutableMap.of("a", 1.0f));
        outputView.close();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        InputView inputView = new StreamInputView(inputStream);
        Object rs = jcodec.readObject(inputView, Map.class);
        System.out.println(rs);
    }

    private static class TestClass1
    {
        private final int f1 = 123;
        private Integer f2;
        private Integer f3;
        private String f4 = "hello";
        private final int[] f5 = new int[] {1, 2, 3};
        private final String[] f6 = new String[] {"a", "b"};
        private Number[] f7 = new Number[] {1, 3.14f, 2L};
        private final Tuple2<String, Tuple2<String, Integer>> f8 = Tuple2.of("k", Tuple2.of("kk", 123));
        private final IdClass[] f9 = new IdClass[] {new IdClass(666)};
        private TypeWrapper f10 = TypeWrapper.BOOLEAN;
        private final Map<String, Integer> f11 = Collections.singletonMap("a", 1);
        private List<String> f12 = Arrays.asList("a", "b", "c");
        private final Integer[] f13 = new Integer[] {1, 2, null};
        private final Date[] f14 = new Date[] {new Timestamp(1), new Time(1)};
        private IdClass f15 = new IdClass(888);

        public TestClass1(int f2)
        {
            this.f2 = f2;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestClass1 that = (TestClass1) o;
            return f1 == that.f1 && Objects.equals(f2, that.f2) && Objects.equals(f3, that.f3) &&
                    Objects.equals(f4, that.f4) && Arrays.equals(f5, that.f5) &&
                    Arrays.equals(f6, that.f6) && Arrays.equals(f7, that.f7) &&
                    Objects.equals(f8, that.f8) && Arrays.equals(f9, that.f9) &&
                    f10 == that.f10 && Objects.equals(f11, that.f11) && Objects.equals(f12, that.f12) &&
                    Arrays.equals(f13, that.f13) && Arrays.equals(f14, that.f14) && Objects.equals(f15, that.f15);
        }

        @Override
        public int hashCode()
        {
            int result = Objects.hash(f1, f2, f3, f4, f8, f10, f11, f12, f15);
            result = 31 * result + Arrays.hashCode(f5);
            result = 31 * result + Arrays.hashCode(f6);
            result = 31 * result + Arrays.hashCode(f7);
            result = 31 * result + Arrays.hashCode(f9);
            result = 31 * result + Arrays.hashCode(f13);
            result = 31 * result + Arrays.hashCode(f14);
            return result;
        }
    }

    public static class IdClass
    {
        private final int id;

        public IdClass(int id)
        {
            this.id = id;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IdClass idClass = (IdClass) o;
            return id == idClass.id;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id);
        }
    }
}
