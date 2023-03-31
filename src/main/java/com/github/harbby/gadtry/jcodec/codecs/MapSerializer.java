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
package com.github.harbby.gadtry.jcodec.codecs;

import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MapSerializer<K, V>
        implements Serializer<Map<K, V>>
{
    private final Class<? extends K> keyClass;
    private final Class<? extends V> vClass;
    private final Serializer<K> kSerializer;
    private final Serializer<V> vSerializer;

    public MapSerializer(Class<? extends K> keyClass, Class<? extends V> vClass, Serializer<K> kSerializer, Serializer<V> vSerializer)
    {
        this.keyClass = keyClass;
        this.vClass = vClass;
        this.kSerializer = kSerializer;
        this.vSerializer = vSerializer;
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, Map<K, V> value)
    {
        if (value == null) {
            output.writeVarInt(0, true);
            return;
        }
        final int size = value.size();
        //write size on the head
        output.writeVarInt(size + 1, true);
        //write key and value
        for (Map.Entry<K, V> entry : value.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue();
            kSerializer.write(jcodec, output, k);
            vSerializer.write(jcodec, output, v);
        }
    }

    @Override
    public Map<K, V> read(Jcodec jcodec, InputView input, Class<? extends Map<K, V>> typeClass)
    {
        final int size = input.readVarInt(true) - 1;
        if (size == -1) {
            return null;
        }
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            K key = kSerializer.read(jcodec, input, keyClass);
            V value = vSerializer.read(jcodec, input, vClass);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public Comparator<Map<K, V>> comparator()
    {
        throw new UnsupportedOperationException("map value not support comparator");
    }
}
