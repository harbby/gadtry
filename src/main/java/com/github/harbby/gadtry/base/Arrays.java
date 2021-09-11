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
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class Arrays
{
    private Arrays() {}

    public static final List<Class<?>> PRIMITIVE_TYPES = java.util.Arrays.asList(int.class, short.class, long.class,
            float.class, double.class, boolean.class, byte.class, char.class, void.class);

    /**
     * @param arrayType String.class Integer.class ...
     * @param length    array length
     * @param <T>       type
     * @return T array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(Class<T> arrayType, int length)
    {
        if (arrayType.isPrimitive()) {
            throw new UnsupportedOperationException("this Primitive have't support! But yours use: " +
                    "java.lang.reflect.Array.newInstance(int.class, length)");
        }
        else {
            return (T[]) java.lang.reflect.Array.newInstance(arrayType, length);
        }
    }

    /**
     * @param arrayType String.class Integer.class ...
     * @param length    array length
     * @param <T>       type
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T createPrimitiveArray(Class<?> arrayType, int length)
    {
        return (T) java.lang.reflect.Array.newInstance(arrayType, length);
    }

    /**
     * @param arrayClass String[].class
     * @param length     array length
     * @param <T>        type
     * @return T[]
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArrayByArrayClass(Class<T[]> arrayClass, int length)
    {
        checkState(arrayClass.isArray(), "ArrayClass %s must arrayClass.isArray()");
        if (((Object) arrayClass == Object[].class)) {
            return (T[]) new Object[length];
        }

        Class<?> aClass = arrayClass.getComponentType();
        return (T[]) createArray(aClass, length);
    }

    /**
     * @param arrayClass String[].class
     * @param length     array length
     * @param <T>        type T
     * @return T[]
     */
    @SuppressWarnings("unchecked")
    public static <T> T createPrimitiveByArrayClass(Class<?> arrayClass, int length)
    {
        checkState(arrayClass.isArray(), "ArrayClass %s must arrayClass.isArray()");
        if (((Object) arrayClass == Object[].class)) {
            return (T) new Object[length];
        }

        Class<?> aClass = arrayClass.getComponentType();
        return (T) createPrimitiveArray(aClass, length);
    }

    /**
     * java list[T] to array[T]
     * arrayClass do not support Primitive int[].class ...
     *
     * @param list       java list
     * @param arrayClass demo: Integer[].class  String[].class
     * @param <T>        type
     * @return T[]
     * @see Arrays#createArray(Class, int)
     */
    public static <T> T[] toArray(List<T> list, Class<T[]> arrayClass)
    {
        if (((Object) arrayClass == Object[].class)) {
            return (T[]) list.toArray();
        }
        return list.toArray(createArrayByArrayClass(arrayClass, list.size()));
    }

    /**
     * java list[T] to array[T]
     *
     * @param list       java list
     * @param arrayClass demo: Integer[].class  String[].class
     * @param <T>        type
     * @return T[]
     * @see Arrays#createArray(Class, int)
     */
    public static <T> T toPrimitiveArray(List<?> list, Class<T> arrayClass)
    {
        checkState(arrayClass.isArray(), "ArrayClass %s must arrayClass.isArray()");
        if ((arrayClass == Object[].class)) {
            return (T) list.toArray();
        }

        Class<?> aClass = arrayClass.getComponentType();
        if (aClass == int.class) {
            return (T) list.stream().mapToInt(x -> (Integer) x).toArray();
        }
        else if (aClass == double.class) {
            return (T) list.stream().mapToDouble(x -> (Double) x).toArray();
        }
        else if (aClass == short.class) {
            short[] array = new short[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = (Short) list.get(i);
            }
            return (T) array;
        }
        else if (aClass == long.class) {
            return (T) list.stream().mapToLong(x -> (Long) x).toArray();
        }
        else if (aClass == float.class) {
            float[] array = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = (Float) list.get(i);
            }
            return (T) array;
        }
        else if (aClass == byte.class) {
            byte[] array = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = (Byte) list.get(i);
            }
            return (T) array;
        }
        else if (aClass == boolean.class) {
            boolean[] array = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = (Boolean) list.get(i);
            }
            return (T) array;
        }
        else if (aClass == char.class) {
            char[] array = new char[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = (Character) list.get(i);
            }
            return (T) array;
        }
        else {
            throw new UnsupportedOperationException("this " + aClass + " have't support!");
        }
    }

    public static <T> Class<T[]> getArrayClass(Class<T> aClass)
    {
        return (Class<T[]>) java.lang.reflect.Array.newInstance(aClass, 0).getClass();
    }

    /**
     * Returns a string representation of the given array. This method takes an Object
     * to allow also all types of primitive type arrays.
     *
     * @param array The array to create a string representation for.
     * @return The string representation of the array.
     * @throws IllegalArgumentException If the given object is no array.
     */
    public static String arrayToString(Object array)
    {
        requireNonNull(array);

        if (array instanceof int[]) {
            return java.util.Arrays.toString((int[]) array);
        }
        if (array instanceof long[]) {
            return java.util.Arrays.toString((long[]) array);
        }
        if (array instanceof Object[]) {
            return java.util.Arrays.toString((Object[]) array);
        }
        if (array instanceof byte[]) {
            return java.util.Arrays.toString((byte[]) array);
        }
        if (array instanceof double[]) {
            return java.util.Arrays.toString((double[]) array);
        }
        if (array instanceof float[]) {
            return java.util.Arrays.toString((float[]) array);
        }
        if (array instanceof boolean[]) {
            return java.util.Arrays.toString((boolean[]) array);
        }
        if (array instanceof char[]) {
            return java.util.Arrays.toString((char[]) array);
        }
        if (array instanceof short[]) {
            return java.util.Arrays.toString((short[]) array);
        }

        throw new IllegalArgumentException("The given argument is no array.");
    }

    /**
     * @param first firstValue
     * @param rest  array
     * @param type  array type
     * @param <T>   type
     * @return merge array
     */
    public static <T> T[] asArray(T first, T[] rest, Class<T> type)
    {
        T[] arr = createPrimitiveArray(type, rest.length + 1);
        arr[0] = first;
        System.arraycopy(rest, 0, arr, 1, rest.length);
        return arr;
    }

    /**
     * Returns a hash code based on the "deep contents" of the specified
     * array.
     * see: {@link java.util.Arrays#deepHashCode(Object[])}
     *
     * @param objects inputs
     * @return hashCode
     */
    public static int deepHashCode(Object... objects)
    {
        return java.util.Arrays.deepHashCode(objects);
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
        int length = Stream.of(arrays).mapToInt(x -> x.length).sum();
        T[] mergeArr = createArrayByArrayClass((Class<T[]>) arrays.getClass().getComponentType(), length);

        int index = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, mergeArr, index, arr.length);
            index += arr.length;
        }
        return mergeArr;
    }

    @SafeVarargs
    public static <T> T mergeByPrimitiveArray(T... arrays)
    {
        checkNotEmpty(arrays, "must arrays length > 0");
        int length = Stream.of(arrays).mapToInt(Array::getLength).sum();
        T mergeArr = createPrimitiveByArrayClass(arrays.getClass().getComponentType(), length);

        int index = 0;
        for (T arr : arrays) {
            System.arraycopy(arr, 0, mergeArr, index, Array.getLength(arr));
            index += Array.getLength(arr);
        }
        return mergeArr;
    }

    public static <T> boolean equals(T arr1, T arr2)
    {
        if (arr1 == null && arr2 == null) {
            return true;
        }
        else if (arr1 != null && arr2 != null) {
            checkArray(arr1);
            checkArray(arr2);
            int len = Array.getLength(arr1);
            if (len != Array.getLength(arr2)) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (!Objects.equals(Array.get(arr1, i), Array.get(arr2, i))) {
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    private static void checkArray(Object arr)
    {
        requireNonNull(arr, "arr is null");
        checkState(arr.getClass().isArray(), "not is array");
    }

    public static <T> void checkNotEmpty(T[] array, String format, Object... objects)
    {
        requireNonNull(array, "array is null");
        if (array.length == 0) {
            throw new IllegalStateException(String.format(format, objects));
        }
    }
}
