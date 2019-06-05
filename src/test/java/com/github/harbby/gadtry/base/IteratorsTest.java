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

import com.github.harbby.gadtry.collection.mutable.MutableList;
import com.github.harbby.gadtry.collection.mutable.MutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IteratorsTest
{
    @Test
    public void ofCreateIteratorTest()
    {
        String[] number = new String[] {"1", "2", "3"};

        Iterator<String> iterator = Iterators.of(number);
        List<String> list = MutableList.copy(() -> iterator);

        Assert.assertArrayEquals(number, list.toArray(new String[0]));
    }

    @Test
    public void createEmptyIteratorTest()
    {
        Assert.assertFalse(Iterators.empty().hasNext());
        try {
            Iterators.empty().next();
            Assert.fail();
        }
        catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void isEmptyTest()
    {
        Assert.assertTrue(Iterators.isEmpty(new ArrayList()));
        Assert.assertTrue(Iterators.isEmpty(Iterators.empty()));
        Assert.assertTrue(Iterators.isEmpty(Iterators::empty));

        Assert.assertFalse(Iterators.isEmpty(Iterators.of(1)));
    }

    @Test
    public void getLastTestByIterator()
    {
        List<String> list = Arrays.asList("1", "2", "3");
        String last = Iterators.getLast(list.iterator());
        Assert.assertEquals(list.get(list.size() - 1), last);

        try {
            Iterators.getLast(Iterators.empty());
            Assert.fail();
        }
        catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void getLastReturnDefault()
    {
        Assert.assertEquals(Iterators.getLast(() -> Iterators.empty(), "done"), "done");
        Assert.assertEquals(Iterators.getLast(new ArrayList<>(), "done"), "done");
        Assert.assertEquals("-1", Iterators.getLast(new ArrayList<>(), "-1"));
    }

    @Test
    public void getLastTestDefaultValueByIterator2()
    {
        Assert.assertEquals("123", Iterators.getLast(MutableSet.of("123"), "-1"));
        Assert.assertEquals("123", Iterators.getLast(MutableList.of("123"), "-1"));
        Assert.assertEquals("123", Iterators.getLast(MutableSet.of("123")));
        Assert.assertEquals("123", Iterators.getLast(MutableList.of("123")));
    }

    @Test
    public void TestIterableGetLastByList()
    {
        List<String> list = Arrays.asList("1", "2", "3");
        String last = Iterators.getLast(list);
        Assert.assertEquals(list.get(list.size() - 1), last);

        try {
            Iterators.getLast(new ArrayList<>());
            Assert.fail();
        }
        catch (NoSuchElementException e) {
        }
    }

    @Test
    public void TestIterableGetLastDefaultValueByList()
    {
        List<String> list = new ArrayList<>();
        String last = Iterators.getLast(list, "-1");
        Assert.assertEquals("-1", last);
    }

    @Test
    public void iteratorSizeTestReturn3()
    {
        List<String> list = Arrays.asList("1", "2", "3");
        Assert.assertEquals(3, Iterators.size(list.iterator()));
    }

    @Test
    public void iteratorMapTest()
    {
        List<String> list = MutableList.of("1", "2", "3");
        Iterable iterable = Iterators.map(list, x -> Integer.parseInt(x) + 1);
        Assert.assertEquals(Arrays.asList(2, 3, 4), MutableList.copy(iterable));

        Iterator limit = iterable.iterator();
        while (limit.hasNext()) {
            limit.next();
            limit.remove();
        }
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void iteratorMapperReduceTest()
    {
        List<String> list = Arrays.asList("1", "2", "3");
        int sum = Iterators.reduce(list.iterator(), x -> Integer.parseInt(x), (x, y) -> x + y);
        Assert.assertEquals(6, sum);
    }

    @Test
    public void limit()
    {
        List<String> list = Arrays.asList("1", "2", "3");
        List<String> limit = MutableList.copy(() -> Iterators.limit(list.iterator(), 1));
        Assert.assertEquals(Arrays.asList("1"), limit);
    }

    @Test(expected = NoSuchElementException.class)
    public void limitError()
    {
        try {
            Iterators.limit(Iterators.empty(), -1);
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "limit must >= 0");
        }

        Iterator<String> iterator = Iterators.limit(Iterators.empty(), 1);
        List<String> limit = MutableList.copy(() -> iterator);
        Assert.assertTrue(limit.isEmpty());
        iterator.next();
    }

    @Test
    public void foreach()
    {
        List<Integer> list = new ArrayList<>();
        Iterators.foreach(Arrays.asList(1, 2, 3).iterator(), num -> {
            list.add(num + 1);
        });

        Assert.assertEquals(list, Arrays.asList(2, 3, 4));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void flatMap()
    {
        Iterator<String> iterator = Iterators.flatMap(Iterators.of("abcd", "123"), s -> {
            char[] chars = s.toCharArray();
            String[] strings = new String[chars.length];
            for (int i = 0; i < chars.length; i++) {
                strings[i] = String.valueOf(chars[i]);
            }
            return strings;
        });
    }
}
