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
package com.github.harbby.gadtry.jvm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.function.UnaryOperator;

/**
 * java8引入了类型关联（TypeIntersection）
 * <p>
 * TypeIntersection
 */
public class ComparatorTest
{
    public static UnaryOperator<Integer> makeComparator()
    {
        UnaryOperator<Integer> func = (UnaryOperator<Integer> & Serializable) (a) -> a + 1;

        return func;
    }

    @Test
    public void Java8TypeIntersectionTest()
    {
        UnaryOperator<Integer> func = makeComparator();
        Assertions.assertEquals(func.apply(1).intValue(), 2);
        Assertions.assertTrue(func instanceof Serializable);
    }
}
