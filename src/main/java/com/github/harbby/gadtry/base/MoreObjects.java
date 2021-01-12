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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

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

    public static void checkState(boolean ok, String error, Object... args)
    {
        if (!ok) {
            throw new IllegalStateException(String.format(error, args));
        }
    }

    public static void checkArgument(boolean ok)
    {
        if (!ok) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean ok, String error)
    {
        if (!ok) {
            throw new IllegalArgumentException(error);
        }
    }

    public static void checkArgument(boolean ok, String error, Object... args)
    {
        if (!ok) {
            throw new IllegalArgumentException(String.format(error, args));
        }
    }

    public static <T> T nullElse(T value, T defaultValue)
    {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static <T> T nullElse(T value, Supplier<T> defaultValue)
    {
        requireNonNull(defaultValue, "defaultValue is null");
        if (value == null) {
            return defaultValue.get();
        }
        return value;
    }

    @SafeVarargs
    public static <T> T firstNonNull(T... values)
    {
        requireNonNull(values, "Both parameters are null");
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        throw new NullPointerException("Both parameters are null");
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
