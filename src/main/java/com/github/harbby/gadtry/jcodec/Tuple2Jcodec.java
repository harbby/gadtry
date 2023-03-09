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

public interface Tuple2Jcodec<K, V>
        extends Jcodec<Tuple2<K, V>>
{
    public Jcodec<K> getKeyEncoder();

    public Jcodec<V> getValueEncoder();

    public static class Tuple2KVJcodec<K, V>
            implements Tuple2Jcodec<K, V>
    {
        private final Jcodec<K> kJcodec;
        private final Jcodec<V> vJcodec;

        public Tuple2KVJcodec(Jcodec<K> kJcodec, Jcodec<V> vJcodec)
        {
            this.kJcodec = requireNonNull(kJcodec, "kEncoder is null");
            this.vJcodec = requireNonNull(vJcodec, "vEncoder is null");
        }

        @Override
        public Jcodec<K> getKeyEncoder()
        {
            return kJcodec;
        }

        @Override
        public Jcodec<V> getValueEncoder()
        {
            return vJcodec;
        }

        @Override
        public void encoder(Tuple2<K, V> value, OutputView output)
        {
            requireNonNull(value, "Tuple2 value is null");
            kJcodec.encoder(value.key(), output);
            vJcodec.encoder(value.value(), output);
        }

        @Override
        public Tuple2<K, V> decoder(InputView input)
        {
            return Tuple2.of(kJcodec.decoder(input), vJcodec.decoder(input));
        }

        @Override
        public Comparator<Tuple2<K, V>> comparator()
        {
            return (kv1, kv2) -> {
                int than = kJcodec.comparator().compare(kv1.key(), kv2.key());
                if (than != 0) {
                    return than;
                }
                return vJcodec.comparator().compare(kv1.value(), kv2.value());
            };
        }
    }

    public static class Tuple2OnlyKeyJcodec<K>
            implements Tuple2Jcodec<K, Void>
    {
        private final Jcodec<K> kJcodec;

        public Tuple2OnlyKeyJcodec(Jcodec<K> kJcodec)
        {
            this.kJcodec = requireNonNull(kJcodec, "kEncoder is null");
        }

        @Override
        public Jcodec<K> getKeyEncoder()
        {
            return kJcodec;
        }

        @Override
        public Jcodec<Void> getValueEncoder()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void encoder(Tuple2<K, Void> value, OutputView output)
        {
            kJcodec.encoder(value.key(), output);
        }

        @Override
        public Tuple2<K, Void> decoder(InputView input)
        {
            return Tuple2.of(kJcodec.decoder(input), null);
        }

        @Override
        public Comparator<Tuple2<K, Void>> comparator()
        {
            return (kv1, kv2) -> kJcodec.comparator().compare(kv1.key(), kv2.key());
        }
    }
}
