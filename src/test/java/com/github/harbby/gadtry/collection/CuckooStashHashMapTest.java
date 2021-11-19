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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

public class CuckooStashHashMapTest
{
    @Test
    public void baseTest()
    {
        CuckooStashHashMap<String, Integer> map = new CuckooStashHashMap<>();
        IntStream.range(0, 26).forEach(i -> {
            char c = (char) ('a' + i);
            map.put(new String(new char[] {c, c}), i + 1);
        });
        int size = map.size();
        Assert.assertEquals(size, 26);
        map.get("yy");
        IntStream.range(0, 26).forEach(i -> {
            char c = (char) ('a' + i);
            Integer v = map.get(new String(new char[] {c, c}));
            Assert.assertEquals(map.get(new String(new char[] {c, c})).intValue(), i + 1);
        });

        System.out.println(map);
    }

    @Test
    public void removeStashKeyTest()
    {
        CuckooStashHashMap<Integer, Integer> map = new CuckooStashHashMap<>(4);
//        Random random = new Random();
//        while (true) {
//            for (int i = 0; i < 3; i++) {
//                map.put(random.nextInt(100), 999);
//            }
//            map.clear();
//        }
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);

        map.remove(19);
        map.remove(46);
        map.remove(34);
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = java.util.ConcurrentModificationException.class)
    public void foreachEntryRemoveThrowConcurrentModificationException()
    {
        Map<Integer, Integer> map = new HashMap<>(4);  //new HashMap<>(4)
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            map.remove(entry.getKey());
        }
        System.out.println(map);
    }

    @Test
    public void foreachEntryRemoveSuccess()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void foreachKeysRemoveSuccess()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void foreachKeysRemoveFailed()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);

        try {
            Iterator<Integer> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                iterator.remove();
                Assert.fail();
            }
        }
        catch (IllegalStateException e) {
            Assert.assertEquals("please call `iterator.next()` first", e.getMessage());
        }
        Assert.assertEquals(map.size(), 2);

        try {
            Iterator<Integer> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                iterator.remove();
                Assert.fail();
            }
        }
        catch (IllegalStateException e) {
            Assert.assertEquals("please call `iterator.next()` first", e.getMessage());
        }
        Assert.assertEquals(map.size(), 2);
    }

    @Test(expected = java.util.ConcurrentModificationException.class)
    public void foreachKeysRemoveThrowConcurrentModificationException()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);

        for (Integer key : map.keySet()) {
            map.remove(key);
        }
    }

    @Test
    public void foreachValuesRemoveSuccess()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);
        Iterator<Integer> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = java.util.ConcurrentModificationException.class)
    public void foreachValuesRemoveThrowConcurrentModificationException()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 999);
        map.put(19, 999);
        map.put(46, 999);

        for (Integer value : map.values()) {
            map.remove(19);
            System.out.println(value);
        }
    }

    @Test
    public void foreachUpdateValuesSuccess()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 1);
        map.put(19, 2);
        map.put(46, 3);

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            entry.setValue(999);
        }
        Assert.assertEquals(ImmutableMap.of(34, 999, 19, 999, 46, 999), map);
    }

    @Test
    public void resizeSuccess()
    {
        Map<Integer, Integer> map = new CuckooStashHashMap<>(4);
        map.put(34, 1);
        map.put(19, 2);
        map.put(46, 3);
        map.put(99, 4);

        Assert.assertEquals(ImmutableMap.of(34, 1, 19, 2, 46, 3, 99, 4), map);
    }
}
