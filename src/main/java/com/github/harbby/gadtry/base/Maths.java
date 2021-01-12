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

public final class Maths
{
    private Maths() {}

    public static boolean isPowerOfTwo(int value)
    {
        return value > 0 && ((value & (value - 1)) == 0);
    }

    /**
     * Returns true if the value is power of two.
     *
     * @param value input number
     * @return bool
     */
    public static boolean isPowerOfTwo(long value)
    {
        return value > 0 && ((value & (value - 1)) == 0);
    }

    /**
     * mod/remainder
     *
     * @param value number
     * @param powerOfTwo 2^?
     * @return mod
     */
    public static int remainder(int value, int powerOfTwo)
    {
        return value & (powerOfTwo - 1);
    }
}
