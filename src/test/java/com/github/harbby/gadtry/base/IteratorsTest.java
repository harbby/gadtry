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

import com.github.harbby.gadtry.collection.MutableList;
import com.github.harbby.gadtry.collection.MutableSet;
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
    public void getFirstByIteratorReturn2()
    {
        List<String> list = Arrays.asList("1", "2", "3");

        Assert.assertEquals(list.get(0), Iterators.getFirst(list.iterator(), 0));
        Assert.assertEquals(list.get(1), Iterators.getFirst(list.iterator(), 1));
        Assert.assertEquals(list.get(2), Iterators.getFirst(list.iterator(), 2));

        Assert.assertEquals("-1", Iterators.getFirst(Iterators.empty(), 999, "-1"));

        try {
            Iterators.getFirst(Iterators.empty(), 999);
            Assert.fail();
        }
        catch (NoSuchElementException ignored) {
        }

        try {
            Iterators.getFirst(Iterators.empty(), -1);
            Assert.fail();
        }
        catch (IllegalStateException ignored) {
        }
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
        List<Integer> list = Arrays.asList(1, 2, 3);
        int sum = Iterators.reduce(list.iterator(), (x, y) -> x + y).get();
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
        catch (IllegalArgumentException e) {
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

    @Test
    public void flatMapTest()
    {
        Iterator<String> iterator = Iterators.flatMap(Iterators.of("abcd", "", "123"), s -> {
            char[] chars = s.toCharArray();
            List<String> strings = new ArrayList<>(chars.length);
            for (int i = 0; i < chars.length; i++) {
                strings.add(String.valueOf(chars[i]));
            }
            return strings.iterator();
        });

        Assert.assertEquals(Iterators.size(iterator), 7);
    }

    @Test
    public void mergeSorted1()
    {
        List<Integer> list1 = Arrays.asList(1, 5, 7, 9, 13, 27);
        List<Integer> list2 = Arrays.asList(0, 4, 8, 16, 20);
        List<Integer> list3 = Arrays.asList(3, 8, 11, 15, 22);

        Iterator<Integer> iterator = Iterators.mergeSorted(Arrays.asList(list1.iterator(),
                list2.iterator(),
                list3.iterator()),
                Integer::compareTo);
        List<Integer> out = MutableList.copy(iterator);
        Assert.assertEquals(out, Arrays.asList(0, 1, 3, 4, 5, 7, 8, 8, 9, 11, 13, 15, 16, 20, 22, 27));
    }

    @Test
    public void mergeSorted2()
    {
        List<Integer> list1 = Arrays.asList(27, 13, 9, 7, 5, 1);
        List<Integer> list2 = Arrays.asList(20, 16, 8, 4, 0);
        List<Integer> list3 = Arrays.asList(22, 15, 11, 8, 3);

        Iterator<Integer> iterator = Iterators.mergeSorted(
                Arrays.asList(list1.iterator(),
                        list2.iterator(),
                        list3.iterator()),
                (x, y) -> Integer.compare(y, x));

        List<Integer> out = MutableList.copy(iterator);
        Assert.assertEquals(out, Arrays.asList(27, 22, 20, 16, 15, 13, 11, 9, 8, 8, 7, 5, 4, 3, 1, 0));
    }

    @Test
    public void filterTest()
    {
        List<Integer> list1 = Arrays.asList(1, 2, 3, null, 5);
        Iterator<Integer> iterator = Iterators.filter(list1.iterator(), x -> x == null || x > 2);
        List<Integer> out = MutableList.copy(iterator);
        System.out.println(out);
        Assert.assertEquals(out, Arrays.asList(3, null, 5));
    }

    @Test
    public void sampleTest()
    {
        List<Integer> list1 = Arrays.asList(1, 2, 3, null, 5, 6);
        Iterator<Integer> iterator = Iterators.sample(list1.iterator(), 3, 6, 12345);
        List<Integer> out = MutableList.copy(iterator);
        System.out.println(out);
        Assert.assertEquals(out, Arrays.asList(1, null, 5));
    }
}
