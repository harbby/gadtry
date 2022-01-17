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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class ImmutableMap<K, V>
        extends AbstractMap<K, V>
{
    static final float DEFAULT_LOAD_FACTOR = 0.83f;

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
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2), EntryNode.checkOf(k3, v3)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2), EntryNode.checkOf(k3, v3), EntryNode.checkOf(k4, v4)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2), EntryNode.checkOf(k3, v3), EntryNode.checkOf(k4, v4),
                EntryNode.checkOf(k5, v5)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2), EntryNode.checkOf(k3, v3),
                EntryNode.checkOf(k4, v4), EntryNode.checkOf(k5, v5), EntryNode.checkOf(k6, v6)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2), EntryNode.checkOf(k3, v3),
                EntryNode.checkOf(k4, v4), EntryNode.checkOf(k5, v5), EntryNode.checkOf(k6, v6), EntryNode.checkOf(k7, v7)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8)
    {
        @SuppressWarnings("unchecked")
        EntryNode<K, V>[] nodes = new EntryNode[] {EntryNode.checkOf(k1, v1), EntryNode.checkOf(k2, v2), EntryNode.checkOf(k3, v3), EntryNode.checkOf(k4, v4),
                EntryNode.checkOf(k5, v5), EntryNode.checkOf(k6, v6), EntryNode.checkOf(k7, v7), EntryNode.checkOf(k8, v8)};
        return copyOfNodes(nodes);
    }

    public static <K, V> ImmutableMap<K, V> copy(Map<K, V> map)
    {
        int size = map.size();
        int capacity = Integer.highestOneBit(size);
        if (capacity * DEFAULT_LOAD_FACTOR < size) {
            capacity = capacity << 1;
        }
        int mask = capacity - 1;
        @SuppressWarnings("unchecked")
        HashEntry<K, V>[] buckets = (HashEntry<K, V>[]) new HashEntry[capacity];
        TreeView<K, V> treeView = new TreeView<>(buckets);
        for (Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            int hash = hash(key);
            int index = hash & mask;
            HashEntry<K, V> hashEntry = buckets[index];
            if (hashEntry instanceof ImmutableMap.TreeNode) {
                treeView.put(index, key, value, hash);
            }
            else {
                EntryNode<K, V> node = new EntryNode<>(key, value, hash);
                node.next = (EntryNode<K, V>) hashEntry;
                int count = checkNotDuplicateKey(node);
                if (count >= 8) {
                    buckets[index] = convertToTree(node, treeView, index);
                }
                else {
                    buckets[index] = node;
                }
            }
        }
        return new HashImmutableMap<>(buckets, size, mask, treeView);
    }

    private static class TreeNode<K, V>
            extends RedBlackTree.TreeNode<K, V>
            implements HashEntry<K, V>
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
            throw new IllegalStateException("duplicate key " + key);
        }

        @Override
        public int getHash()
        {
            return hash;
        }
    }

    private static class TreeView<K, V>
            extends RedBlackTree<K, V>
    {
        private final HashEntry<K, V>[] buckets;

        private TreeView(HashEntry<K, V>[] buckets)
        {
            this.buckets = buckets;
        }

        @Override
        public TreeNode<K, V> getRoot(int treeId)
        {
            return (TreeNode<K, V>) buckets[treeId];
        }

        @Override
        public void setRoot(int treeId, TreeNode<K, V> root)
        {
            buckets[treeId] = root;
        }

        @Override
        public TreeNode<K, V> createNode(K key, V value, int hash)
        {
            return new ImmutableMap.TreeNode<>(key, value, hash);
        }
    }

    private static <K, V> ImmutableMap<K, V> copyOfNodes(EntryNode<K, V>[] nodes)
    {
        int size = nodes.length;
        int capacity = Integer.highestOneBit(size);
        if (capacity * DEFAULT_LOAD_FACTOR < size) {
            capacity = capacity << 1;
        }
        int mask = capacity - 1;
        @SuppressWarnings("unchecked")
        HashEntry<K, V>[] buckets = (HashEntry<K, V>[]) new HashEntry[capacity];
        TreeView<K, V> treeView = new TreeView<>(buckets);
        for (EntryNode<K, V> node : nodes) {
            int index = node.hash & mask;
            HashEntry<K, V> hashEntry = buckets[index];
            if (hashEntry instanceof ImmutableMap.TreeNode) {
                treeView.put(index, node.key, node.value, node.hash);
            }
            else {
                node.next = (EntryNode<K, V>) hashEntry;
                int count = checkNotDuplicateKey(node);
                if (count >= 8) { //jdk redBlack tree default length is 8
                    buckets[index] = convertToTree(node, treeView, index);
                }
                else {
                    buckets[index] = node;
                }
            }
        }
        return new HashImmutableMap<>(buckets, size, mask, treeView);
    }

    private static <K, V> TreeNode<K, V> convertToTree(EntryNode<K, V> first, TreeView<K, V> treeView, int index)
    {
        treeView.buckets[index] = null;
        EntryNode<K, V> next = first;
        while (next != null) {
            treeView.put(index, next.key, next.value, next.hash);
            next = next.next;
        }
        return (TreeNode<K, V>) treeView.getRoot(index);
    }

    private static <K, V> int checkNotDuplicateKey(EntryNode<K, V> node)
    {
        K key = node.key;
        int hash = node.hash;
        EntryNode<K, V> next = node.next;
        int c = 1;
        while (next != null) {
            if (hash == next.hash && key.equals(next.key)) {
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
        private HashEntry<K, V>[] buckets;
        private int size;
        private int mask;
        //        private transient Entry<K, V>[] nodes; //foreach
        private transient TreeView<K, V> treeView;

        private HashImmutableMap(HashEntry<K, V>[] buckets, int size, int mask, TreeView<K, V> treeView)
        {
            this.buckets = buckets;
            this.size = size;
            this.mask = mask;
            this.treeView = treeView;
        }

        public HashImmutableMap() {}

        @Override
        public V get(Object key)
        {
            int hash = hash(key);
            int index = hash & mask;
            HashEntry<K, V> hashEntry = buckets[index];
            if (hashEntry instanceof TreeNode) {
                return treeView.get((TreeNode<K, V>) hashEntry, key, hash);
            }
            EntryNode<K, V> node = (EntryNode<K, V>) hashEntry;
            while (node != null) {
                if (hash == node.hash && key.equals(node.key)) {
                    return node.value;
                }
                node = node.next;
            }
            return null;
        }

        @Override
        public V getOrDefault(Object key, V defaultValue)
        {
            V value = this.get(key);
            return value == null ? defaultValue : value;
        }

        @Override
        public boolean containsKey(Object key)
        {
            int hash = hash(key);
            int index = hash & mask;
            HashEntry<K, V> hashEntry = buckets[index];
            if (hashEntry instanceof TreeNode) {
                return treeView.containsKey((TreeNode<K, V>) hashEntry, key, hash);
            }
            EntryNode<K, V> node = (EntryNode<K, V>) hashEntry;
            while (node != null) {
                if (hash == node.hash && key.equals(node.key)) {
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
                    return new Iterator<Entry<K, V>>()
                    {
                        private int index = 0;
                        private Iterator<? extends Entry<K, V>> bucketIterator = nextBucketIterator();

                        @Override
                        public boolean hasNext()
                        {
                            return bucketIterator != null;
                        }

                        @Override
                        public Entry<K, V> next()
                        {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            Entry<K, V> entry = bucketIterator.next();
                            if (!bucketIterator.hasNext()) {
                                bucketIterator = nextBucketIterator();
                            }
                            return entry;
                        }

                        private Iterator<? extends HashEntry<K, V>> nextBucketIterator()
                        {
                            while (index < buckets.length) {
                                HashEntry<K, V> entry = buckets[index++];
                                if (entry != null) {
                                    if (entry instanceof TreeNode) {
                                        return treeView.iterator((TreeNode<K, V>) entry);
                                    }
                                    else {
                                        EntryNode<K, V> entryNode = (EntryNode<K, V>) entry;
                                        return entryNode.iterator();
                                    }
                                }
                            }
                            return null;
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
                    if (value == null) {
                        return false;
                    }
                    return value.equals(((Entry<?, ?>) o).getValue());
                }
            };
        }

        @Override
        public void writeExternal(ObjectOutput out)
                throws IOException
        {
            out.writeInt(size);
            out.writeInt(buckets.length); //capacity
            HashEntry<K, V>[] tab = buckets;
            if (size > 0 && tab != null) {
                for (HashEntry<K, V> hashEntry : tab) {
                    if (hashEntry instanceof TreeNode) {
                        Iterator<RedBlackTree.TreeNode<K, V>> iterator = treeView.iterator((TreeNode<K, V>) hashEntry);
                        while (iterator.hasNext()) {
                            RedBlackTree.TreeNode<K, V> treeNode = iterator.next();
                            out.writeObject(treeNode.getKey());
                            out.writeObject(treeNode.getValue());
                        }
                    }
                    else {
                        EntryNode<K, V> node = (EntryNode<K, V>) hashEntry;
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
            HashEntry<K, V>[] buckets = new HashEntry[in.readInt()];
            TreeView<K, V> treeView = new TreeView<>(buckets);
            int mask = buckets.length - 1;

            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                K key = (K) in.readObject();
                @SuppressWarnings("unchecked")
                V value = (V) in.readObject();
                int hash = hash(key);
                int index = hash & mask;

                HashEntry<K, V> hashEntry = buckets[index];
                if (hashEntry instanceof TreeNode) {
                    TreeNode<K, V> treeNode = new TreeNode<>(key, value, hash);
                    treeView.put(index, treeNode);
                }
                else {
                    EntryNode<K, V> node = new EntryNode<>(key, value, hash);
                    node.next = (EntryNode<K, V>) hashEntry;
                    int count = checkNotDuplicateKey(node);
                    if (count >= 8) {
                        buckets[index] = convertToTree(node, treeView, index);
                    }
                    else {
                        buckets[index] = node;
                    }
                }
            }
            this.buckets = buckets;
            this.size = size;
            this.mask = mask;
            this.treeView = treeView;
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
            this.key = requireNonNull(key, "key is null");
            this.value = requireNonNull(value, "value is null");
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public boolean containsKey(Object key)
        {
            return this.key.equals(key);
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
        public V getOrDefault(Object key, V defaultValue)
        {
            return this.key.equals(key) ? value : defaultValue;
        }

        @Override
        public Collection<V> values()
        {
            return ImmutableList.of(value);
        }

        @Override
        public Set<Entry<K, V>> entrySet()
        {
            return ImmutableSet.of(EntryNode.checkOf(key, value));
        }
    }

    @Override
    public abstract boolean containsKey(Object key);

    public abstract V getOrDefault(Object key, V defaultValue);

    @Override
    public abstract int size();

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

    private static int hash(Object key)
    {
        return Objects.hashCode(key);
    }

    private static class EntryNode<K, V>
            implements HashEntry<K, V>
    {
        private final K key;
        private final int hash;
        private final V value;
        private EntryNode<K, V> next;

        private static <K, V> EntryNode<K, V> checkOf(K key, V value)
        {
            return new EntryNode<>(
                    requireNonNull(key, "key is null"),
                    requireNonNull(value, "value is null"),
                    hash(key));
        }

        private EntryNode(K key, V value, int hash)
        {
            this.key = key;
            this.value = value;
            this.hash = hash;
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

        private Iterator<EntryNode<K, V>> iterator()
        {
            return new Iterator<EntryNode<K, V>>()
            {
                private EntryNode<K, V> next = EntryNode.this;

                @Override
                public boolean hasNext()
                {
                    return next != null;
                }

                @Override
                public EntryNode<K, V> next()
                {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    EntryNode<K, V> old = next;
                    next = next.next;
                    return old;
                }
            };
        }
    }

    public static class Builder<K, V>
    {
        private final ArrayList<EntryNode<K, V>> nodes = new ArrayList<>();

        public Builder<K, V> put(K k, V v)
        {
            nodes.add(EntryNode.checkOf(k, v));
            return this;
        }

        public Builder<K, V> putAll(Map<K, V> map)
        {
            nodes.ensureCapacity(map.size());
            for (Entry<K, V> entry : map.entrySet()) {
                nodes.add(EntryNode.checkOf(entry.getKey(), entry.getValue()));
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
