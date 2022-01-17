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
import com.github.harbby.gadtry.graph.Graph;
import org.fusesource.jansi.Ansi;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

//see: https://www.cs.usfca.edu/~galles/visualization/RedBlack.html
public class RedBlackTreeTest
{
    private static class EntryNode<K, V>
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

    private static class TestRedBlackTree<K, V>
            extends SingleRedBlackTree<K, V>
    {
        @Override
        public TreeNode<K, V> createNode(K key, V value, int hash)
        {
            return new EntryNode<>(key, value, hash);
        }
    }

    @Test
    public void deleteTest()
    {
        SingleRedBlackTree<Integer, String> tree = new TestRedBlackTree<>();
        List<Integer> data = ImmutableList.of(12, 34, 34, 40, 67, 78, 89, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180);  //
        data.forEach(i -> tree.put(i, "value" + i, i));
        show(tree);

        //delete
        Assert.assertEquals(tree.remove(12, 12), "value" + 12);
        Assert.assertEquals(tree.remove(140, 140), "value" + 140);
        Assert.assertEquals(tree.remove(67, 67), "value" + 67);
        show(tree);
    }

    @Test
    public void randomRemoveTest()
    {
        List<Integer> data = MutableList.of(12, 23, 45, 34, 40, 67, 78, 89, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180);
        Random random = new Random();
        SingleRedBlackTree<Integer, String> tree = new TestRedBlackTree<>();
        for (int step = 0; step < 100; step++) {
            Collections.shuffle(data, random);
            data.forEach(i -> tree.put(i, "value" + i, i));
            for (int i = 0; i < data.size(); i++) {
                int index = random.nextInt(data.size());
                int key = data.get(index);
                tree.remove(key, key);
            }
            show(tree);
            tree.clear();
        }
    }

    @Test
    public void insertTest()
    {
        //see: https://www.cs.usfca.edu/~galles/visualization/RedBlack.html
        SingleRedBlackTree<Integer, String> tree = new TestRedBlackTree<>();
        List<Integer> data = ImmutableList.of(12, 23, 45, 34, 40, 67, 78, 89, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180);
        data.forEach(i -> tree.put(i, "value" + i, i));

        //check
        List<Integer> copyData = new ArrayList<>(data);
        Collections.shuffle(copyData);
        copyData.forEach(i -> {
            String value = tree.get(i, i);
            Assert.assertEquals(value, "value" + i);
        });

        List<Integer> numbers = new ArrayList<>();
        Iterators.foreach(tree.iterator(), (n) -> numbers.add(n.getKey()));
        Assert.assertEquals(numbers, ImmutableList.of(89, 40, 23, 12, 34, 67, 45, 78, 120, 100, 90, 110, 140, 130, 160, 150, 170, 180));
        //show
        show(tree);
    }

    @Test
    public void hashDuplicateTest()
    {
        TestRedBlackTree<String, Integer> tree = new TestRedBlackTree<>();
        tree.put("a", 1, 9);
        tree.put("bb", 2, 9);
        tree.put("ccc", 3, 9);
        tree.put("dddd", 4, 9);
        tree.put("eeeee", 5, 9);
        show(tree);
        Assert.assertEquals(3, tree.get("ccc", 9).intValue());
        Assert.assertEquals(1, tree.remove("a", 9).intValue());
        Assert.assertTrue(tree.containsKey("ccc", 9));
        Assert.assertEquals(3, tree.remove("ccc", 9).intValue());
        Assert.assertFalse(tree.containsKey("ccc", 9));
        show(tree);
    }

    public static <K, V> void show(SingleRedBlackTree<K, V> tree)
    {
        Graph.GraphBuilder<GNode<K>, Void> graph = Graph.builder();
        Iterator<RedBlackTree.TreeNode<K, V>> iterator = tree.iterator();
        while (iterator.hasNext()) {
            RedBlackTree.TreeNode<K, V> treeNode = iterator.next();
            GNode<K> gNode = GNode.of(treeNode);
            graph.addNode(gNode);
            RedBlackTree.TreeNode<K, V> parent = treeNode.getParent();
            if (parent != null) {
                graph.addEdge(GNode.of(parent), gNode);
            }
        }
        graph.create().printShow().forEach(System.out::println);
    }

    private static class GNode<K>
    {
        private final K key;
        private final Ansi.Color color;

        private GNode(RedBlackTree.TreeNode<K, ?> treeNode)
        {
            this.key = treeNode.getKey();
            this.color = treeNode.isRed() ? Ansi.Color.RED : Ansi.Color.BLACK;
        }

        public static <K> GNode<K> of(RedBlackTree.TreeNode<K, ?> treeNode)
        {
            return new GNode<>(treeNode);
        }

        @Override
        public int hashCode()
        {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) {
                return true;
            }
            if (obj instanceof GNode) {
                return key.equals(((GNode<?>) obj).key);
            }
            return false;
        }

        @Override
        public String toString()
        {
            return new Ansi().fg(color).a(key).reset().toString();
        }
    }
}
