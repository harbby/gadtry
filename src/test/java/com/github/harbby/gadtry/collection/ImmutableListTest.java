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

import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.base.Try;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImmutableListTest
{
    @Test
    public void serializableTest()
            throws IOException, ClassNotFoundException
    {
        List<Integer> list = ImmutableList.of(1, 2, 3);
        byte[] bytes = Serializables.serialize((Serializable) list);
        List<Integer> out = Serializables.byteToObject(bytes);
        Assert.assertEquals(list, out);
        Assert.assertEquals(out, Arrays.asList(1, 2, 3));
    }

    @Test
    public void ofTest()
            throws Exception
    {
        for (int i = 0; i <= 8; i++) {
            Class<?>[] types = IntStream.range(0, i).mapToObj(x -> Object.class).toArray(Class[]::new);
            Object[] strings = IntStream.range(0, i).mapToObj(x -> "str" + x).toArray(String[]::new);
            List<String> list = (List<String>) ImmutableList.class.getMethod("of", types).invoke(null, strings);
            Assert.assertEquals(list, IntStream.range(0, i).mapToObj(x -> "str" + x).collect(Collectors.toList()));
        }
    }

    @Test
    public void warpTest()
    {
        Assert.assertEquals(ImmutableList.wrap(new String[] {"a"}), Arrays.asList("a"));
        Assert.assertEquals(ImmutableList.wrap(new String[0]), Collections.emptyList());
    }

    @Test
    public void subListTest()
    {
        List<Integer> list1 = ImmutableList.of(1, 2, 3);
        Assert.assertEquals(Arrays.asList(1, 2, 3).listIterator(3).previous().intValue(), 3);
        Assert.assertEquals(list1.listIterator(3).previous().intValue(), 3);

        List<Integer> list = ImmutableList.of(1, 2, 3, 4, 5);
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5).subList(1, 4), Arrays.asList(2, 3, 4));
        Assert.assertEquals(list.subList(1, 4), Arrays.asList(2, 3, 4));

        try {
            list.sort(Integer::compareTo);
            Assert.fail();
        }
        catch (UnsupportedOperationException ignored) {
        }

        //other check
        try {
            list.listIterator(-1);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.listIterator(list.size() + 1);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.subList(-1, list.size());
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.subList(0, list.size() + 1);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.subList(3, 1);
            Assert.fail();
        }
        catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void listIteratorTest()
    {
        List<Integer> list = ImmutableList.of(1, 2, 3);
        ListIterator<Integer> iterator = list.listIterator();
        List<Integer> rs = new ArrayList<>();

        Assert.assertEquals(0, iterator.nextIndex());
        Assert.assertEquals(1, iterator.next().intValue());

        Assert.assertEquals(1, iterator.nextIndex());
        Assert.assertEquals(2, iterator.next().intValue());

        Assert.assertEquals(2, iterator.nextIndex());
        Assert.assertEquals(3, iterator.next().intValue());

        Try.of(iterator::nextIndex).onSuccess(Assert::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        Try.of(iterator::next).onSuccess(Assert::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        //--------
        Assert.assertEquals(2, iterator.previousIndex());
        Assert.assertEquals(3, iterator.previous().intValue());
        Assert.assertEquals(2, iterator.previous().intValue());
        Assert.assertEquals(1, iterator.previous().intValue());
        Try.of(iterator::previousIndex).onSuccess(Assert::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        Try.of(iterator::previous).onSuccess(Assert::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        //---error check

        Try.of(() -> iterator.add(-1)).onSuccess(Assert::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
        Try.of(() -> iterator.set(-1)).onSuccess(Assert::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
        Try.of(iterator::remove).onSuccess(Assert::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
    }
}
