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

import static com.github.harbby.gadtry.base.JavaTypes.getPrimitiveClassInitValue;

public class MockitoArgument
{
    private MockitoArgument() {}

    public static <T> T any()
    {
        return null;
    }

    public static int anyInt()
    {
        return getPrimitiveClassInitValue(int.class);
    }

    public static byte anyByte()
    {
        return getPrimitiveClassInitValue(byte.class);
    }

    public static short anyShort()
    {
        return getPrimitiveClassInitValue(short.class);
    }

    public static long anyLong()
    {
        return getPrimitiveClassInitValue(long.class);
    }

    public static double anyDouble()
    {
        return getPrimitiveClassInitValue(double.class);
    }

    public static char anyChar()
    {
        return getPrimitiveClassInitValue(char.class);
    }

    public static float anyFloat()
    {
        return getPrimitiveClassInitValue(float.class);
    }

    public static boolean anyBoolean()
    {
        return getPrimitiveClassInitValue(boolean.class);
    }
}
