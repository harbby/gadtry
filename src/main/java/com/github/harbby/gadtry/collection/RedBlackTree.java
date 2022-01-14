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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public class RedBlackTree<K, V>
{
    private TreeNode<K, V> root;

    public V putNode(TreeNode<K, V> node)
    {
        node.isRed = true; //新插入节点为红色
        if (root == null) {
            root = node;
            root.isRed = false;
            return null;
        }
        else {
            return root.add(node, this);
        }
    }

    private void rotateLeft(TreeNode<K, V> node)
    {
        TreeNode<K, V> rightChild = node.right;
        node.right = rightChild.left;
        if (rightChild.left != null) {
            rightChild.left.parent = node;
        }
        rightChild.left = node;

        TreeNode<K, V> parent = node.parent;
        rightChild.parent = parent;
        if (parent != null) {
            if (node.isLeftNode()) {
                parent.left = rightChild;
            }
            else {
                parent.right = rightChild;
            }
        }
        node.parent = rightChild;
        if (node == root) {
            root = rightChild;
            root.isRed = false;
            node.isRed = true;
        }
    }

    private void rotateRight(TreeNode<K, V> node)
    {
        TreeNode<K, V> leftChild = node.left;
        node.left = leftChild.right;
        if (leftChild.right != null) {
            leftChild.right.parent = node;
        }
        leftChild.right = node;

        TreeNode<K, V> parent = node.parent;
        leftChild.parent = parent;
        if (parent != null) {
            if (node.isLeftNode()) {
                parent.left = leftChild;
            }
            else {
                parent.right = leftChild;
            }
        }
        node.parent = leftChild;
        if (node == root) {
            root = leftChild;
            root.isRed = false;
            node.isRed = true;
        }
    }

    private void balanceInsert(TreeNode<K, V> insertedNode, boolean isLeftInsertedNode)
    {
        TreeNode<K, V> n = insertedNode;
        boolean isLeft = isLeftInsertedNode;
        while (true) {
            //assert n.isRed;
            //assert n.parent.isRed;
            TreeNode<K, V> parent = n.parent;
            TreeNode<K, V> uncle = n.getUncle();
            TreeNode<K, V> grandParent = parent.parent;
            if (uncle != null && uncle.isRed) {
                //Parent and uncle is red
                parent.isRed = false;
                uncle.isRed = false;
                grandParent.isRed = true;
                if (grandParent == root) {
                    grandParent.isRed = false;
                }
                else if (grandParent.parent != null && grandParent.parent.isRed) {
                    n = grandParent;
                    isLeft = grandParent.isLeftNode();
                    continue;
                    //don't while(true) can also be used `balanceInsert(grandParent, grandParent.isLeftNode())`;
                }
            }
            else {
                //Parent is red but uncle is black
                if (parent.isLeftNode() == isLeft) {
                    //Parent and N are on the same side
                    if (isLeft) {
                        //Parent and N are on the left
                        rotateRight(grandParent);
                        parent.isRed = false;
                        grandParent.isRed = true;
                    }
                    else {
                        //Parent and N are on the right
                        this.rotateLeft(grandParent);
                        parent.isRed = false;
                        grandParent.isRed = true;
                    }
                }
                else { //rotate at twice
                    if (isLeft) {
                        //Parent is right and N is left
                        this.rotateRight(parent);
                        //Parent and N are on the right
                        rotateLeft(grandParent);
                        n.isRed = false;
                        grandParent.isRed = true;
                    }
                    else {
                        //Parent is left and N is right
                        this.rotateLeft(parent);
                        //Parent and N are on the left
                        rotateRight(grandParent);
                        n.isRed = false;
                        grandParent.isRed = true;
                    }
                }
            }
            break;
        }
    }

    public V find(Object key, int hash)
    {
        if (root == null) {
            return null;
        }
        return root.get(key, hash);
    }

    public boolean containsKey(Object key, int hash)
    {
        return root.containsKey(key, hash);
    }

    public Iterator<TreeNode<K, V>> iterator()
    {
        return new Iterator<TreeNode<K, V>>()
        {
            private TreeNode<K, V> node = root;
            private final LinkedList<TreeNode<K, V>> queue = new LinkedList<>();

            {
                addNext(root);
            }

            @Override
            public boolean hasNext()
            {
                return node != null;
            }

            @Override
            public TreeNode<K, V> next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                TreeNode<K, V> old = node;
                this.node = queue.poll();
                addNext(node);
                return old;
            }

            private void addNext(TreeNode<K, V> treeNode)
            {
                if (treeNode != null) {
                    TreeNode<K, V> left = treeNode.left;
                    TreeNode<K, V> right = treeNode.right;
                    if (right != null) {
                        queue.addFirst(right);
                    }
                    if (left != null) {
                        queue.addFirst(left);
                    }
                }
            }
        };
    }

    @Override
    public String toString()
    {
        return root == null ? "" : root.toString();
    }

    public abstract static class TreeNode<K, V>
            implements Map.Entry<K, V>
    {
        private boolean isRed;
        private TreeNode<K, V> left;
        private TreeNode<K, V> right;
        private TreeNode<K, V> parent;

        public abstract K getKey();

        public abstract V getValue();

        public abstract V setValue(V value);

        public abstract int getHash();

        public V get(Object key, int hash)
        {
            TreeNode<K, V> next = this;
            do {
                int hash0 = next.getHash();
                if (hash0 == hash && key.equals(next.getKey())) {
                    return next.getValue();
                }
                next = hash > hash0 ? next.right : next.left;
            }
            while (next != null);
            return null;
        }

        public boolean containsKey(Object key, int hash)
        {
            TreeNode<K, V> next = this;
            do {
                int hash0 = next.getHash();
                if (hash0 == hash && key.equals(next.getKey())) {
                    return true;
                }
                next = hash > hash0 ? next.right : next.left;
            }
            while (next != null);
            return false;
        }

        boolean isLeftNode()
        {
            return parent.left == this;
        }

        private TreeNode<K, V> getUncle()
        {
            TreeNode<K, V> grandParent = this.parent.parent;
            return parent.isLeftNode() ? grandParent.right : grandParent.left;
        }

        public V add(TreeNode<K, V> node, RedBlackTree<K, V> tree)
        {
            TreeNode<K, V> next = this;
            while (true) {
                int hash0 = next.getHash();
                if (hash0 == node.getHash() && next.getKey().equals(node.getKey())) {
                    return this.setValue(node.getValue());
                }
                else if (node.getHash() > hash0) {
                    if (next.right == null) {
                        next.right = node;
                        node.parent = next;
                        if (next.isRed) {
                            tree.balanceInsert(node, false);
                        }
                        return null;
                    }
                    else {
                        next = next.right;
                    }
                }
                else {
                    if (next.left == null) {
                        next.left = node;
                        node.parent = next;
                        if (next.isRed) {
                            tree.balanceInsert(node, true);
                        }
                        return null;
                    }
                    else {
                        next = next.left;
                    }
                }
            }
        }

        public boolean isRed()
        {
            return isRed;
        }

        public TreeNode<K, V> getParent()
        {
            return parent;
        }

        public TreeNode<K, V> getLeft()
        {
            return left;
        }

        public TreeNode<K, V> getRight()
        {
            return right;
        }

        @Override
        public final String toString()
        {
            return toStringHelper(this)
                    .add("key", getKey())
                    .add("color", isRed ? "red" : "black")
                    .add("value", getValue())
                    .add("left", left.getKey())
                    .add("right", right.getKey())
                    .toString();
        }
    }
}
