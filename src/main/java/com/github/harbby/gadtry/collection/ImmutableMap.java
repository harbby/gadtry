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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImmutableMap
{
    private ImmutableMap() {}

    public static <K, V> Map<K, V> copy(Map<? extends K, ? extends V> map)
    {
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Builder<K, V> builder()
    {
        return new Builder<>();
    }

    public static class Builder<K, V>
    {
        private Stream.Builder<Tuple2<K, V>> builder = Stream.builder();

        public Builder<K, V> put(K k, V v)
        {
            builder.add(Tuple2.of(k, v));
            return this;
        }

        public Builder<K, V> putAll(Map<K, V> map)
        {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                builder.add(Tuple2.of(entry.getKey(), entry.getValue()));
            }
            return this;
        }

        public Map<K, V> build()
        {
            return builder.build().collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
        }
    }

    public static <K, V> Map<K, V> of(K k1, V v1)
    {
        return Stream.of(Tuple2.of(k1, v1)).collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2),
                Tuple2.of(k3, v3))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2),
                Tuple2.of(k3, v3),
                Tuple2.of(k4, v4))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2),
                Tuple2.of(k3, v3),
                Tuple2.of(k4, v4),
                Tuple2.of(k5, v5))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2),
                Tuple2.of(k3, v3),
                Tuple2.of(k4, v4),
                Tuple2.of(k5, v5),
                Tuple2.of(k6, v6))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6,
            K k7, V v7)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2),
                Tuple2.of(k3, v3),
                Tuple2.of(k4, v4),
                Tuple2.of(k5, v5),
                Tuple2.of(k6, v6),
                Tuple2.of(k7, v7))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6,
            K k7, V v7,
            K k8, V v8)
    {
        return Stream.of(
                Tuple2.of(k1, v1),
                Tuple2.of(k2, v2),
                Tuple2.of(k3, v3),
                Tuple2.of(k4, v4),
                Tuple2.of(k5, v5),
                Tuple2.of(k6, v6),
                Tuple2.of(k7, v7),
                Tuple2.of(k8, v8))
                .collect(Collectors.toMap(Tuple2::f0, Tuple2::f1));
    }
}
