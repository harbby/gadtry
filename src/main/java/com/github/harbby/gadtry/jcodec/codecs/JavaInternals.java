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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JavaInternals
{
    private JavaInternals() {}

    public static class SingletonMapSerializer<K, V>
            implements Serializer<Map<K, V>>
    {
        @Override
        public void write(Jcodec jcodec, OutputView output, Map<K, V> value)
        {
            for (Map.Entry<K, V> entry : value.entrySet()) {
                jcodec.writeClassAndObject(output, entry.getKey());
                jcodec.writeClassAndObject(output, entry.getValue());
                break;
            }
        }

        @Override
        public Map<K, V> read(Jcodec jcodec, InputView input, Class<? extends Map<K, V>> typeClass)
        {
            return Collections.singletonMap(jcodec.readClassAndObject(input), jcodec.readClassAndObject(input));
        }
    }

    public static class ArrayList<T>
            implements Serializer<List<T>>
    {
        @Override
        public void write(Jcodec jcodec, OutputView output, List<T> value)
        {
            output.writeVarInt(value.size(), true);
            for (T v : value) {
                jcodec.writeClassAndObject(output, v);
            }
        }

        @Override
        public List<T> read(Jcodec jcodec, InputView input, Class<? extends List<T>> typeClass)
        {
            int size = input.readVarInt(true);
            @SuppressWarnings("unchecked")
            T[] arr = (T[]) new Object[size];
            for (int i = 0; i < size; i++) {
                arr[i] = jcodec.readClassAndObject(input);
            }
            return Arrays.asList(arr);
        }
    }
}
