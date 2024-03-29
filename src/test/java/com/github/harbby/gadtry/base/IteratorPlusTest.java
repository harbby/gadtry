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

import com.github.harbby.gadtry.collection.tuple.Tuple1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class IteratorPlusTest
{
    @Test
    public void allTest()
    {
        Iterator<String> source = Iterators.of("1,2,3,4,5");
        Tuple1<Boolean> closed = Tuple1.of(false);
        Assertions.assertTrue(source.hasNext());
        int rs = Iterators.flatMap(source, x -> Iterators.of(x.split(",")))
                .map(Integer::parseInt)
                .filter(x -> x < 5)
                .limit(3)
                .autoClose(() -> closed.set(true))
                .reduce(Integer::sum)
                .get();
        Assertions.assertEquals(rs, 6);
        Assertions.assertTrue(closed.f1);
    }

    @Test
    public void sizeTest()
    {
        Assertions.assertEquals(Iterators.of(1, 2, 3).size(), 3);
    }
}
