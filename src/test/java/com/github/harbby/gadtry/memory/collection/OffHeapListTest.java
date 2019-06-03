/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.memory.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class OffHeapListTest
{
    @Test
    public void getTestReturn001Give1()
    {
        List<String> list = new OffHeapList<>(String::getBytes, String::new);
        list.add("2019-06-01 harbby");
        list.add("001");

        Assert.assertEquals(list.get(1), "001");
        Assert.assertEquals(list, Arrays.asList("2019-06-01 harbby", "001"));
    }

    @Test
    public void set()
    {
        List<String> list = new OffHeapList<>(String::getBytes, String::new);
        list.add("2019-06-01 harbby");
        list.add("001");

        Assert.assertEquals(list.set(1, "001_up"), "001");
    }

    @Test
    public void remove()
    {
        List<String> list = new OffHeapList<>(String::getBytes, String::new);
        list.add("2019-06-01 harbby");
        list.add("001");

        Assert.assertEquals(list.remove(0), "2019-06-01 harbby");
    }

    @Test
    public void size()
    {
        List<String> list = new OffHeapList<>(String::getBytes, String::new);
        list.add("2019-06-01 harbby");
        list.add("001");

        Assert.assertEquals(list.size(), 2);
    }
}
