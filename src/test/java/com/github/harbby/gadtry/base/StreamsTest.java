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

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.IntStream;

import static com.github.harbby.gadtry.base.Streams.range;

public class StreamsTest
{
    @Test
    public void testRange10()
    {
        int[] array = range(10).toArray();

        Assertions.assertArrayEquals(array, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Test
    public void testRange1To5()
    {
        int[] array = range(1, 5).toArray();
        Assertions.assertArrayEquals(array, new int[] {1, 2, 3, 4});
    }

    @Test
    public void range0To30Step5()
    {
        int[] array = range(0, 30, 5).toArray();
        Assertions.assertArrayEquals(array, new int[] {0, 5, 10, 15, 20, 25});
    }

    @Test
    public void range0To10Step3()
    {
        int[] array = range(0, 10, 3).toArray();
        Assertions.assertArrayEquals(array, new int[] {0, 3, 6, 9});
    }

    @Test
    public void range0To_10Step_1()
    {
        int[] array = range(0, -10, -1).toArray();
        Assertions.assertArrayEquals(array, new int[] {0, -1, -2, -3, -4, -5, -6, -7, -8, -9});
    }

    @Test
    public void range1To0ReturnEmp()
    {
        int[] array = range(1, 0).toArray();
        Assertions.assertArrayEquals(array, new int[0]);
    }

    @Test
    public void range0ReturnEmp()
    {
        int[] array = range(0).toArray();
        Assertions.assertArrayEquals(array, new int[0]);
    }

    @Test
    public void parallelRange10()
    {
        for (int i = 0; i < 1; i++) {
            Queue<Integer> list = new ArrayBlockingQueue<>(10);
            IntStream stream = null;
            //stream = IntStream.range(0, 10);
            //stream = IntStream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            stream = range(10);
            stream.parallel().forEach(x -> {
                list.offer(x);   //如果使用ArrayList线程不安全 可以观察到null值, 数据缺少, 或者越界错误 三种情况
            });
            System.out.println(list);
            Assertions.assertArrayEquals(list.stream().sorted().mapToInt(x -> x).toArray(), new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        }
    }

    @Test
    public void parallelRange1To5()
    {
        int[] array = range(1, 5).parallel().toArray();
        Assertions.assertArrayEquals(array, new int[] {1, 2, 3, 4});
    }
}
