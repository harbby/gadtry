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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(list, out);
        Assertions.assertEquals(out, Arrays.asList(1, 2, 3));
    }

    @Test
    public void ofTest()
            throws Exception
    {
        for (int i = 0; i <= 8; i++) {
            Class<?>[] types = IntStream.range(0, i).mapToObj(x -> Object.class).toArray(Class[]::new);
            Object[] strings = IntStream.range(0, i).mapToObj(x -> "str" + x).toArray(String[]::new);
            List<String> list = (List<String>) ImmutableList.class.getMethod("of", types).invoke(null, strings);
            Assertions.assertEquals(list, IntStream.range(0, i).mapToObj(x -> "str" + x).collect(Collectors.toList()));
        }
    }

    @Test
    public void warpTest()
    {
        Assertions.assertEquals(ImmutableList.wrap(new String[] {"a"}), Arrays.asList("a"));
        Assertions.assertEquals(ImmutableList.wrap(new String[0]), Collections.emptyList());
    }

    @Test
    public void subListTest()
    {
        List<Integer> list1 = ImmutableList.of(1, 2, 3);
        Assertions.assertEquals(Arrays.asList(1, 2, 3).listIterator(3).previous().intValue(), 3);
        Assertions.assertEquals(list1.listIterator(3).previous().intValue(), 3);

        List<Integer> list = ImmutableList.of(1, 2, 3, 4, 5);
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4, 5).subList(1, 4), Arrays.asList(2, 3, 4));
        Assertions.assertEquals(list.subList(1, 4), Arrays.asList(2, 3, 4));

        try {
            list.sort(Integer::compareTo);
            Assertions.fail();
        }
        catch (UnsupportedOperationException ignored) {
        }

        //other check
        try {
            list.listIterator(-1);
            Assertions.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.listIterator(list.size() + 1);
            Assertions.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.subList(-1, list.size());
            Assertions.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.subList(0, list.size() + 1);
            Assertions.fail();
        }
        catch (IndexOutOfBoundsException ignored) {
        }

        try {
            list.subList(3, 1);
            Assertions.fail();
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

        Assertions.assertEquals(0, iterator.nextIndex());
        Assertions.assertEquals(1, iterator.next().intValue());

        Assertions.assertEquals(1, iterator.nextIndex());
        Assertions.assertEquals(2, iterator.next().intValue());

        Assertions.assertEquals(2, iterator.nextIndex());
        Assertions.assertEquals(3, iterator.next().intValue());

        Try.of(iterator::nextIndex).onSuccess(Assertions::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        Try.of(iterator::next).onSuccess(Assertions::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        //--------
        Assertions.assertEquals(2, iterator.previousIndex());
        Assertions.assertEquals(3, iterator.previous().intValue());
        Assertions.assertEquals(2, iterator.previous().intValue());
        Assertions.assertEquals(1, iterator.previous().intValue());
        Try.of(iterator::previousIndex).onSuccess(Assertions::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        Try.of(iterator::previous).onSuccess(Assertions::fail).matchException(NoSuchElementException.class, e -> {}).doTry();
        //---error check

        Try.of(() -> iterator.add(-1)).onSuccess(Assertions::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
        Try.of(() -> iterator.set(-1)).onSuccess(Assertions::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
        Try.of(iterator::remove).onSuccess(Assertions::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
    }
}
