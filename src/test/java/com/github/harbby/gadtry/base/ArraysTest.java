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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Arrays.PRIMITIVE_TYPES;

public class ArraysTest
{
    @Test
    public void createArrayTest()
    {
        Integer[] a2 = Arrays.createArray(Integer.class, 2);
        Assert.assertEquals(a2.length, 2);
        Assert.assertEquals(a2.getClass(), Integer[].class);

        try {
            Arrays.createArray(int.class, 0);
            Assert.fail();
        }
        catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void createPrimitiveArray()
    {
        int[] a1 = Arrays.createPrimitiveArray(int.class, 2);
        Assert.assertEquals(a1.length, 2);
        Assert.assertEquals(a1.getClass(), int[].class);

        Integer[] a2 = Arrays.createPrimitiveArray(Integer.class, 2);
        Assert.assertEquals(a2.length, 2);
        Assert.assertEquals(a2.getClass(), Integer[].class);
    }

    @Test
    public void createPrimitiveByArrayClassTest()
    {
        int[] array2 = Arrays.createPrimitiveByArrayClass(int[].class, 2);
        Assert.assertEquals(array2.length, 2);
        Assert.assertEquals(array2.getClass(), int[].class);
        Assert.assertEquals(Arrays.createPrimitiveByArrayClass(Object[].class, 0).getClass(), Object[].class);
    }

    @Test
    public void createObjectArrayByArrayClassTest()
    {
        Object[] array = Arrays.createArrayByArrayClass(Object[].class, 2);
        Assert.assertEquals(array.length, 2);
        Assert.assertEquals(array.getClass(), Object[].class);
    }

    @Test
    public void toArrayTest()
    {
        Assert.assertArrayEquals(new Integer[] {1, 2}, new Object[] {1, 2});
        List<Integer> list = java.util.Arrays.asList(1, 2, 3, 4);
        Integer[] out = Arrays.toArray(list, Integer[].class);
        Assert.assertArrayEquals(out, list.toArray());
        Assert.assertArrayEquals(Arrays.toArray(new ArrayList<>(), Object[].class), new ArrayList<>().toArray());
    }

    @Test
    public void toPrimitiveIntegerArrayTest()
    {
        List<Integer> list = java.util.Arrays.asList(1, 2, 3, 4);
        Integer[] out = Arrays.toArray(list, Integer[].class);
        Assert.assertArrayEquals(out, new Integer[] {1, 2, 3, 4});

        try {
            Arrays.toPrimitiveArray(list, Integer[].class);
            Assert.fail();
        }
        catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void toPrimitiveIntArrayTest()
    {
        List<Integer> list = java.util.Arrays.asList(1, 2, 3, 4);
        int[] out = Arrays.toPrimitiveArray(list, int[].class);

        Assert.assertArrayEquals(out, new int[] {1, 2, 3, 4});
    }

    @Test
    public void toPrimitiveLongArrayTest()
    {
        List<Long> list = java.util.Arrays.asList(1L, 2L, 3L, 4L);
        long[] out = Arrays.toPrimitiveArray(list, long[].class);

        Assert.assertArrayEquals(out, new long[] {1L, 2L, 3L, 4L});
    }

    @Test
    public void toPrimitiveShortArrayTest()
    {
        List<Short> list = java.util.Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4);
        short[] out = Arrays.toPrimitiveArray(list, short[].class);

        Assert.assertArrayEquals(out, new short[] {(short) 1, (short) 2, (short) 3, (short) 4});
    }

    @Test
    public void toPrimitiveFloatArrayTest()
    {
        List<Float> list = java.util.Arrays.asList(1f, 2f, 3f, 4f);
        float[] out = Arrays.toPrimitiveArray(list, float[].class);

        Assert.assertArrayEquals(out, new float[] {1.0f, 2.0f, 3.0f, 4.0f}, 0);
    }

    @Test
    public void toPrimitiveDoubleArrayTest()
    {
        List<Double> list = java.util.Arrays.asList(1.0, 2.0, 3.0, 4.0);
        double[] out = Arrays.toPrimitiveArray(list, double[].class);

        Assert.assertArrayEquals(out, new double[] {1.0, 2.0, 3.0, 4.0}, 0);
    }

    @Test
    public void toPrimitiveByteArrayTest()
    {
        List<Byte> list = java.util.Arrays.asList((byte) 1, (byte) 2, (byte) 3, (byte) 4);
        byte[] out = Arrays.toPrimitiveArray(list, byte[].class);

        Assert.assertArrayEquals(out, new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4});
    }

    @Test
    public void toPrimitiveBooleanArrayTest()
    {
        List<Boolean> list = java.util.Arrays.asList(true, false, true, false);
        boolean[] out = Arrays.toPrimitiveArray(list, boolean[].class);

        Assert.assertArrayEquals(out, new boolean[] {true, false, true, false});
    }

    @Test
    public void toPrimitiveCharArrayTest()
    {
        List<Character> list = java.util.Arrays.asList('a', 'b', 'c', 'd');
        char[] out = Arrays.toPrimitiveArray(list, char[].class);

        Assert.assertArrayEquals(out, new char[] {'a', 'b', 'c', 'd'});
    }

    @Test
    public void toPrimitiveObjectArrayTest()
    {
        List<Character> list = java.util.Arrays.asList('a', 'b', 'c', 'd');
        Object[] out = Arrays.toPrimitiveArray(list, Object[].class);

        Assert.assertArrayEquals(out, new Object[] {'a', 'b', 'c', 'd'});
    }

    @Test
    public void getArrayClassTest()
    {
        Class<?> arrayClass = Arrays.getArrayClass(int.class);
        Assert.assertEquals(int[].class, arrayClass);

        Class<?> arrayClass2 = Arrays.getArrayClass(Integer.class);
        Assert.assertEquals(Integer[].class, arrayClass2);
    }

    @Test
    public void arrayToString()
    {
        List<String> arrStr = PRIMITIVE_TYPES.stream()
                .filter(x -> x != void.class)
                .map(x -> Arrays.createPrimitiveArray(x, 0))
                .map(Arrays::arrayToString)
                .collect(Collectors.toList());
        Assert.assertEquals(arrStr.size(), PRIMITIVE_TYPES.size() - 1);
        arrStr.forEach(x -> Assert.assertEquals("[]", x));

        Assert.assertEquals(Arrays.arrayToString(new Object[] {1}), "[1]");

        try {
            Arrays.arrayToString("1");
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertEquals("The given argument is no array.", e.getMessage());
        }
    }
}
