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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ImmutableMap<K, V>
        extends AbstractMap<K, V>
{
    public static <K, V> ImmutableMap<K, V> of(K k, V v)
    {
        return new SingleImmutableMap<>(k, v);
    }

    public static <K, V> Builder<K, V> builder()
    {
        return new Builder<>();
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2), new EntryNode<>(k3, v3)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2), new EntryNode<>(k3, v3), new EntryNode<>(k4, v4)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2), new EntryNode<>(k3, v3), new EntryNode<>(k4, v4), new EntryNode<>(k5, v5)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2), new EntryNode<>(k3, v3),
                new EntryNode<>(k4, v4), new EntryNode<>(k5, v5), new EntryNode<>(k6, v6)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2), new EntryNode<>(k3, v3),
                new EntryNode<>(k4, v4), new EntryNode<>(k5, v5), new EntryNode<>(k6, v6), new EntryNode<>(k7, v7)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {new EntryNode<>(k1, v1), new EntryNode<>(k2, v2), new EntryNode<>(k3, v3), new EntryNode<>(k4, v4),
                new EntryNode<>(k5, v5), new EntryNode<>(k6, v6), new EntryNode<>(k7, v7), new EntryNode<>(k8, v8)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> copy(Map<K, V> map)
    {
        int size = map.size();
        int capacity = Integer.highestOneBit(size);
        if (capacity * 0.83D < size) {
            capacity = capacity << 1;
        }
        int mask = capacity - 1;
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] keyTable = (EntryNode<K, V>[]) new EntryNode[capacity];
        for (Entry<K, V> entry : map.entrySet()) {
            int hash = Objects.hashCode(entry.getKey());
            int index = hash & mask;
            EntryNode<K, V> node = new EntryNode<>(entry.getKey(), entry.getValue());
            node.next = keyTable[index];
            keyTable[index] = node;
            checkNotDuplicateKey(node);
        }
        return new HashImmutableMap<>(keyTable, size, mask);
    }

    private static <K, V> ImmutableMap<K, V> copyOfNodes(EntryNode<K, V>[] nodes)
    {
        return copyOfNodes(Arrays.asList(nodes));
    }

    private static <K, V> ImmutableMap<K, V> copyOfNodes(Collection<EntryNode<K, V>> nodes)
    {
        int size = nodes.size();
        int capacity = Integer.highestOneBit(size);
        if (capacity * 0.83D < size) {
            capacity = capacity << 1;
        }
        int mask = capacity - 1;
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] buckets = (EntryNode<K, V>[]) new EntryNode[capacity];
        for (EntryNode<K, V> node : nodes) {
            int hash = Objects.hashCode(node.key);
            int index = hash & mask;
            node.next = buckets[index];
            buckets[index] = node;
            checkNotDuplicateKey(node);
        }
        return new HashImmutableMap<>(buckets, size, mask);
    }

    private static <K, V> void checkNotDuplicateKey(EntryNode<K, V> node)
    {
        K key = node.key;
        EntryNode<K, V> next = node.next;
        while (next != null) {
            if (Objects.equals(next.key, key)) {
                throw new IllegalStateException("duplicate key " + key);
            }
            next = next.next;
        }
    }

    private static class HashImmutableMap<K, V>
            extends ImmutableMap<K, V>
            implements Externalizable
    {
        private EntryNode<K, V>[] keyTable;
        private int size;
        private int mask;

        private HashImmutableMap(EntryNode<K, V>[] keyTable, int size, int mask)
        {
            this.keyTable = keyTable;
            this.size = size;
            this.mask = mask;
        }

        public HashImmutableMap() {}

        @Override
        public V get(Object key)
        {
            int hash = Objects.hashCode(key);
            int index = hash & mask;
            EntryNode<K, V> node = keyTable[index];
            while (node != null) {
                if (Objects.equals(node.key, key)) {
                    return node.value;
                }
                node = node.next;
            }
            return null;
        }

        @Override
        public int size()
        {
            return size;
        }

        @Override
        public Set<Entry<K, V>> entrySet()
        {
            return new ImmutableSet<Entry<K, V>>()
            {
                @Override
                public Iterator<Entry<K, V>> iterator()
                {
                    return new Iterator<Entry<K, V>>()
                    {
                        private int index = 0;
                        private EntryNode<K, V> node = nextBucket();

                        @Override
                        public boolean hasNext()
                        {
                            return node != null;
                        }

                        @Override
                        public Entry<K, V> next()
                        {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            EntryNode<K, V> old = node;
                            this.node = nextNode();
                            return old;
                        }

                        private EntryNode<K, V> nextBucket()
                        {
                            while (index < keyTable.length) {
                                EntryNode<K, V> node = keyTable[index++];
                                if (node != null) {
                                    return node;
                                }
                            }
                            return null;
                        }

                        private EntryNode<K, V> nextNode()
                        {
                            if (node.next != null) {
                                return node.next;
                            }
                            return nextBucket();
                        }
                    };
                }

                @Override
                public int size()
                {
                    return HashImmutableMap.this.size;
                }

                @Override
                public boolean contains(Object o)
                {
                    if (!(o instanceof Entry)) {
                        return false;
                    }
                    Object key = ((Entry<?, ?>) o).getKey();
                    V value = HashImmutableMap.this.get(key);
                    return Objects.equals(value, ((Entry<?, ?>) o).getValue());
                }
            };
        }

        @Override
        public void writeExternal(ObjectOutput out)
                throws IOException
        {
            out.writeInt(size);
            out.writeInt(keyTable.length); //capacity
            EntryNode<K, V>[] tab = keyTable;
            if (size > 0 && tab != null) {
                for (EntryNode<K, V> node : tab) {
                    for (EntryNode<K, V> e = node; e != null; e = e.next) {
                        out.writeObject(e.key);
                        out.writeObject(e.value);
                    }
                }
            }
        }

        @Override
        public void readExternal(ObjectInput in)
                throws IOException, ClassNotFoundException
        {
            this.size = in.readInt();
            @SuppressWarnings("unchecked")
            EntryNode<K, V>[] buckets = new EntryNode[in.readInt()];
            this.mask = buckets.length - 1;

            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                EntryNode<K, V> node = new EntryNode<>((K) in.readObject(), (V) in.readObject());
                int hash = Objects.hashCode(node.key);
                int index = hash & mask;
                node.next = buckets[index];
                buckets[index] = node;
                //checkNotDuplicateKey(node);
            }
            this.keyTable = buckets;
        }
    }

    private static class SingleImmutableMap<K, V>
            extends ImmutableMap<K, V>
            implements Serializable
    {
        private final K key;
        private final V value;

        private SingleImmutableMap(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public V get(Object key)
        {
            if (Objects.equals(this.key, key)) {
                return value;
            }
            return null;
        }

        @Override
        public Collection<V> values()
        {
            return ImmutableList.of(value);
        }

        @Override
        public Set<Entry<K, V>> entrySet()
        {
            return ImmutableSet.of(new SimpleEntry<>(key, value));
        }
    }

    @Override
    public V put(K key, V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(K key, V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        throw new UnsupportedOperationException();
    }

    private static class EntryNode<K, V>
            implements Entry<K, V>
    {
        private final K key;
        private final V value;
        private EntryNode<K, V> next;

        private EntryNode(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }
    }

    public static class Builder<K, V>
    {
        private final ArrayList<EntryNode<K, V>> nodes = new ArrayList<>();

        public Builder<K, V> put(K k, V v)
        {
            nodes.add(new EntryNode<>(k, v));
            return this;
        }

        public Builder<K, V> putAll(Map<K, V> map)
        {
            nodes.ensureCapacity(map.size());
            for (Entry<K, V> entry : map.entrySet()) {
                nodes.add(new EntryNode<>(entry.getKey(), entry.getValue()));
            }
            return this;
        }

        public ImmutableMap<K, V> build()
        {
            return copyOfNodes(nodes);
        }
    }
}
