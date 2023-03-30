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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayUtilTest
{
    @Test
    public void createArrayTest()
    {
        Integer[] a2 = ArrayUtil.createArray(Integer.class, 2);
        Assertions.assertEquals(a2.length, 2);
        Assertions.assertEquals(a2.getClass(), Integer[].class);
    }

    @Test
    public void createPrimitiveArray()
    {
        int[] a1 = ArrayUtil.createArray(int.class, 2);
        Assertions.assertEquals(a1.length, 2);
        Assertions.assertEquals(a1.getClass(), int[].class);

        Integer[] a2 = ArrayUtil.createArray(Integer.class, 2);
        Assertions.assertEquals(a2.length, 2);
        Assertions.assertEquals(a2.getClass(), Integer[].class);
    }

    @Test
    public void createPrimitiveByArrayClassTest()
    {
        int[] array2 = ArrayUtil.createArray(int.class, 2);
        Assertions.assertEquals(array2.length, 2);
        Assertions.assertEquals(array2.getClass(), int[].class);
        Assertions.assertEquals(ArrayUtil.createArray(Object.class, 0).getClass(), Object[].class);
    }

    @Test
    public void createObjectArrayByArrayClassTest()
    {
        Object[] array = ArrayUtil.createArray(Object.class, 2);
        Assertions.assertEquals(array.length, 2);
        Assertions.assertEquals(array.getClass(), Object[].class);
    }

    @Test
    public void getArrayClassTest()
    {
        Class<?> arrayClass = ArrayUtil.getArrayClass(int.class);
        Assertions.assertEquals(int[].class, arrayClass);

        Class<?> arrayClass2 = ArrayUtil.getArrayClass(Integer.class);
        Assertions.assertEquals(Integer[].class, arrayClass2);
    }

    @Test
    public void arrayMerge()
    {
        String[] arr = ArrayUtil.<String>merge(new String[] {"1", "2"}, new String[] {"3", "4"}, new String[] {"5"});
        Assertions.assertArrayEquals(arr, new String[] {"1", "2", "3", "4", "5"});

        try {
            ArrayUtil.merge();
            Assertions.fail();
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "must arrays length > 0");
        }
    }

    @Test
    public void mergePrimitiveByArray()
    {
        int[] arr = ArrayUtil.merge(new int[] {1, 2}, new int[] {3, 4}, new int[] {5});
        Assertions.assertArrayEquals(arr, new int[] {1, 2, 3, 4, 5});
        try {
            ArrayUtil.merge();
            Assertions.fail();
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "must arrays length > 0");
        }
    }
}
