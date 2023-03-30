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

import com.github.harbby.gadtry.base.TimSort;
import com.github.harbby.gadtry.base.TimeSortDataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class AppendOnlyMapTest
{
    @Test
    public void compressTest()
    {
        AppendOnlyMap<Integer, Integer> appendOnlyMap = new AppendOnlyMap<>(Integer::sum, 1000);
        Random random = new Random(0);
        int[] intArr = random.ints(50, 0, 100).toArray();
        Comparator<Integer> comparator = (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            else if (o1 == null) {
                return -1;
            }
            else if (o2 == null) {
                return 1;
            }
            else {
                return o1 - o2;
            }
        };

        Map<Integer, Integer> treeMap = new TreeMap<>(comparator);
        for (int i : intArr) {
            appendOnlyMap.append(i, 1);
            treeMap.merge(i, 1, Integer::sum);
        }
        // add null key
        appendOnlyMap.append(null, 1);
        appendOnlyMap.append(null, 1);
        treeMap.merge(null, 1, Integer::sum);
        treeMap.merge(null, 1, Integer::sum);

        Assertions.assertEquals(appendOnlyMap.size(), treeMap.size());

        Object[] objects = appendOnlyMap.compress();

        TimSort.sort(objects, 0, appendOnlyMap.size(), comparator, new TimeSortDataFormat.PairDataFormat<>());
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : treeMap.entrySet()) {
            Assertions.assertEquals(entry.getKey(), objects[i]);
            Assertions.assertEquals(entry.getValue(), objects[i + 1]);
            i += 2;
        }
    }
}
