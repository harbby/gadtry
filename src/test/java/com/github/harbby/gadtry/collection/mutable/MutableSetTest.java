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
package com.github.harbby.gadtry.collection.mutable;

import com.github.harbby.gadtry.base.Iterators;
import com.github.harbby.gadtry.collection.MutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MutableSetTest
{
    @Test
    public void copyCreateTest()
    {
        Set<String> check = new HashSet<>();
        check.add("1");
        check.add("2");
        check.add("3");

        Set<String> create = MutableSet.copy(check);
        Assert.assertEquals(create, check);
    }

    @Test
    public void of()
    {
        Set<String> check = new HashSet<>();
        check.add("1");
        check.add("2");
        check.add("3");

        Set<String> create = MutableSet.of("1", "2", "3");
        Assert.assertEquals(create, check);
    }

    @Test
    public void builder()
    {
        Set<String> check = new HashSet<>();
        check.add("1");
        check.add("2");
        check.add("3");
        check.add("4");
        check.add("5");

        Set<String> create = MutableSet.<String>builder()
                .add("1")
                .addAll("2", "3")
                .addAll(Iterators.of("4"))
                .addAll(Arrays.asList("5"))
                .build();
        Assert.assertEquals(create, check);
    }
}
