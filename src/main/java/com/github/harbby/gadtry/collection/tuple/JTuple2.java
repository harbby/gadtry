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

import java.util.Objects;

public final class JTuple2<K, V>
        implements Tuple2<K, V>
{
    private K k;
    private V v;

    public JTuple2(K k, V v)
    {
        this.k = k;
        this.v = v;
    }

    public static <K, V> Tuple2<K, V> of(K k, V v)
    {
        return new JTuple2<>(k, v);
    }

    @Override
    public void setKey(K k)
    {
        this.k = k;
    }

    @Override
    public void setValue(V v)
    {
        this.v = v;
    }

    @Override
    public K key()
    {
        return k;
    }

    @Override
    public K f1()
    {
        return k;
    }

    @Override
    public V f2()
    {
        return v;
    }

    @Override
    public V value()
    {
        return v;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getField(int pos)
    {
        switch (pos) {
            case 1:
                return (T) k;
            case 2:
                return (T) v;
            default:
                throw new IndexOutOfBoundsException(String.valueOf(pos));
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(k, v);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        JTuple2<?, ?> other = (JTuple2<?, ?>) obj;
        return Objects.equals(this.k, other.k) &&
                Objects.equals(this.v, other.v);
    }

    @Override
    public Tuple2<K, V> copy()
    {
        return new JTuple2<>(k, v);
    }

    @Override
    public String toString()
    {
        return String.format("(%s, %s)", k, v);
    }
}
