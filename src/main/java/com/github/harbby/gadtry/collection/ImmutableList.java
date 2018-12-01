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
package com.github.harbby.gadtry.collection;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImmutableList
{
    private ImmutableList() {}

    public static <T> List<T> copy(List<? extends T> list)
    {
        return Collections.unmodifiableList(list);
    }

    public static <T> List<T> copy(Iterable<? extends T> iterable)
    {
        Stream.Builder<T> builder = Stream.builder();
        for (T it : iterable) {
            builder.add(it);
        }
        return builder.build().collect(Collectors.toList());
    }

    public static <T> List<T> of(T... t)
    {
        return Stream.of(t).collect(Collectors.toList());
    }

    public static <T> Builder<T> builder()
    {
        return new Builder<>();
    }

    public static class Builder<T>
    {
        private Stream.Builder<T> builder = Stream.builder();

        public Builder<T> add(T t)
        {
            builder.add(t);
            return this;
        }

        public Builder<T> addAll(Iterable<T> iterable)
        {
            for (T it : iterable) {
                builder.add(it);
            }
            return this;
        }

        public List<T> build()
        {
            return builder.build().collect(Collectors.toList());
        }
    }
}
