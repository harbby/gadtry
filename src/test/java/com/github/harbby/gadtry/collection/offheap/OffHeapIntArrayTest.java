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
package com.github.harbby.gadtry.collection.offheap;

import com.github.harbby.gadtry.base.Platform;
import org.junit.Assert;
import org.junit.Test;

public class OffHeapIntArrayTest
{
    @Test
    public void putIntsTest()
    {
        int[] arr = new int[] {1, 2, 3, 4, 5};
        long intArr = Platform.allocateMemory(10 << 2);
        OffHeapIntArray.putInts(intArr, arr);
        Assert.assertArrayEquals(OffHeapIntArray.getInts(intArr, 5), arr);
        //--------------------
        int[] arr2 = new int[3];
        OffHeapIntArray.getInts(intArr, arr2, 2);
        Assert.assertArrayEquals(arr2, new int[] {1, 2, 0});
    }

    @Test
    public void putCountInts()
    {
        long intArr = Platform.allocateMemory(10 << 2);
        OffHeapIntArray.putInts(intArr, new int[] {1, 1, 1, 1, 1, 1});
        int[] arr = new int[] {1, 2, 3, 4, 5};
        OffHeapIntArray.putInts(intArr, arr, 3);
        Assert.assertArrayEquals(OffHeapIntArray.getInts(intArr, 5), new int[] {1, 2, 3, 1, 1});
    }
}
