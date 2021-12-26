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
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * non thread safe cache
 */
class LRUHashCache<K, V>
        extends AbstractMap<K, V>
        implements Cache<K, V>
{
    private final Node<K, V> headNode = new Node<K, V>(null, null)
    {
        @Override
        public String toString()
        {
            return "headNode";
        }
    };
    private final Node<K, V> endNode = new Node<K, V>(null, null)
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

    private final Map<K, Node<K, V>> map;

    LRUHashCache(int maxCapacity, Callback<K, V> callback, long maxDuration)
    {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("maxCapacity < 0");
        }
        this.maxCapacity = maxCapacity;
        this.callback = requireNonNull(callback, "callback is null");
        this.map = new HashMap<>(maxCapacity);
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

    @Override
    public V put(K key, V value)
    {
        Node<K, V> old = map.get(key);
        if (old != null) {
            V returnValue = old.value;
            old.updateValue(value);
            moveToHead(old);
            return returnValue;
        }

        Node<K, V> newNode = new Node<>(key, value);
        if (map.put(key, newNode) != null) {
            throw new ConcurrentModificationException();
        }

        //添加newNode到队头
        addHeadNode(newNode);

        //达到容量上限。开始移除endNode节点
        if (map.size() == maxCapacity + 1) {
            Node<K, V> endDataNode = endNode.last;
            map.remove(endDataNode.key);
            deleteNodeOnList(endDataNode);
            doCallBack(endDataNode, EvictCause.OVERFLOW);
        }

        return null;
    }

    /**
     * lazy clear timeout
     */
    private void clearTimeoutNodes(Node<K, V> timeoutNode)
    {
        Node<K, V> maxTimeoutNode = timeoutNode;
        if (timeoutNode == headNode.next) {
            this.clear();
        }
        this.endNode.last = timeoutNode.last;
        timeoutNode.last.next = this.endNode;
        //移除该节点后面的所有节点
        while (maxTimeoutNode != endNode) {
            map.remove(maxTimeoutNode.key);
            doCallBack(maxTimeoutNode, EvictCause.TIME_OUT);
            maxTimeoutNode = maxTimeoutNode.next;
        }
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
        Node<K, V> old = map.remove(key);
        if (old != null) {
            deleteNodeOnList(old);
            return old.value;
        }

        return null;
    }

    @Override
    public void clear()
    {
        map.clear();
        headNode.next = endNode;
        endNode.last = headNode;
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        int size = map.size();
        return new AbstractSet<Map.Entry<K, V>>()
        {
            @Override
            public Iterator<Map.Entry<K, V>> iterator()
            {
                return new Iterator<Map.Entry<K, V>>()
                {
                    private Node<K, V> next = headNode.next;

                    @Override
                    public boolean hasNext()
                    {
                        return next != endNode && !isTimeout(next);
                    }

                    @Override
                    public Map.Entry<K, V> next()
                    {
                        Map.Entry<K, V> old = next;
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
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        else if (isTimeout(node)) {
            //find timeout node, clear timeoutNode -> endNode
            clearTimeoutNodes(node);
            return null;
        }
        else {
            moveToHead(node);
            return node.value;
        }
    }

    private void addHeadNode(Node<K, V> node)
    {
        Node<K, V> next = this.headNode.next;
        //移动到头部
        node.next = next;
        node.last = this.headNode;
        next.last = node;
        this.headNode.next = node;
    }

    private void moveToHead(Node<K, V> node)
    {
        node.updateReadTime();
        if (node == headNode.next) {
            return;
        }
        //将当前节点从原始位置移除
        deleteNodeOnList(node);
        //添加到头部
        this.addHeadNode(node);
    }

    @Override
    public <E extends Exception> V get(K key, Supplier<V, E> caller)
            throws E
    {
        Node<K, V> node = map.get(key);
        if (node == null) {
            V value = caller.apply();
            this.put(key, value);
            return value;
        }

        if (isTimeout(node)) {
            //clear timeoutNode -> endNode
            if (node.next != endNode) {
                clearTimeoutNodes(node.next);
            }
            node.updateValue(caller.apply());
        }

        moveToHead(node);
        return node.value;
    }

    @Override
    public Map<K, V> asMap()
    {
        return this;
    }

    @Override
    public Map<K, V> getAllPresent()
    {
        return new HashMap<>(this);
    }

    private static class Node<K, V>
            implements Entry<K, V>
    {
        private final K key;
        private V value;
        private long writeTime;
        private long readTime;
        private Node<K, V> last;
        private Node<K, V> next;

        private Node(K key, V value)
        {
            this.key = key;
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
