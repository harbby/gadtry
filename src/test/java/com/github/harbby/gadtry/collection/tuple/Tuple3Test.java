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
package com.github.harbby.gadtry.collection.tuple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Tuple3Test
{
    private final Tuple3<String, Integer, Boolean> tuple = Tuple3.of("k1", 1, true);

    @Test
    public void of()
    {
        Assertions.assertEquals(tuple.f1(), "k1");
        Assertions.assertEquals(tuple.f2().intValue(), 1);
        Assertions.assertEquals(tuple.f3(), true);
    }

    @Test
    public void hashCodeTest()
    {
        tuple.hashCode();
    }

    @Test
    public void toString1()
    {
        Assertions.assertEquals(tuple.toString(), "(k1, 1, true)");
    }

    @Test
    public void getArity()
    {
        Assertions.assertEquals(tuple.getArity(), 3);
    }

    @Test
    public void getField()
    {
        Assertions.assertEquals(tuple.getField(1), "k1");
        Assertions.assertEquals(tuple.<Integer>getField(2).intValue(), 1);
        Assertions.assertEquals(tuple.<Boolean>getField(3), true);

        try {
            Assertions.assertEquals(tuple.getField(tuple.getArity() + 1), "1");
            Assertions.fail();
        }
        catch (IndexOutOfBoundsException e) {
            Assertions.assertEquals(e.getMessage(), tuple.getArity() + 1 + "");
        }
    }

    @Test
    public void copy()
    {
        Assertions.assertEquals(tuple, tuple.copy());
    }
}
