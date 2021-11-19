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

import com.github.harbby.gadtry.base.Maths;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static com.github.harbby.gadtry.base.Maths.smearHashCode;
import static java.util.Objects.requireNonNull;

public class CuckooStashHashMap<K, V>
        extends AbstractMap<K, V>
        implements Serializable
{
    static final float DEFAULT_LOAD_FACTOR = 0.85f;
    private static final Random random = new Random();
    private final float loadFactor;

    private transient K[] keys;
    private transient V[] values;
    private int capacity;
    private int mask;
    private int stashCapacity;
    private int stashSize;
    private int size = 0;
    private int threshold;

    private int cuckooRetriesNumber;

    public CuckooStashHashMap()
    {
        this(32);
    }

    @SuppressWarnings("unchecked")
    public CuckooStashHashMap(int initSize)
    {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        int capacity = Maths.nextPowerOfTwo(initSize);

        int mask = capacity - 1;
        this.stashCapacity = Math.max(3, (int) Math.ceil(Math.log(capacity)) * 2);

        this.keys = (K[]) new Object[capacity + stashCapacity];
        this.values = (V[]) new Object[keys.length];
        this.capacity = capacity;
        this.mask = mask;
        this.cuckooRetriesNumber = Math.max(Math.min(capacity, 4), (int) Math.sqrt(capacity) / 8);
        this.threshold = (int) (capacity * loadFactor);
    }

    @Override
    public V put(K key, V value)
    {
        requireNonNull(key, "key is null");
        return this.put0(key, value);
    }

    private V put0(K key, V value)
    {
        int hash1 = key.hashCode();
        int index1 = hash1 & mask;
        if (key.equals(keys[index1])) {
            V old = values[index1];
            values[index1] = value;
            return old;
        }

        //hash2
        int hash2 = smearHashCode(hash1);
        int index2 = hash2 & mask;
        if (key.equals(keys[index2])) {
            V old = values[index2];
            values[index2] = value;
            return old;
        }

        //hash3
        int hash3 = smearHashCode(hash2);
        int index3 = hash3 & mask;
        if (key.equals(keys[index3])) {
            V old = values[index3];
            values[index3] = value;
            return old;
        }
        //check Stash
        for (int i = capacity; i < capacity + stashSize; i++) {
            if (key.equals(keys[i])) {
                V old = values[i];
                values[i] = value;
                return old;
            }
        }
        size++;

        if (keys[index1] == null) {
            keys[index1] = key;
            values[index1] = value;
        }
        else if (keys[index2] == null) {
            keys[index2] = key;
            values[index2] = value;
        }
        else if (keys[index3] == null) {
            keys[index3] = key;
            values[index3] = value;
        }
        else {
            this.cuckooPush(key, value, index1, index2, index3);
        }

        if (size > threshold) {
            resize();
        }
        return null;
    }

    private void cuckooPush(K key, V value, int index1, int index2, int index3)
    {
        K popKey = key;
        V popValue = value;
        int hash1, hash2, hash3;

        int pushNumber = 0;
        do {
            switch (random.nextInt(3)) {
                case 0: {
                    K key0 = keys[index1];
                    V value0 = values[index1];
                    keys[index1] = popKey;
                    values[index1] = popValue;
                    popKey = key0;
                    popValue = value0;
                    break;
                }
                case 1: {
                    K key0 = keys[index2];
                    V value0 = values[index2];
                    keys[index2] = popKey;
                    values[index2] = popValue;
                    popKey = key0;
                    popValue = value0;
                    break;
                }
                default: {
                    K key0 = keys[index3];
                    V value0 = values[index3];
                    keys[index3] = popKey;
                    values[index3] = popValue;
                    popKey = key0;
                    popValue = value0;
                }
            }

            if (popKey == null) {
                return;
            }

            hash1 = popKey.hashCode();
            index1 = hash1 & mask;
            hash2 = smearHashCode(hash1);
            index2 = hash2 & mask;
            hash3 = smearHashCode(hash2);
            index3 = hash3 & mask;
            if (keys[index1] == null) {
                keys[index1] = popKey;
                values[index1] = popValue;
                return;
            }
            else if (keys[index2] == null) {
                keys[index2] = popKey;
                values[index2] = popValue;
                return;
            }
            else if (keys[index3] == null) {
                keys[index3] = popKey;
                values[index3] = popValue;
                return;
            }

            pushNumber++;
        }
        while (pushNumber < cuckooRetriesNumber);

        //push to Stash
        this.pushToStash(popKey, popValue);
    }

    @SuppressWarnings("unchecked")
    private void resize()
    {
        //backup metadata
        K[] oldKeys = keys;
        V[] oldValues = values;
        int oldCapacity = capacity;
        int oldStashSize = stashSize;
        int oldSize = this.size;
        //init
        this.capacity = this.capacity << 1;
        this.mask = capacity - 1;
        this.stashCapacity = Math.max(3, (int) Math.ceil(Math.log(capacity)) * 2);
        this.cuckooRetriesNumber = Math.max(Math.min(capacity, 4), (int) Math.sqrt(capacity) / 8);
        this.threshold = (int) (capacity * loadFactor);
        this.keys = (K[]) new Object[capacity + stashCapacity];
        this.values = (V[]) new Object[keys.length];
        this.stashSize = 0;
        this.size = 0;

        int count = 0;
        //copy stash data
        K key;
        for (int i = oldCapacity; i < oldCapacity + oldStashSize; i++) {
            this.put0(oldKeys[i], oldValues[i]);
        }
        count += oldStashSize;

        for (int i = 0; i < oldCapacity && count < oldSize; i++) {
            key = oldKeys[i];
            if (key != null) {
                count++;
                this.put0(key, oldValues[i]);
            }
        }
    }

    @Override
    public V get(Object key)
    {
        requireNonNull(key, "key is null");
        int index = findKey(key);
        if (index > -1) {
            return values[index];
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key)
    {
        if (key == null) {
            return false;
        }

        int index = findKey(key);
        return index > -1;
    }

    @Override
    public V remove(Object key)
    {
        requireNonNull(key, "key is null");
        int index = findKey(key);
        if (index > -1) {
            return removeByIndex(index);
        }
        return null;
    }

    private V removeByIndex(int index)
    {
        V oldValue = values[index];
        keys[index] = null;
        values[index] = null;
        size--;

        if (index >= capacity) {
            if (index < capacity + stashSize - 1) {
                System.arraycopy(keys, index + 1, keys, index, capacity + stashSize - index);
                System.arraycopy(values, index + 1, values, index, capacity + stashSize - index);
            }
            stashSize--;
        }
        return oldValue;
    }

    @Override
    public boolean containsValue(Object value)
    {
        int endIndex = capacity + stashSize;
        for (int i = 0, count = 0; i < endIndex && count < size; i++) {
            if (keys[i] != null) {
                count++;
                if (Objects.equals(value, values[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private int findKey(Object key)
    {
        int hash1 = key.hashCode();
        int index1 = hash1 & mask;
        if (key.equals(keys[index1])) {
            return index1;
        }

        //hash2
        int hash2 = smearHashCode(hash1);
        int index2 = hash2 & mask;
        if (key.equals(keys[index2])) {
            return index2;
        }

        //hash3
        int hash3 = smearHashCode(hash2);
        int index3 = hash3 & mask;
        if (key.equals(keys[index3])) {
            return index3;
        }
        //check Stash
        for (int i = capacity; i < capacity + stashSize; i++) {
            if (key.equals(keys[i])) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Set<K> keySet()
    {
        return new AbstractSet<K>()
        {
            @Override
            public Iterator<K> iterator()
            {
                return new ForeachIterator<K>()
                {
                    @Override
                    public K wrapperNode(K key, int index)
                    {
                        return key;
                    }
                };
            }

            @Override
            public int size()
            {
                return size;
            }
        };
    }

    @Override
    public Collection<V> values()
    {
        return new AbstractCollection<V>()
        {
            @Override
            public Iterator<V> iterator()
            {
                return new ForeachIterator<V>()
                {
                    @Override
                    public V wrapperNode(K key, int index)
                    {
                        return values[index];
                    }
                };
            }

            @Override
            public int size()
            {
                return size;
            }
        };
    }

    @Override
    public int size()
    {
        return size;
    }

    private void pushToStash(K key, V value)
    {
        if (stashSize >= capacity + stashCapacity) {
            resize();
            this.put0(key, value);
            return;
        }
        for (int i = capacity; i < capacity + stashCapacity; i++) {
            if (keys[i] == null) {
                keys[i] = key;
                values[i] = value;
                stashSize++;
                return;
            }
        }
    }

    @Override
    public void clear()
    {
        this.size = 0;
        this.stashSize = 0;
        Arrays.fill(keys, null);
        Arrays.fill(values, null);
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return new AbstractSet<Entry<K, V>>()
        {
            @Override
            public Iterator<Entry<K, V>> iterator()
            {
                return new ForeachIterator<Entry<K, V>>()
                {
                    @Override
                    public Entry<K, V> wrapperNode(K key, int index)
                    {
                        return new MapEntry(key, index);
                    }
                };
            }

            @Override
            public int size()
            {
                return size;
            }
        };
    }

    private class MapEntry
            implements Entry<K, V>
    {
        private final int keyIndex;
        private final K key;

        private MapEntry(K key, int keyIndex)
        {
            this.keyIndex = keyIndex;
            this.key = key;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return values[keyIndex];
        }

        @Override
        public V setValue(V value)
        {
            V old = values[keyIndex];
            values[keyIndex] = value;
            return old;
        }
    }

    private abstract class ForeachIterator<E>
            implements Iterator<E>
    {
        private int expectedMapCount = size;
        private int count = 0;
        private int index = 0;
        private final int maxIndex = capacity + stashSize;
        private final int hashSize = size - stashSize;

        @Override
        public boolean hasNext()
        {
            if (expectedMapCount != size) {
                throw new ConcurrentModificationException();
            }
            return count < expectedMapCount;
        }

        @Override
        public E next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            K key;
            while (count < hashSize) {
                key = keys[index];
                if (key != null) {
                    count++;
                    return wrapperNode(key, index++);
                }
                index++;
            }
            if (index < capacity) {
                index = capacity;
            }
            while (index < maxIndex) {
                count++;
                return wrapperNode(keys[index], index++);
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public void remove()
        {
            int removeIndex = index - 1;
            if (removeIndex < 0 || keys[removeIndex] == null) {
                throw new IllegalStateException("please call `iterator.next()` first");
            }
            CuckooStashHashMap.this.removeByIndex(removeIndex);
            count--;
            expectedMapCount--;
        }

        public abstract E wrapperNode(K key, int keyIndex);
    }
}
