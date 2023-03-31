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
package com.github.harbby.gadtry.jcodec;

import com.github.harbby.gadtry.collection.tuple.Tuple2;

import java.util.Comparator;

import static java.util.Objects.requireNonNull;

public interface Tuple2Serializer<K, V>
        extends Serializer<Tuple2<K, V>>
{
    public Serializer<K> getKeyEncoder();

    public Serializer<V> getValueEncoder();

    public static class Tuple2KVSerializer<K, V>
            implements Tuple2Serializer<K, V>
    {
        private final Class<? extends K> keyClass;
        private final Class<? extends V> vClass;
        private final Serializer<K> kSerializer;
        private final Serializer<V> vSerializer;

        public Tuple2KVSerializer(Class<? extends K> keyClass, Class<? extends V> vClass, Serializer<K> kSerializer, Serializer<V> vSerializer)
        {
            this.keyClass = keyClass;
            this.vClass = vClass;
            this.kSerializer = requireNonNull(kSerializer, "kEncoder is null");
            this.vSerializer = requireNonNull(vSerializer, "vEncoder is null");
        }

        @Override
        public Serializer<K> getKeyEncoder()
        {
            return kSerializer;
        }

        @Override
        public Serializer<V> getValueEncoder()
        {
            return vSerializer;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, Tuple2<K, V> value)
        {
            requireNonNull(value, "Tuple2 value is null");
            kSerializer.write(jcodec, output, value.key());
            vSerializer.write(jcodec, output, value.value());
        }

        @Override
        public Tuple2<K, V> read(Jcodec jcodec, InputView input, Class<? extends Tuple2<K, V>> typeClass)
        {
            return Tuple2.of(kSerializer.read(jcodec, input, keyClass), vSerializer.read(jcodec, input, vClass));
        }

        @Override
        public Comparator<Tuple2<K, V>> comparator()
        {
            return (kv1, kv2) -> {
                int than = kSerializer.comparator().compare(kv1.key(), kv2.key());
                if (than != 0) {
                    return than;
                }
                return vSerializer.comparator().compare(kv1.value(), kv2.value());
            };
        }
    }

    public static class Tuple2OnlyKeySerializer<K>
            implements Tuple2Serializer<K, Void>
    {
        private final Class<? extends K> kClass;
        private final Serializer<K> kSerializer;

        public Tuple2OnlyKeySerializer(Class<? extends K> kClass, Serializer<K> kSerializer)
        {
            this.kClass = kClass;
            this.kSerializer = requireNonNull(kSerializer, "kEncoder is null");
        }

        @Override
        public Serializer<K> getKeyEncoder()
        {
            return kSerializer;
        }

        @Override
        public Serializer<Void> getValueEncoder()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, Tuple2<K, Void> value)
        {
            kSerializer.write(jcodec, output, value.key());
        }

        @Override
        public Tuple2<K, Void> read(Jcodec jcodec, InputView input, Class<? extends Tuple2<K, Void>> typeClass)
        {
            return Tuple2.of(kSerializer.read(jcodec, input, kClass), null);
        }

        @Override
        public Comparator<Tuple2<K, Void>> comparator()
        {
            return (kv1, kv2) -> kSerializer.comparator().compare(kv1.key(), kv2.key());
        }
    }
}
