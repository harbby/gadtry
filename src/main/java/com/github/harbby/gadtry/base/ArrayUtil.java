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

import java.lang.reflect.Array;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ArrayUtil
{
    private ArrayUtil() {}

    public static final List<Class<?>> PRIMITIVE_TYPES = java.util.Arrays.asList(int.class, short.class, long.class,
            float.class, double.class, boolean.class, byte.class, char.class, void.class);

    /**
     * @param arrayType String.class Integer.class ...
     * @param length    array length
     * @param <T>       type
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T createArray(Class<?> arrayType, int length)
    {
        return (T) java.lang.reflect.Array.newInstance(arrayType, length);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T[]> getArrayClass(Class<T> aClass)
    {
        return (Class<T[]>) java.lang.reflect.Array.newInstance(aClass, 0).getClass();
    }

    /**
     * array merge
     *
     * @param arrays input arrays
     * @param <T>    array type
     * @return merged array
     */
    @SafeVarargs
    public static <T> T[] merge(T[]... arrays)
    {
        checkNotEmpty(arrays, "must arrays length > 0");
        int length = 0;
        for (T[] arr : arrays) {
            length += arr.length;
        }
        T[] mergeArr = createArray(arrays[0].getClass().getComponentType(), length);
        int index = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, mergeArr, index, arr.length);
            index += arr.length;
        }
        return mergeArr;
    }

    @SafeVarargs
    public static <T> T merge(T... arrays)
    {
        checkNotEmpty(arrays, "must arrays length > 0");
        int length = 0;
        for (T arr : arrays) {
            length += Array.getLength(arr);
        }
        Class<?> type = arrays[0].getClass().getComponentType();
        T mergeArr = createArray(type, length);

        int index = 0;
        for (T arr : arrays) {
            System.arraycopy(arr, 0, mergeArr, index, Array.getLength(arr));
            index += Array.getLength(arr);
        }
        return mergeArr;
    }

    public static <T> void checkNotEmpty(T[] array, String format, Object... objects)
    {
        requireNonNull(array, "array is null");
        if (array.length == 0) {
            throw new IllegalStateException(String.format(format, objects));
        }
    }
}
