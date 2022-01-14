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
package com.github.harbby.gadtry.cache;

import com.github.harbby.gadtry.function.exception.Supplier;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * non thread safe cache
 */
class LRUHashCache<K, V>
        extends AbstractMap<K, V>
        implements Cache<K, V>
{
    static final float DEFAULT_LOAD_FACTOR = 0.83f;

    private final Node<K, V> headNode = new Node<K, V>(null, null, Integer.MIN_VALUE)
    {
        @Override
        public String toString()
        {
            return "headNode";
        }
    };
    private final Node<K, V> endNode = new Node<K, V>(null, null, Integer.MIN_VALUE)
    {
        @Override
        public String toString()
        {
            return "endNode";
        }
    };

    private final int maxCapacity;
    private final Callback<K, V> callback;
    private final long maxWriteDuration;
    private final long maxReadDuration;

    private final Node<K, V>[] buckets;
    private final int mask;

    LRUHashCache(int maxCapacity, Callback<K, V> callback, long maxDuration)
    {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("maxCapacity < 0");
        }
        int bucketsSize = (int) (maxCapacity / DEFAULT_LOAD_FACTOR);
        this.mask = bucketsSize - 1;
        this.buckets = (Node<K, V>[]) new Node[bucketsSize];
        this.maxCapacity = maxCapacity;
        this.callback = requireNonNull(callback, "callback is null");
        this.maxReadDuration = maxDuration;
        this.maxWriteDuration = maxDuration;
        this.clear();
    }

    @Deprecated
    @Override
    public V get(Object key)
    {
        return getIfPresent((K) key);
    }

    private void deleteNodeOnList(Node<K, V> node)
    {
        node.last.next = node.next;
        node.next.last = node.last;
        //node.last = null;
        //node.next = null;
    }

    private Node<K, V> remove0(Object key, int hash)
    {
        int index = hash & mask;
        Node<K, V> node = buckets[index];
        Node<K, V> last = null;
        while (node != null) {
            if (hash == node.hash && Objects.equals(key, node.key)) {
                if (last == null) {
                    buckets[index] = null;
                }
                else {
                    last.hashNext = node.hashNext;
                }
                deleteNodeOnList(node);
                size--;
                return node;
            }
            last = node;
            node = node.hashNext;
        }

        return this.headNode; //return value is null node
    }

    private int size = 0;

    @Override
    public V put(K key, V value)
    {
        int hash = Objects.hashCode(key.hashCode());
        int index = hash & mask;
        Node<K, V> first = buckets[index];
        Node<K, V> node = first;
        while (node != null) {
            if (hash == node.hash && Objects.equals(key, node.key)) {
                V returnValue = node.value;
                node.updateValue(value);
                moveToHead(node);
                return returnValue;
            }
            node = node.hashNext;
        }
        Node<K, V> newNode = new Node<>(key, value, hash);
        newNode.hashNext = first;
        buckets[index] = newNode;
        size++;

        //添加newNode到队头
        addHeadNode(newNode);
        checkCapacity();
        return null;
    }

    private void doCallBack(Node<K, V> node, EvictCause cause)
    {
        try {
            callback.call(node.key, node.value, cause);
        }
        catch (Exception e) {
            //the call back don't throw Exception
        }
    }

    private boolean isTimeout(Node<K, V> node)
    {
        long currentTime = System.currentTimeMillis();

        return currentTime - node.readTime > maxReadDuration ||
                currentTime - node.writeTime > maxWriteDuration;
    }

    @Override
    public V remove(Object key)
    {
        int hash = Objects.hashCode(key.hashCode());
        return remove0(key, hash).value;
    }

    @Override
    public void clear()
    {
        Arrays.fill(buckets, null);
        this.size = 0;

        headNode.next = endNode;
        endNode.last = headNode;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return new AbstractSet<Entry<K, V>>()
        {
            @Override
            public Iterator<Entry<K, V>> iterator()
            {
                return new Iterator<Entry<K, V>>()
                {
                    private Node<K, V> next = headNode.next;

                    @Override
                    public boolean hasNext()
                    {
                        return next != endNode;
                    }

                    @Override
                    public Node<K, V> next()
                    {
                        Node<K, V> old = next;
                        next = next.next;
                        return old;
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
    public V getIfPresent(K key)
    {
        int hash = Objects.hashCode(key.hashCode());
        int index = hash & mask;
        Node<K, V> node = buckets[hash & mask];
        Node<K, V> last = null;
        while (node != null) {
            if (hash == node.hash && Objects.equals(key, node.key)) {
                if (isTimeout(node)) {
                    //hash remove
                    if (last == null) {
                        buckets[index] = null;
                    }
                    else {
                        last.hashNext = node.hashNext;
                    }
                    size--;
                    //liked remove
                    deleteNodeOnList(node);
                    doCallBack(node, EvictCause.TIME_OUT);
                    return null;
                }
                else {
                    node.updateReadTime();
                    moveToHead(node);
                    return node.value;
                }
            }
            last = node;
            node = node.next;
        }
        return null;
    }

    /**
     * add to head
     *
     * @param node move
     */
    private void addHeadNode(Node<K, V> node)
    {
        Node<K, V> next = this.headNode.next;
        //add to head
        node.next = next;
        node.last = this.headNode;
        next.last = node;
        this.headNode.next = node;
    }

    /***
     * move to head
     * @param node node
     */
    private void moveToHead(Node<K, V> node)
    {
        if (node == headNode.next) {
            return;
        }
        //remove this pos
        deleteNodeOnList(node);
        //add to head
        this.addHeadNode(node);
    }

    @Override
    public <E extends Exception> V get(K key, Supplier<V, E> caller)
            throws E
    {
        int hash = Objects.hashCode(key.hashCode());
        int index = hash & mask;
        Node<K, V> first = buckets[index];
        Node<K, V> node = first;
        while (node != null) {
            if (hash == node.hash && Objects.equals(key, node.key)) {
                if (isTimeout(node)) {
                    node.updateValue(caller.apply());
                }
                else {
                    node.updateReadTime();
                }
                moveToHead(node);
                return node.value;
            }
            node = node.hashNext;
        }
        //not find key
        //add new key node
        V returnValue = caller.apply();
        Node<K, V> newNode = new Node<>(key, returnValue, hash);
        newNode.hashNext = first;
        buckets[index] = newNode;
        size++;

        //添加newNode到队头
        addHeadNode(newNode);
        checkCapacity();
        return returnValue;
    }

    private void checkCapacity()
    {
        if (size == maxCapacity + 1) {
            //达到容量上限。开始移除endNode节点
            Node<K, V> endDataNode = endNode.last;
            if (remove0(endDataNode.key, endDataNode.hash) != endDataNode) {
                throw new ConcurrentModificationException();
            }
            doCallBack(endDataNode, EvictCause.OVERFLOW);
        }
    }

    @Override
    public Map<K, V> asMap()
    {
        return this;
    }

    @Override
    public Map<K, V> getAllPresent()
    {
        Map<K, V> map = new HashMap<>();
        for (Entry<K, V> entry : this.entrySet()) {
            Node<K, V> node = (Node<K, V>) entry;
            if (!isTimeout(node)) {
                map.put(node.key, node.value);
            }
        }
        return map;
    }

    private static class Node<K, V>
            implements Entry<K, V>
    {
        private final K key;
        private final int hash;
        private V value;
        private long writeTime;
        private long readTime;

        private Node<K, V> hashNext;

        private Node<K, V> last;
        private Node<K, V> next;

        private Node(K key, V value, int hash)
        {
            this.key = key;
            this.hash = hash;
            this.value = value;
            this.writeTime = System.currentTimeMillis();
            this.readTime = writeTime;
        }

        private void updateReadTime()
        {
            this.readTime = System.currentTimeMillis();
        }

        private void updateValue(V value)
        {
            this.value = value;
            this.writeTime = System.currentTimeMillis();
        }

        @Override
        public String toString()
        {
            return String.format("{key: %s, value: %s}", key, value);
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
}
