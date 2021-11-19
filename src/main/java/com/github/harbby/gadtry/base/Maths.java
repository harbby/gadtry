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
     * @param value      number
     * @param powerOfTwo 2^?
     * @return mod
     */
    public static int remainder(int value, int powerOfTwo)
    {
        return value & (powerOfTwo - 1);
    }

    public static int lastPowerOfTwo(int value)
    {
        return Integer.highestOneBit(value);
    }

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    public static int nextPowerOfTwo(int value)
    {
        if (value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        //return value + 1;
        return (value < 0) ? 1 : (value >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : value + 1;
    }

    private static final long C1 = 0xcc9e2d51;
    private static final long C2 = 0x1b873593;

    /**
     * see: google guava Hashing.smear(int)
     */
    public static int smearHashCode(int code)
    {
        return (int) (C2 * Integer.rotateLeft((int) (code * C1), 15));
    }
}
