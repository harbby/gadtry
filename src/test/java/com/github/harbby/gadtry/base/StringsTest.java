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

import org.junit.Assert;
import org.junit.Test;

public class StringsTest
{
    @Test
    public void isNotBlank()
    {
        Assert.assertFalse(Strings.isNotBlank(""));
        Assert.assertFalse(Strings.isNotBlank(null));
        Assert.assertFalse(Strings.isNotBlank("   "));

        Assert.assertTrue(Strings.isNotBlank(" dwa "));
    }

    @Test
    public void isBlank()
    {
        Assert.assertTrue(Strings.isBlank(""));
        Assert.assertTrue(Strings.isBlank(null));
        Assert.assertTrue(Strings.isBlank("   "));
    }

    @Test
    public void isAsciiTest()
    {
        Assert.assertTrue(Strings.isAscii("info"));
        Assert.assertTrue(Strings.isAscii("Info"));
        Assert.assertTrue(Strings.isAscii("123info.!#@&123Abc"));
        Assert.assertFalse(Strings.isAscii("hello!,你好"));
    }
}
