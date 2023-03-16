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
package com.github.harbby.gadtry.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class PairPriorityQueueTest
{
    @Test
    public void initSortTest()
    {
        Random random = new Random(0);
        int[] intArr = random.ints(50, 0, 100).toArray();
        Object[] heap = new Object[intArr.length * 2];
        for (int i = 0; i < intArr.length; i++) {
            heap[i * 2] = intArr[i];
            heap[i * 2 + 1] = "data_" + intArr[i];
        }
        PairPriorityQueue<Integer, String> queue = new PairPriorityQueue<>(heap, heap.length, Integer::compare);
        Arrays.sort(intArr);
        for (int i = 0; i < intArr.length; i++) {
            int v = queue.getHeapKey();
            String pair = queue.getHeapValue();
            Assert.assertEquals(intArr[i], v);
            Assert.assertEquals(pair, "data_" + v);
            queue.removeHead();
            Assert.assertEquals(queue.size(), (int) (heap.length - (i + 1) * 2));
        }
    }

    @Test
    public void addTest()
    {
        Random random = new Random(0);
        int[] intArr = random.ints(50, 0, 100).toArray();

        Object[] heap = new Object[intArr.length * 2];
        PairPriorityQueue<Integer, Integer> queue = new PairPriorityQueue<>(heap, 0, Integer::compare);
        for (int j : intArr) {
            queue.add(j, j & 1);
        }
        Assert.assertEquals(queue.size(), heap.length);
        Arrays.sort(intArr);
        for (int i = 0; i < intArr.length; i++) {
            int v = queue.getHeapKey();
            int pair = queue.getHeapValue();
            Assert.assertEquals(intArr[i], v);
            Assert.assertEquals(pair, v & 1);
            queue.removeHead();
            Assert.assertEquals(queue.size(), (int) (heap.length - (i + 1) * 2));
        }
    }
}
