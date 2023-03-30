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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OnewayLinkedListTest
{
    private static class Node<V>
    {
        private final V v;
        private Node<V> next;

        public Node(V v)
        {
            this.v = v;
        }

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

    @Test
    public void revertLinks()
    {
        Node<String> a = new Node<>("a");
        Node<String> b = new Node<>("b");
        Node<String> c = new Node<>("c");
        Node<String> d = new Node<>("d");
        a.next = b;
        b.next = c;
        c.next = d;
        //or  a.addNext("b").addNext("c").addNext("d");
        Node<String> node = reverse(a);
        Assertions.assertEquals(node.v, "d");

        StringBuilder builder = new StringBuilder();
        while (node != null) {
            builder.append(node.v);
            node = node.next;
        }
        Assertions.assertEquals(builder.toString(), "dcba");
    }

    @Test
    public void listTest()
    {
        OnewayLinkedList<String> list = new OnewayLinkedList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(String.valueOf((char) ('a' + i)), list.get(i));
        }

        list.reverse();
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(String.valueOf((char) ('d' - i)), list.get(i));
        }
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
