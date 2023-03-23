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

import java.util.Arrays;

/**
 * TimeSortDataFormat is a generic interface used to define data formats and operations in sorting algorithms.
 * It contains several methods for array operations.
 * @param <K> Type of elements in the array
 * @param <ARRAY> Array type
 */
public interface TimeSortDataFormat<K, ARRAY>
{
    /**
     * Returns the length of the array.
     * @param arr Array whose length is to be returned
     * @return Length of the array
     */
    int getLength(ARRAY arr);

    /**
     * Creates an array of specified length.
     * @param length Length of the array
     * @return Created array
     */
    ARRAY createArray(int length);

    /**
     * Returns the element at specified position.
     * @param arr Array from which element is to be returned
     * @param i Position of the element
     * @return Element at the specified position
     */
    K get(ARRAY arr, int i);

    /**
     * Copies a portion of the source array to the destination array.
     * @param src Source array
     * @param srcPos Starting position in the source array
     * @param dest Destination array
     * @param destPos Starting position in the destination data
     */
    void copyTo(ARRAY src, int srcPos, ARRAY dest, int destPos);

    /**
     * Array copy operation.
     * @param src Source array
     * @param srcPos Starting position in the source array
     * @param dest Destination array
     * @param destPos Starting position in the destination array
     * @param len Number of elements to be copied
     */
    void arrayCopy(ARRAY src, int srcPos, ARRAY dest, int destPos, int len);

    /**
     * Swaps two elements in the array.
     * @param arr Array containing the elements to be swapped
     * @param i1 Position of the first element to be swapped
     * @param i2 Position of the second element to be swapped
     */
    void swap(ARRAY arr, int i1, int i2);

    void clearArray(ARRAY tmp);

    static final class SingleDataFormat<K, T>
            implements TimeSortDataFormat<K, T[]>
    {
        @Override
        public int getLength(T[] arr)
        {
            return arr.length;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T[] createArray(int length)
        {
            return (T[]) new Object[length];
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public K get(T[] arr, int i)
        {
            return (K) arr[i];
        }

        @Override
        public void copyTo(T[] src, int srcPos, T[] dest, int destPos)
        {
            dest[destPos] = src[srcPos];
        }

        @Override
        public void arrayCopy(T[] src, int srcPos, T[] dest, int destPos, int len)
        {
            System.arraycopy(src, srcPos, dest, destPos, len);
        }

        @Override
        public void swap(T[] arr, int i1, int i2)
        {
            T temp = arr[i1];
            arr[i1] = arr[i2];
            arr[i2] = temp;
        }

        @Override
        public void clearArray(T[] tmp)
        {
            Arrays.fill(tmp, null);
        }
    }

    static final class PairDataFormat<K, T>
            implements TimeSortDataFormat<K, T[]>
    {
        @Override
        public int getLength(T[] arr)
        {
            return arr.length >> 1;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T[] createArray(int length)
        {
            return (T[]) new Object[length << 1];
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public K get(T[] arr, int i)
        {
            return (K) arr[i << 1];
        }

        @Override
        public void copyTo(Object[] src, int srcPos, Object[] dest, int destPos)
        {
            srcPos = srcPos << 1;
            destPos = destPos << 1;
            dest[destPos] = src[srcPos];
            dest[destPos + 1] = src[srcPos + 1];
        }

        @Override
        public void arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int len)
        {
            System.arraycopy(src, srcPos << 1, dest, destPos << 1, len << 1);
        }

        @Override
        public void swap(Object[] arr, int i1, int i2)
        {
            i1 = i1 << 1;
            i2 = i2 << 1;
            Object temp = arr[i1];
            arr[i1] = arr[i2];
            arr[i2] = temp;
            i1++;
            i2++;
            temp = arr[i1];
            arr[i1] = arr[i2];
            arr[i2] = temp;
        }

        @Override
        public void clearArray(T[] tmp)
        {
            Arrays.fill(tmp, null);
        }
    }
}
