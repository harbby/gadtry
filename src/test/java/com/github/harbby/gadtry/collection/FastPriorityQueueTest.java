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

public class FastPriorityQueueTest
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
        FastPriorityQueue.PairDataFormat<Integer, String> dataFormat = new FastPriorityQueue.PairDataFormat<>(heap, intArr.length, Integer::compare);
        FastPriorityQueue queue = new FastPriorityQueue(dataFormat);
        Arrays.sort(intArr);
        intArr[0] = -248;
        Arrays.sort(intArr);

        dataFormat.replaceHead(-248, "data_" + (-248), queue);
        for (int i = 0; i < intArr.length; i++) {
            int v = dataFormat.getHead();
            String pair = dataFormat.getHeadValue();
            Assert.assertEquals(intArr[i], v);
            Assert.assertEquals(pair, "data_" + v);
            queue.removeHead();
            Assert.assertEquals(queue.size(), (int) (heap.length / 2 - (i + 1)));
        }
    }

    @Test
    public void addTest()
    {
        Random random = new Random(0);
        int[] intArr = random.ints(50, 0, 100).toArray();

        Object[] heap = new Object[intArr.length * 2];
        FastPriorityQueue.PairDataFormat<Integer, Integer> dataFormat = new FastPriorityQueue.PairDataFormat<>(heap, 0, Integer::compare);
        FastPriorityQueue queue = new FastPriorityQueue(dataFormat);
        for (int j : intArr) {
            dataFormat.add(j, j & 1, queue);
        }
        Assert.assertEquals(queue.size(), intArr.length);
        Arrays.sort(intArr);
        for (int i = 0; i < intArr.length; i++) {
            int v = dataFormat.getHead();
            int pair = dataFormat.getHeadValue();
            Assert.assertEquals(intArr[i], v);
            Assert.assertEquals(pair, v & 1);
            queue.removeHead();
            Assert.assertEquals(queue.size(), (int) (heap.length / 2 - (i + 1)));
        }
    }

    @Test
    public void primitiveTypeTest()
    {
        Random random = new Random(0);
        int[] intArr = random.ints(50, 0, 100).toArray();

        final int[] heap = new int[intArr.length];
        System.arraycopy(intArr, 0, heap, 0, intArr.length / 2);

        FastPriorityQueue.DataFormat<Integer> dataFormat = new FastPriorityQueue.DataFormat<Integer>(heap.length / 2)
        {
            @Override
            public void swap(int i1, int i2)
            {
                int temp = heap[i1];
                heap[i1] = heap[i2];
                heap[i2] = temp;
            }

            @Override
            public int compare(int i1, int i2)
            {
                return heap[i1] - heap[i2];
            }

            @Override
            public Integer getHead()
            {
                return heap[0];
            }
        };
        FastPriorityQueue queue = new FastPriorityQueue(dataFormat, false);
        // add next 1/2
        for (int i = intArr.length / 2; i < intArr.length; i++) {
            heap[dataFormat.size++] = intArr[i];
            queue.siftUp(dataFormat.size - 1);
        }
        // check
        Arrays.sort(intArr);
        for (int i = 0; i < intArr.length; i++) {
            int v = dataFormat.getHead();
            Assert.assertEquals(intArr[i], v);
            queue.removeHead();
            Assert.assertEquals(queue.size(), (int) (heap.length - (i + 1)));
        }
    }
}
