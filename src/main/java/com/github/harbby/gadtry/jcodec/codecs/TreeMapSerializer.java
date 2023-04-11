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
import java.util.Map;
import java.util.TreeMap;

public class TreeMapSerializer<K, V>
        implements Serializer<TreeMap<K, V>>
{
    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, TreeMap<K, V> map)
    {
        if (map == null) {
            output.writeVarInt(0, true);
            return;
        }

        int size = map.size();
        output.writeVarInt(size + 1, true);
        Comparator<? super K> comparator = map.comparator();
        jcodec.writeClassAndObject(output, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            jcodec.writeClassAndObject(output, entry.getKey());
            jcodec.writeClassAndObject(output, entry.getValue());
        }
    }

    @Override
    public TreeMap<K, V> read(Jcodec jcodec, InputView input, Class<? extends TreeMap<K, V>> typeClass)
    {
        int size = input.readVarInt(true);
        if (size == 0) {
            return null;
        }
        size--;
        Comparator<? super K> comparator = jcodec.readClassAndObject(input);
        TreeMap<K, V> treeMap = new TreeMap<>(comparator);
        for (int i = 0; i < size; i++) {
            treeMap.put(jcodec.readClassAndObject(input), jcodec.readClassAndObject(input));
        }
        return treeMap;
    }
}
