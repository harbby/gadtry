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
package com.github.harbby.gadtry.collection.tuple;

public interface Tuple2<K, V>
        extends Tuple
{
    public static <K, V> Tuple2<K, V> of(K k, V v)
    {
        return new JTuple2<>(k, v);
    }

    public K key();

    public V value();

    default K f1()
    {
        return key();
    }

    default V f2()
    {
        return value();
    }

    public void setKey(K k);

    public void setValue(V v);

    default void setF1(K k)
    {
        this.setKey(k);
    }

    default void setF2(V v)
    {
        this.setValue(v);
    }

    @Override
    default int getArity()
    {
        return 2;
    }

    @Override
    @SuppressWarnings("unchecked")
    public default <T> T getField(int pos)
    {
        switch (pos) {
            case 1:
                return (T) key();
            case 2:
                return (T) value();
            default:
                throw new IndexOutOfBoundsException(String.valueOf(pos));
        }
    }

    @Override
    public Tuple2<K, V> copy();
}
