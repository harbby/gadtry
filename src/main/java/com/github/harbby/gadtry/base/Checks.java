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

import java.util.function.Function;

public class Checks
{
    private Checks() {}

    public static void checkState(boolean ok)
    {
        if (!ok) {
            throw new IllegalStateException();
        }
    }

    public static void checkState(boolean ok, String error)
    {
        if (!ok) {
            throw new IllegalStateException(error);
        }
    }

    public static <T> boolean checkContainsTrue(T[] source, Function<T, Boolean> filter)
    {
        if (source == null || source.length == 0) {
            return true;
        }
        for (T t : source) {
            if (filter.apply(t)) {
                return true;
            }
        }
        return false;
    }
}
