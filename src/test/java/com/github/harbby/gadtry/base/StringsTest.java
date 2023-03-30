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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringsTest
{
    @Test
    public void isNotBlank()
    {
        Assertions.assertFalse(Strings.isNotBlank(""));
        Assertions.assertFalse(Strings.isNotBlank(null));
        Assertions.assertFalse(Strings.isNotBlank("   "));

        Assertions.assertTrue(Strings.isNotBlank(" dwa "));
    }

    @Test
    public void isBlank()
    {
        Assertions.assertTrue(Strings.isBlank(""));
        Assertions.assertTrue(Strings.isBlank(null));
        Assertions.assertTrue(Strings.isBlank("   "));
    }

    @Test
    public void isAsciiTest()
    {
        Assertions.assertTrue(Strings.isAscii("info"));
        Assertions.assertTrue(Strings.isAscii("Info"));
        Assertions.assertTrue(Strings.isAscii("123info.!#@&123Abc"));
        Assertions.assertFalse(Strings.isAscii("hello!,你好"));
    }
}
