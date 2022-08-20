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

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class MoreObjects
{
    private MoreObjects() {}

    /**
     * copy source object field data to target Object
     *
     * @param modelClass copy model
     * @param source     source object
     * @param target     target object
     */
    public static <T> void copyWriteObjectState(Class<T> modelClass, T source, T target)
    {
        requireNonNull(modelClass, "modelClass is null");
        checkState(!modelClass.isInterface(), "don't copy interface field");
        requireNonNull(source, "source is null");
        requireNonNull(target, "target is null");
        Unsafe unsafe = Platform.getUnsafe();

        Class<?> it = modelClass;
        while (it != Object.class) {
            Field[] fields = it.getDeclaredFields();
            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                long offset = unsafe.objectFieldOffset(field);
                Object value = unsafe.getObject(source, offset);
                unsafe.putObject(target, offset, value);
            }
            it = it.getSuperclass();
        }
    }

    public static <E extends Throwable> void checkState(boolean access, Function<String, E> consumer, String msg, Object... objs)
            throws E
    {
        if (!access) {
            throw consumer.apply(String.format(msg, objs));
        }
    }

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

    public static <T> T getNonNull(T value, T nonNullValue)
    {
        if (value == null) {
            return requireNonNull(nonNullValue, "nonNullValue is null");
        }
        return value;
    }

    public static <T> T getNonNull(T value, Supplier<T> defaultValue)
    {
        if (value == null) {
            requireNonNull(defaultValue, "Supplier is null");
            return requireNonNull(defaultValue.get(), "Supplier.get() is null");
        }
        return value;
    }

    @SafeVarargs
    public static <T> T getFirstNonNull(T... values)
    {
        requireNonNull(values, "Both parameters are null");
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        throw new NullPointerException("values all null");
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
