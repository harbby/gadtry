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
package com.github.harbby.gadtry.aop.mock;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class MockGoArgumentTest
{
    @Test
    public void any()
    {
        Assert.assertNull(MockGoArgument.any());
    }

    @Test
    public void anyInt()
    {
        Assert.assertEquals(MockGoArgument.anyInt(), 0);
    }

    @Test
    public void anyByte()
    {
        Assert.assertEquals(MockGoArgument.anyByte(), 0);
    }

    @Test
    public void anyShort()
    {
        Assert.assertEquals(MockGoArgument.anyShort(), 0);
    }

    @Test
    public void anyLong()
    {
        Assert.assertEquals(MockGoArgument.anyLong(), 0L);
    }

    @Test
    public void anyDouble()
    {
        Assert.assertEquals(MockGoArgument.anyDouble(), 0.0d, 0.0d);
    }

    @Test
    public void anyChar()
    {
        Assert.assertEquals(MockGoArgument.anyChar(), 0);
    }

    @Test
    public void anyFloat()
    {
        Assert.assertEquals(MockGoArgument.anyFloat(), 0f, 0f);
    }

    @Test
    public void anyBoolean()
    {
        Assert.assertFalse(MockGoArgument.anyBoolean());
    }

    @Test
    public void anyString()
    {
        Assert.assertEquals(MockGoArgument.anyString(), "");
    }

    @Test
    public void anyMap()
    {
        Assert.assertEquals(MockGoArgument.anyMap(), Collections.emptyMap());
    }

    @Test
    public void anyList()
    {
        Assert.assertEquals(MockGoArgument.anyList(), Collections.emptyList());
    }

    @Test
    public void anySet()
    {
        Assert.assertEquals(MockGoArgument.anySet(), Collections.emptySet());
    }

    @Test
    public void anyIterator()
    {
        Assert.assertFalse(MockGoArgument.anyIterator().hasNext());
    }

    @Test
    public void anyIterable()
    {
        Assert.assertFalse(MockGoArgument.anyIterable().iterator().hasNext());
    }
}
