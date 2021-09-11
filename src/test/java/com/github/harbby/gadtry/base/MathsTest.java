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

public class MathsTest
{
    @Test
    public void isPowerOfTwoIntTest()
    {
        Assert.assertFalse(Maths.isPowerOfTwo(-1));
        Assert.assertFalse(Maths.isPowerOfTwo(7));
        Assert.assertTrue(Maths.isPowerOfTwo(8));
    }

    @Test
    public void IsPowerOfTwoLongTest()
    {
        Assert.assertFalse(Maths.isPowerOfTwo(-1L));
        Assert.assertFalse(Maths.isPowerOfTwo(7L));
        Assert.assertTrue(Maths.isPowerOfTwo(8L));
    }

    @Test
    public void remainderTest()
    {
        Assert.assertEquals(Maths.remainder(7, 2), 1);
        Assert.assertEquals(Maths.remainder(11, 4), 3);
        Assert.assertEquals(Maths.remainder(-11, 4), 1);
    }
}
