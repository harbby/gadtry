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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ivan
 * @date 2021.02.07 21:34:23
 * map Serialize
 */
public class MapJcodec<K, V>
        implements Jcodec<Map<K, V>>
{
    private final Jcodec<K> kJcodec;
    private final Jcodec<V> vJcodec;

    public MapJcodec(Jcodec<K> kJcodec, Jcodec<V> vJcodec)
    {
        this.kJcodec = kJcodec;
        this.vJcodec = vJcodec;
    }

    @Override
    public void encoder(Map<K, V> value, OutputView output)
    {
        if (value == null) {
            output.writeVarInt(0, false);
            return;
        }
        final int size = value.size();
        //write size on the head
        output.writeVarInt(size + 1, false);
        //write key and value
        for (Map.Entry<K, V> entry : value.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue();
            kJcodec.encoder(k, output);
            vJcodec.encoder(v, output);
        }
    }

    @Override
    public Map<K, V> decoder(InputView input)
    {
        final int size = input.readVarInt(false) - 1;
        if (size == -1) {
            return null;
        }
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            K key = kJcodec.decoder(input);
            V value = vJcodec.decoder(input);
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
