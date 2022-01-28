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
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public abstract class ImmutableSet<V>
        extends AbstractSet<V>
{
    static final float DEFAULT_LOAD_FACTOR = 0.83f;
    private static final ImmutableSet<?> EMPTY = new EmptyImmutableSet<>();

    public static <V> ImmutableSet<V> copy(Set<V> set)
    {
        switch (set.size()) {
            case 0:
                return ImmutableSet.of();
            case 1:
                return ImmutableSet.of(set.iterator().next());
            case 2:
                Iterator<V> iterator = set.iterator();
                return new TwoValueImmutableSet<>(iterator.next(), iterator.next());
            default:
                @SuppressWarnings("unchecked")
                V[] arrays = (V[]) set.toArray();
                return new HashImmutableSet<>(arrays);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> ImmutableSet<V> of()
    {
        return (ImmutableSet<V>) EMPTY;
    }

    public static <V> ImmutableSet<V> of(V v)
    {
        return new SingleImmutableSet<>(v);
    }

    public static <V> ImmutableSet<V> of(V v1, V v2)
    {
        requireNonNull(v1, "value is null");
        if (v1.equals(v2)) {
            return new SingleImmutableSet<>(v1);
        }
        else {
            return new TwoValueImmutableSet<>(v1, v2);
        }
    }

    public static <V> ImmutableSet<V> of(V v1, V v2, V v3)
    {
        Set<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        return ImmutableSet.copy(set);
    }

    public static <V> ImmutableSet<V> of(V v1, V v2, V v3, V v4)
    {
        Set<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        set.add(v4);
        return ImmutableSet.copy(set);
    }

    public static <V> ImmutableSet<V> of(V v1, V v2, V v3, V v4, V v5)
    {
        Set<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        set.add(v4);
        set.add(v5);
        return ImmutableSet.copy(set);
    }

    @SafeVarargs
    public static <V> ImmutableSet<V> of(V v1, V v2, V v3, V v4, V v5, V v6, V... others)
    {
        Set<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        set.add(v4);
        set.add(v5);
        set.add(v6);
        set.addAll(Arrays.asList(others));
        return ImmutableSet.copy(set);
    }

    private static class EmptyImmutableSet<V>
            extends ImmutableSet<V>
            implements Serializable
    {
        @Override
        public Iterator<V> iterator()
        {
            return Iterators.empty();
        }

        @Override
        public boolean contains(Object o)
        {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            return c.isEmpty();
        }

        @Override
        public boolean removeIf(Predicate<? super V> filter)
        {
            requireNonNull(filter);
            return false;
        }

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public void forEach(Consumer<? super V> action)
        {
            requireNonNull(action, "action is null");
        }

        @Override
        public Spliterator<V> spliterator()
        {
            return Spliterators.emptySpliterator();
        }

        public Object[] toArray()
        {
            return ImmutableList.EMPTY_ARRAY;
        }

        public <T> T[] toArray(T[] a)
        {
            return a;
        }
    }

    private static class SingleImmutableSet<V>
            extends ImmutableSet<V>
            implements Serializable
    {
        private final V value;

        private SingleImmutableSet(V value)
        {
            this.value = requireNonNull(value, "value is null");
        }

        @Override
        public Iterator<V> iterator()
        {
            return Iterators.of(value);
        }

        @Override
        public boolean contains(Object o)
        {
            return value.equals(o);
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            return c.contains(value);
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public void forEach(Consumer<? super V> action)
        {
            action.accept(value);
        }

        @Override
        public Object[] toArray()
        {
            return new Object[] {value};
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a)
        {
            T[] objects = a.length >= 2 ? a :
                    (T[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), 2);
            objects[0] = (T) value;
            return objects;
        }
    }

    private static class TwoValueImmutableSet<V>
            extends ImmutableSet<V>
            implements Serializable
    {
        private final V value1;
        private final V value2;

        private TwoValueImmutableSet(V value1, V value2)
        {
            this.value1 = requireNonNull(value1);
            this.value2 = requireNonNull(value2);
        }

        @Override
        public Iterator<V> iterator()
        {
            return Iterators.of(value1, value2);
        }

        @Override
        public int size()
        {
            return 2;
        }

        @Override
        public boolean contains(Object o)
        {
            return value1.equals(o) || value2.equals(o);
        }

        @Override
        public void forEach(Consumer<? super V> action)
        {
            requireNonNull(action, "action is null");
            action.accept(value1);
            action.accept(value2);
        }

        @Override
        public Object[] toArray()
        {
            return new Object[] {value1, value2};
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a)
        {
            T[] objects = a.length >= 2 ? a :
                    (T[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), 2);

            objects[0] = (T) value1;
            objects[1] = (T) value2;
            return objects;
        }
    }

    private static class HashImmutableSet<V>
            extends ImmutableSet<V>
            implements Externalizable
    {
        private Node<V>[] buckets;
        private int size;
        private int mask;

        private HashImmutableSet(V[] nodes)
        {
            int size = nodes.length;
            int capacity = Integer.highestOneBit(size);
            if (capacity * DEFAULT_LOAD_FACTOR < size) {
                capacity = capacity << 1;
            }
            int mask = capacity - 1;
            @SuppressWarnings("unchecked")
            Node<V>[] buckets = new Node[capacity];
            for (V value : nodes) {
                requireNonNull(value, "value is null");
                int hash = value.hashCode();
                int index = hash & mask;
                Node<V> node = new Node<>(value);
                node.next = buckets[index];
                buckets[index] = node;
            }
            this.buckets = buckets;
            this.size = size;
            this.mask = mask;
        }

        public HashImmutableSet() {}

        @Override
        public Iterator<V> iterator()
        {
            return new Iterator<V>()
            {
                private int index;
                private Node<V> node = nextBucket();

                @Override
                public boolean hasNext()
                {
                    return node != null;
                }

                @Override
                public V next()
                {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    V value = node.value;
                    this.node = nextNode();
                    return value;
                }

                private Node<V> nextBucket()
                {
                    while (index < buckets.length) {
                        Node<V> node = buckets[index++];
                        if (node != null) {
                            return node;
                        }
                    }
                    return null;
                }

                private Node<V> nextNode()
                {
                    if (node.next != null) {
                        return node.next;
                    }
                    return nextBucket();
                }
            };
        }

        @Override
        public boolean contains(Object o)
        {
            if (o == null) {
                return false;
            }
            int hash = o.hashCode();
            int index = hash & mask;
            Node<V> node = buckets[index];
            while (node != null) {
                if (o.equals(node.value)) {
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
        public void writeExternal(ObjectOutput out)
                throws IOException
        {
            out.writeInt(size);
            out.writeInt(buckets.length); //capacity
            Node<V>[] tab = buckets;
            if (size > 0 && tab != null) {
                for (Node<V> node : tab) {
                    for (Node<V> e = node; e != null; e = e.next) {
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
            Node<V>[] buckets = new Node[in.readInt()];
            this.mask = buckets.length - 1;

            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                Node<V> node = new Node<>((V) in.readObject());
                int hash = node.value.hashCode();
                int index = hash & mask;
                node.next = buckets[index];
                buckets[index] = node;
            }
            this.buckets = buckets;
        }
    }

    @Override
    public abstract boolean contains(Object o);

    @Override
    public final boolean add(V v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super V> filter)
    {
        requireNonNull(filter);
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(Collection<? extends V> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear()
    {
        throw new UnsupportedOperationException();
    }

    private static class Node<V>
    {
        private final V value;
        private Node<V> next;

        private Node(V value)
        {
            this.value = value;
        }
    }

    public static <V> Builder<V> builder()
    {
        return new Builder<>();
    }

    public static class Builder<V>
    {
        private final Set<V> set = new HashSet<>();

        public Builder<V> add(V value)
        {
            set.add(value);
            return this;
        }

        public Builder<V> addAll(Collection<V> value)
        {
            set.addAll(value);
            return this;
        }

        public ImmutableSet<V> build()
        {
            return ImmutableSet.copy(set);
        }
    }
}
