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
package com.github.harbby.gadtry.base;

import java.util.Arrays;

public class Strings
{
    private Strings() {}

    public static boolean isNotBlank(final CharSequence cs)
    {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs)
    {
        if (cs == null) {
            return true;
        }
        int strLen = cs.length();
        if (strLen == 0) {
            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a string representation of the given array. This method takes an Object
     * to allow also all types of primitive type arrays.
     *
     * @param array The array to create a string representation for.
     * @return The string representation of the array.
     * @throws IllegalArgumentException If the given object is no array.
     */
    public static String arrayToString(Object array)
    {
        if (array == null) {
            throw new NullPointerException();
        }

        if (array instanceof int[]) {
            return Arrays.toString((int[]) array);
        }
        if (array instanceof long[]) {
            return Arrays.toString((long[]) array);
        }
        if (array instanceof Object[]) {
            return Arrays.toString((Object[]) array);
        }
        if (array instanceof byte[]) {
            return Arrays.toString((byte[]) array);
        }
        if (array instanceof double[]) {
            return Arrays.toString((double[]) array);
        }
        if (array instanceof float[]) {
            return Arrays.toString((float[]) array);
        }
        if (array instanceof boolean[]) {
            return Arrays.toString((boolean[]) array);
        }
        if (array instanceof char[]) {
            return Arrays.toString((char[]) array);
        }
        if (array instanceof short[]) {
            return Arrays.toString((short[]) array);
        }

        if (array.getClass().isArray()) {
            return "<unknown array type>";
        }
        else {
            throw new IllegalArgumentException("The given argument is no array.");
        }
    }

    /**
     * Returns a string representation of the given array. This method takes an Object
     * to allow also all types of primitive type arrays.
     *
     * @param array The array to create a string representation for.
     * @return The string representation of the array.
     * @throws IllegalArgumentException If the given object is no array.
     */
    public static String arrayClass(Object array)
    {
        if (array == null) {
            throw new NullPointerException();
        }

        if (array instanceof int[]) {
            return Arrays.toString((int[]) array);
        }
        if (array instanceof long[]) {
            return Arrays.toString((long[]) array);
        }
        if (array instanceof Object[]) {
            return Arrays.toString((Object[]) array);
        }
        if (array instanceof byte[]) {
            return Arrays.toString((byte[]) array);
        }
        if (array instanceof double[]) {
            return Arrays.toString((double[]) array);
        }
        if (array instanceof float[]) {
            return Arrays.toString((float[]) array);
        }
        if (array instanceof boolean[]) {
            return Arrays.toString((boolean[]) array);
        }
        if (array instanceof char[]) {
            return Arrays.toString((char[]) array);
        }
        if (array instanceof short[]) {
            return Arrays.toString((short[]) array);
        }

        if (array.getClass().isArray()) {
            return "<unknown array type>";
        }
        else {
            throw new IllegalArgumentException("The given argument is no array.");
        }
    }
}
