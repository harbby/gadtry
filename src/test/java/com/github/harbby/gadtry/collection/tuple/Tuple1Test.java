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
package com.github.harbby.gadtry.collection.tuple;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Tuple1Test
{
    private Tuple1<String> tuple;

    @Before
    public void init()
    {
        tuple = Tuple1.of("1");
    }

    @Test
    public void set()
    {
        Assert.assertEquals(tuple.get(), "1");
        tuple.set("2");
        Assert.assertEquals(tuple.f1(), "2");
    }

    @Test
    public void toString1()
    {
        Assert.assertEquals(tuple.toString(), "(1)");
    }

    @Test
    public void getArity()
    {
        Assert.assertEquals(tuple.getArity(), 1);
    }

    @Test
    public void getField()
    {
        Assert.assertEquals(tuple.getField(1), "1");
        try {
            Assert.assertEquals(tuple.getField(tuple.getArity() + 1), "1");
            Assert.fail();
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(e.getMessage(), tuple.getArity() + 1 + "");
        }
    }

    @Test
    public void copy()
    {
        Assert.assertEquals(tuple, tuple.copy());
    }

    @Test
    public void hashCodeTest()
    {
        tuple.hashCode();
    }
}
