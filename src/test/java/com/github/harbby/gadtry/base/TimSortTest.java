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

import java.util.Arrays;
import java.util.Random;

// a\[[0-9a-z+ ]?
public class TimSortTest
{
    @Test
    public void sortTest()
    {
        Random random = new Random(0);
        for (int size : new int[] {1, 16, 50, 100}) {
            Integer[] intArr = random.ints(size, 0, 100).boxed().toArray(Integer[]::new);
            Integer[] copyArr = Arrays.copyOf(intArr, size);

            TimSort.sort(intArr, Integer::compareTo, new TimeSortDataFormat.SingleDataFormat<>());
            Arrays.sort(copyArr, Integer::compareTo);

            Assert.assertArrayEquals(intArr, copyArr);
        }
    }

    @Test
    public void sortTwoTest()
    {
        Random random = new Random(0);
        for (int size : new int[] {1, 16, 50, 100}) {
            int[] baseArr = random.ints(size, 0, 100).toArray();
            Object[] data = new Object[size * 2];
            for (int i = 0; i < size; i++) {
                data[i * 2] = baseArr[i];
                data[i * 2 + 1] = "int_" + baseArr[i];
            }

            TimSort.sort(data, Integer::compareTo, new TimeSortDataFormat.PairDataFormat<>());
            Arrays.sort(baseArr);
            // check
            for (int i = 0; i < size; i++) {
                int v = (int) data[i * 2];
                String str = (String) data[i * 2 + 1];
                Assert.assertEquals(baseArr[i], v);
                Assert.assertEquals(str, "int_" + v);
            }
        }
    }
}
