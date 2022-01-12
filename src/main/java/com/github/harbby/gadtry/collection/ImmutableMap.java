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

import com.github.harbby.gadtry.base.Iterators;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
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
        @SuppressWarnings("unchecked")
        Entry<K, V>[] nodes = map.entrySet().toArray(new Entry[0]);
        int size = nodes.length;
        int capacity = Integer.highestOneBit(size);
        if (capacity * 0.83D < size) {
            capacity = capacity << 1;
        }
        int mask = capacity - 1;
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] buckets = (EntryNode<K, V>[]) new EntryNode[capacity];
        for (Entry<K, V> entry : nodes) {
            EntryNode<K, V> node = new EntryNode<>(entry.getKey(), entry.getValue());
            int index = node.hash & mask;
            EntryNode<K, V> first = buckets[index];
            if (first instanceof ImmutableMap.TreeBin) {
                ((TreeBin<K, V>) first).putVal(node);
            }
            else {
                node.next = buckets[index];
                int count = checkNotDuplicateKey(node);
                if (count >= 8) {
                    buckets[index] = convertToTree(node);
                }
                else {
                    buckets[index] = node;
                }
            }
        }
        return new HashImmutableMap<>(buckets, mask, nodes);
    }

    private static class TreeNode<K, V>
            extends RedBlackTree.TreeNode<K, V>
            implements Map.Entry<K, V>
    {
        private final K key;
        private final int hash;
        private final V value;

        private TreeNode(K key, V value, int hash)
        {
            this.key = key;
            this.hash = hash;
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

        @Override
        public int getHash()
        {
            return hash;
        }
    }

    private static class TreeBin<K, V>
            extends EntryNode<K, V>
    {
        private final RedBlackTree<K, V> tree = new RedBlackTree<>();

        private TreeBin()
        {
            super(null, null);
        }

        public void putVal(EntryNode<K, V> node)
        {
            tree.putNode(new TreeNode<>(node.key, node.value, node.hash));
        }

        public V get(Object key, int hash)
        {
            return tree.find(key, hash);
        }

        public boolean containsKey(Object key, int hash)
        {
            return tree.containsKey(key, hash);
        }

        public Iterator<RedBlackTree.TreeNode<K, V>> iterator()
        {
            return tree.iterator();
        }
    }

    private static <K, V> ImmutableMap<K, V> copyOfNodes(EntryNode<K, V>[] nodes)
    {
        int size = nodes.length;
        int capacity = Integer.highestOneBit(size);
        if (capacity * 0.83D < size) {
            capacity = capacity << 1;
        }
        int mask = capacity - 1;
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] buckets = (EntryNode<K, V>[]) new EntryNode[capacity];
        for (EntryNode<K, V> node : nodes) {
            int index = node.hash & mask;
            EntryNode<K, V> first = buckets[index];
            if (first instanceof ImmutableMap.TreeBin) {
                ((TreeBin<K, V>) first).putVal(node);
            }
            else {
                node.next = buckets[index];
                int count = checkNotDuplicateKey(node);
                if (count >= 8) { //jdk redBlack tree default length is 8
                    buckets[index] = convertToTree(node);
                }
                else {
                    buckets[index] = node;
                }
            }
        }
        return new HashImmutableMap<>(buckets, mask, nodes);
    }

    private static <K, V> TreeBin<K, V> convertToTree(EntryNode<K, V> first)
    {
        TreeBin<K, V> tree = new TreeBin<>();
        EntryNode<K, V> next = first;
        while (next != null) {
            tree.putVal(next);
            next = next.next;
        }
        return tree;
    }

    private static <K, V> int checkNotDuplicateKey(EntryNode<K, V> node)
    {
        K key = node.key;
        int hash = node.hash;
        EntryNode<K, V> next = node.next;
        int c = 1;
        while (next != null) {
            if (hash == next.hash && Objects.equals(next.key, key)) {
                throw new IllegalStateException("duplicate key " + key);
            }
            c++;
            next = next.next;
        }
        return c;
    }

    private static class HashImmutableMap<K, V>
            extends ImmutableMap<K, V>
            implements Externalizable
    {
        private EntryNode<K, V>[] buckets;
        private int size;
        private int mask;
        private transient Entry<K, V>[] nodes; //foreach

        private HashImmutableMap(EntryNode<K, V>[] buckets, int mask, Entry<K, V>[] nodes)
        {
            this.buckets = buckets;
            this.size = nodes.length;
            this.mask = mask;
            this.nodes = nodes;
        }

        public HashImmutableMap() {}

        @Override
        public V get(Object key)
        {
            int hash = Objects.hashCode(key);
            int index = hash & mask;
            EntryNode<K, V> node = buckets[index];
            if (node instanceof TreeBin) {
                return ((TreeBin<K, V>) node).get(key, hash);
            }
            while (node != null) {
                if (hash == node.hash && Objects.equals(node.key, key)) {
                    return node.value;
                }
                node = node.next;
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key)
        {
            int hash = Objects.hashCode(key);
            int index = hash & mask;
            EntryNode<K, V> node = buckets[index];
            if (node instanceof TreeBin) {
                return ((TreeBin<K, V>) node).containsKey(key, hash);
            }
            while (node != null) {
                if (hash == node.hash && Objects.equals(node.key, key)) {
                    return true;
                }
                node = node.next;
            }
            return false;
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
                    return Iterators.of(nodes);
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
                    if (!HashImmutableMap.this.containsKey(key)) {
                        return false;
                    }
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
            out.writeInt(buckets.length); //capacity
            EntryNode<K, V>[] tab = buckets;
            if (size > 0 && tab != null) {
                for (EntryNode<K, V> node : tab) {
                    if (node instanceof TreeBin) {
                        Iterator<RedBlackTree.TreeNode<K, V>> iterator = ((TreeBin<K, V>) node).iterator();
                        while (iterator.hasNext()) {
                            RedBlackTree.TreeNode<K, V> treeNode = iterator.next();
                            out.writeObject(treeNode.getKey());
                            out.writeObject(treeNode.getValue());
                        }
                    }
                    else {
                        for (EntryNode<K, V> e = node; e != null; e = e.next) {
                            out.writeObject(e.key);
                            out.writeObject(e.value);
                        }
                    }
                }
            }
        }

        @Override
        public void readExternal(ObjectInput in)
                throws IOException, ClassNotFoundException
        {
            int size = in.readInt();
            @SuppressWarnings("unchecked")
            EntryNode<K, V>[] nodes = new EntryNode[size];
            @SuppressWarnings("unchecked")
            EntryNode<K, V>[] buckets = new EntryNode[in.readInt()];
            int mask = buckets.length - 1;

            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                EntryNode<K, V> node = new EntryNode<>((K) in.readObject(), (V) in.readObject());
                nodes[i] = node;
                int index = node.hash & mask;
                node.next = buckets[index];
                int count = checkNotDuplicateKey(node);
                if (count >= 8) {
                    buckets[index] = convertToTree(node);
                }
                else {
                    buckets[index] = node;
                }
            }
            this.buckets = buckets;
            this.nodes = nodes;
            this.size = size;
            this.mask = mask;
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
        public boolean containsKey(Object key)
        {
            return Objects.equals(this.key, key);
        }

        @Override
        public V get(Object key)
        {
            if (this.containsKey(key)) {
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
            return ImmutableSet.of(new EntryNode<>(key, value));
        }
    }

    @Override
    public abstract boolean containsKey(Object key);

    @Override
    public abstract V get(Object key);

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
        private final int hash;
        private final V value;
        private EntryNode<K, V> next;

        private EntryNode(K key, V value)
        {
            this.key = key;
            this.value = value;
            this.hash = Objects.hashCode(key);
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

        @SuppressWarnings("unchecked")
        public ImmutableMap<K, V> build()
        {
            return copyOfNodes(nodes.toArray(new EntryNode[0]));
        }
    }
}
