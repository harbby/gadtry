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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class MoreObjects
{
    private MoreObjects() {}

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

    public static <T> T firstNonNull(T... values)
    {
        if (values == null) {
            throw new NullPointerException("Both parameters are null");
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        throw new NullPointerException("Both parameters are null");
    }

    public static <T> boolean checkContainsTrue(T[] ts, Function<T, Boolean> filter)
    {
        if (ts == null || ts.length == 0) {
            return true;
        }
        checkState(filter != null, "filter is null");
        for (T t : ts) {
            if (filter.apply(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean checkContainsTrue(Iterable<T> ts, Function<T, Boolean> filter)
    {
        if (ts == null) {
            return true;
        }
        checkState(filter != null, "filter is null");
        for (T t : ts) {
            if (filter.apply(t)) {
                return true;
            }
        }
        return false;
    }

    public static ToStringBuilder toStringHelper(Object object)
    {
        return new ToStringBuilder(object);
    }

    public static class ToStringBuilder
    {
        private final Object object;
        private final Map<String, Object> builder = new LinkedHashMap<>();

        public ToStringBuilder(Object object)
        {
            this.object = object;
        }

        public ToStringBuilder add(String key, Object value)
        {
            builder.put(key, value);
            return this;
        }

        public ToStringBuilder add(String key, int value)
        {
            builder.put(key, String.valueOf(value));
            return this;
        }

        public ToStringBuilder add(String key, long value)
        {
            builder.put(key, String.valueOf(value));
            return this;
        }

        public ToStringBuilder add(String key, boolean value)
        {
            builder.put(key, String.valueOf(value));
            return this;
        }

        public ToStringBuilder add(String key, float value)
        {
            builder.put(key, String.valueOf(value));
            return this;
        }

        public ToStringBuilder add(String key, double value)
        {
            builder.put(key, String.valueOf(value));
            return this;
        }

        public String toString()
        {
            return object.getClass().getSimpleName() + builder;
        }
    }
}
