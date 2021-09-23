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
package com.github.harbby.gadtry.democode;

import java.util.AbstractList;

public class OnewayLinkedList<V>
        extends AbstractList<V>
{
    private Node<V> headNode;
    private Node<V> lastNode;
    private int size;

    @Override
    public V get(int index)
    {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Node<V> node = headNode;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node.v;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean add(V v)
    {
        Node<V> next = new Node<>(v);
        if (size == 0) {
            this.lastNode = next;
            this.headNode = next;
        }
        else {
            this.lastNode.next = next;
            this.lastNode = next;
        }

        size++;
        return true;
    }

    static class Node<V>
    {
        private final V v;
        private Node<V> next;

        public Node(V v) {this.v = v;}

        public Node<V> addNext(V next)
        {
            Node<V> nextNode = new Node<>(next);
            this.next = nextNode;
            return nextNode;
        }

        @Override
        public String toString()
        {
            return String.valueOf(v);
        }
    }

    public void reverse()
    {
        this.lastNode = headNode;
        this.headNode = reverse(headNode);
    }

    static <V> Node<V> reverse(Node<V> headNode)
    {
        if (headNode.next == null) {
            return headNode;
        }
        Node<V> node = headNode;
        Node<V> next = node.next;
        while (next != null) {
            Node<V> bak = next.next;
            next.next = node;
            //-prepare next loop
            node = next;
            next = bak;
        }
        headNode.next = null;
        return node;
    }
}
