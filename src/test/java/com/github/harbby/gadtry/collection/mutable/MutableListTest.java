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
import com.github.harbby.gadtry.collection.MutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MutableListTest
{
    @Test
    public void asList()
    {
        List<Integer> create = MutableList.asList(1, new Integer[] {1, 2, 3});
        Assert.assertEquals(create, Arrays.asList(1, 1, 2, 3));
    }

    @Test
    public void copy()
    {
        List<Integer> create = MutableList.copy(() -> Iterators.of(1, 2, 3));
        Assert.assertEquals(create, Arrays.asList(1, 2, 3));
    }

    @Test
    public void of()
    {
        List<Integer> create = MutableList.of(1, 2, 3);
        Assert.assertEquals(create, Arrays.asList(1, 2, 3));
    }

    @Test
    public void builder()
    {
        List<Integer> check = Arrays.asList(1, 2, 3, 4, 5);

        List<Integer> create = MutableList.<Integer>builder()
                .add(1)
                .addAll(new Integer[] {2, 3})
                .addAll(Iterators.of(4))
                .addAll(Arrays.asList(5))
                .build();
        Assert.assertEquals(create, check);
    }
}
