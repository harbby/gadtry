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
package com.github.harbby.gadtry.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MutableMap
{
    private MutableMap() {}

    public static <K, V> Map<K, V> copy(Map<? extends K, ? extends V> map)
    {
        MutableMap.Builder<K, V> builder = MutableMap.builder();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static <K, V> Builder<K, V> builder()
    {
        return new Builder<>();
    }

    public static class Builder<K, V>
    {
        private Map<K, V> builder = new HashMap<>();

        public Builder<K, V> put(K k, V v)
        {
            builder.put(k, v);
            return this;
        }

        public Builder<K, V> putAll(Map<? extends K, ? extends V> map)
        {
            builder.putAll(map);
            return this;
        }

        public Map<K, V> build()
        {
            return builder;
        }
    }

    public static <K, V> Map<K, V> of()
    {
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> of(K k1, V v1)
    {
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .build();
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2)
    {
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .build();
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3)
    {
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .build();
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4)
    {
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .build();
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5)
    {
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .build();
    }

    public static <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6)
    {
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .put(k6, v6)
                .build();
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
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .put(k6, v6)
                .put(k7, v7)
                .build();
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
        return MutableMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .put(k6, v6)
                .put(k7, v7)
                .put(k8, v8)
                .build();
    }
}
