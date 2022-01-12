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

import com.github.harbby.gadtry.graph.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RedBlackTreeTest
{
    static class EntryNode<K, V>
            extends RedBlackTree.TreeNode<K, V>
            implements Map.Entry<K, V>
    {
        final K key;
        final int hash;
        V value;

        public EntryNode(K key, V value, int hash)
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
            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int getHash()
        {
            return hash;
        }
    }

    @Test
    public void baseTest()
    {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        List<Integer> data = MutableList.of(12, 23, 45, 34, 40, 67, 78, 89, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180);
        data.forEach(i -> {
            tree.putNode(new EntryNode<>(i, "value" + i, i));
        });
        //
        //check
        data.forEach(i -> {
            String value = tree.find(i, i);
            Assert.assertEquals(value, "value" + i);
        });
        //show
        Graph.GraphBuilder<String, Void> graph = Graph.builder();
        Iterator<RedBlackTree.TreeNode<Integer, String>> iterator = tree.iterator();

        List<Integer> numbers = new ArrayList<>();
        while (iterator.hasNext()) {
            RedBlackTree.TreeNode<Integer, String> node = iterator.next();
            graph.addNode(node.getKey() + "", node.isRed() ? "red" : "black");
            RedBlackTree.TreeNode<Integer, String> parent = node.getParent();
            if (parent != null) {
                graph.addEdge(parent.getKey() + "", node.getKey() + "");
            }
            numbers.add(node.getKey());
        }
        graph.create().printShow().forEach(x -> System.out.println(x));
        Assert.assertEquals(numbers, ImmutableList.of(89, 40, 23, 12, 34, 67, 45, 78, 120, 100, 90, 110, 140, 130, 160, 150, 170, 180));
    }
}
