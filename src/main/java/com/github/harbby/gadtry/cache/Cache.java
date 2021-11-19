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
package com.github.harbby.gadtry.cache;

import com.github.harbby.gadtry.function.exception.Supplier;

import java.util.Map;

public interface Cache<K, V>
{
    V getIfPresent(K key);

    <E extends Exception> V get(K key, Supplier<V, E> caller) throws E;

    public V remove(K k);

    public V put(K key, V value);

    void clear();

    int size();

    Map<K, V> asMap();

    Map<K, V> getAllPresent();
}
