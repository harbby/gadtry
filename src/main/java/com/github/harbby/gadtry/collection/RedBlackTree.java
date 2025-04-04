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
import java.util.NoSuchElementException;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public abstract class RedBlackTree<K, V>
{
    public abstract TreeNode<K, V> getRoot(int treeId);

    public abstract void setRoot(int treeId, TreeNode<K, V> root);

    public abstract TreeNode<K, V> createNode(K key, V value, int hash);

    public final V get(final TreeNode<K, V> root, Object key, int hash)
    {
        if (root == null) {
            return null;
        }

        TreeNode<K, V> next = root;
        do {
            int hash0 = next.getHash();
            if (hash0 == hash) {
                do {
                    if (key.equals(next.getKey())) {
                        return next.getValue();
                    }
                    next = next.hashDuplicated;
                }
                while (next != null);
                return null;
            }
            next = hash > hash0 ? next.right : next.left;
        }
        while (next != null);
        return null;
    }

    public final boolean containsKey(TreeNode<K, V> root, Object key, int hash)
    {
        if (root == null) {
            return false;
        }
        TreeNode<K, V> next = root;
        do {
            int hash0 = next.getHash();
            if (hash0 == hash) {
                do {
                    if (key.equals(next.getKey())) {
                        return true;
                    }
                    next = next.hashDuplicated;
                }
                while (next != null);
                return false;
            }
            next = hash > hash0 ? next.right : next.left;
        }
        while (next != null);
        return false;
    }

    public final V put(int treeId, K key, V value, int hash)
    {
        final TreeNode<K, V> root = getRoot(treeId);
        if (root == null) {
            TreeNode<K, V> node = createNode(key, value, hash);
            node.red = false;
            this.setRoot(treeId, node);
            return null;
        }

        TreeNode<K, V> next = root;
        do {
            int hash0 = next.getHash();
            if (hash0 == hash) {
                TreeNode<K, V> first = next;
                do {
                    if (key.equals(next.getKey())) {
                        return next.setValue(value);
                    }
                    next = next.hashDuplicated;
                }
                while (next != null);
                TreeNode<K, V> node = this.createNode(key, value, hash);
                node.hashDuplicated = first.hashDuplicated;
                first.hashDuplicated = node;
                return null;
            }
            else if (hash > hash0) {
                if (next.right == null) {
                    TreeNode<K, V> node = this.createNode(key, value, hash);
                    next.right = node;
                    node.parent = next;
                    if (next.red) {
                        this.balanceInsert(treeId, node, false);
                    }
                    return null;
                }
                else {
                    next = next.right;
                }
            }
            else {
                if (next.left == null) {
                    TreeNode<K, V> node = this.createNode(key, value, hash);
                    next.left = node;
                    node.parent = next;
                    if (next.red) {
                        this.balanceInsert(treeId, node, true);
                    }
                    return null;
                }
                else {
                    next = next.left;
                }
            }
        }
        while (true);
    }

    public final V remove(int treeId, K key, int hash)
    {
        final TreeNode<K, V> root = getRoot(treeId);
        if (root == null) {
            return null;
        }
        TreeNode<K, V> next = root;
        while (next != null) {
            int hash0 = next.getHash();
            if (hash0 == hash) {
                if (key.equals(next.getKey())) {
                    V returnValue = next.getValue();
                    TreeNode<K, V> linkedFirst = next.hashDuplicated;
                    if (linkedFirst == null) {
                        removeNode(treeId, this, next);
                        return returnValue;
                    }
                    else {
                        //move to first
                        overwrite(treeId, linkedFirst, next);
                    }
                    return returnValue;
                }
                else {
                    TreeNode<K, V> last = next;
                    while ((next = next.hashDuplicated) != null) {
                        if (key.equals(next.getKey())) {
                            last.hashDuplicated = next.hashDuplicated;
                            return next.getValue();
                        }
                        last = next;
                    }
                    return null;
                }
            }
            else if (hash > hash0) {
                next = next.right;
            }
            else {
                next = next.left;
            }
        }
        return null;
    }

    private void overwrite(int treeId, TreeNode<K, V> in, TreeNode<K, V> target)
    {
        in.parent = target.parent;
        in.red = target.red;
        if (this.getRoot(treeId) == target) {
            this.setRoot(treeId, in);
        }
        else {
            if (target.isLeftNode()) {
                target.parent.left = in;
            }
            else {
                target.parent.right = in;
            }
        }
        TreeNode<K, V> leftChild = target.left;
        in.left = leftChild;
        if (leftChild != null) {
            leftChild.parent = in;
        }
        TreeNode<K, V> rightChild = target.right;
        in.right = rightChild;
        if (rightChild != null) {
            rightChild.parent = in;
        }
    }

    private void removeNode(int treeId, RedBlackTree<K, V> tree, TreeNode<K, V> node)
    {
        if (node.left == null && node.right == null) {
            if (tree.getRoot(treeId) == node) {
                tree.setRoot(treeId, null);
                return;
            }
            if (!node.red) {
                tree.balanceRemove(treeId, node);
            }
            if (node.isLeftNode()) {
                node.parent.left = null;
            }
            else {
                node.parent.right = null;
            }
        }
        else if (node.left != null && node.right != null) {
            TreeNode<K, V> leftMax = findAndRemoveMaxNode(treeId, tree, node.left);
            overwrite(treeId, leftMax, node);
        }
        else {
            TreeNode<K, V> child = node.left == null ? node.right : node.left;
            child.parent = node.parent;
            child.red = false;
            if (tree.getRoot(treeId) != node) {
                if (node.isLeftNode()) {
                    node.parent.left = child;
                }
                else {
                    node.parent.right = child;
                }
            }
            else {
                tree.setRoot(treeId, child);
            }
        }
    }

    private static <K, V> TreeNode<K, V> findAndRemoveMaxNode(int treeId, RedBlackTree<K, V> tree, TreeNode<K, V> node)
    {
        boolean isLeft = true;
        do {
            if (node.right != null) {
                isLeft = false;
                node = node.right;
            }
            else if (node.left != null) {
                isLeft = true;
                node = node.left;
            }
            else {
                break;
            }
        }
        while (true);

        if (!node.red) {
            tree.balanceRemove(treeId, node);
        }

        if (isLeft) {
            node.parent.left = null;
        }
        else {
            node.parent.right = null;
        }
        return node;
    }

    private void rotateLeft(int treeId, TreeNode<K, V> node)
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
        if (node == getRoot(treeId)) {
            this.setRoot(treeId, rightChild);
        }
    }

    private void rotateRight(int treeId, TreeNode<K, V> node)
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
        if (node == this.getRoot(treeId)) {
            this.setRoot(treeId, leftChild);
        }
    }

    private void balanceInsert(int treeId, TreeNode<K, V> insertedNode, boolean isLeftInsert)
    {
        TreeNode<K, V> n = insertedNode;
        boolean isLeft = isLeftInsert;
        TreeNode<K, V> parent, grandParent;
        while (true) {
            //assert n.isRed;
            //assert n.parent.isRed;
            parent = n.parent;
            TreeNode<K, V> uncle = n.getUncle();
            grandParent = parent.parent;
            if (!(uncle != null && uncle.red)) {
                break;
            }
            //Parent and uncle is red
            parent.red = false;
            uncle.red = false;
            grandParent.red = true;
            if (grandParent == this.getRoot(treeId)) {
                grandParent.red = false;
                return;
            }
            else if (grandParent.parent != null && grandParent.parent.red) {
                n = grandParent;
                isLeft = grandParent.isLeftNode();
                continue;
            }
            return;
        }

        //Parent is red but uncle is black
        if (parent.isLeftNode() == isLeft) {
            //Parent and N are on the same side
            if (isLeft) {
                //Parent and N are on the left
                rotateRight(treeId, grandParent);
            }
            else {
                //Parent and N are on the right
                this.rotateLeft(treeId, grandParent);
            }
            parent.red = false;
            grandParent.red = true;
        }
        else { //rotate at twice
            if (isLeft) {
                //Parent is right and N is left
                this.rotateRight(treeId, parent);
                //Parent and N are on the right
                this.rotateLeft(treeId, grandParent);
            }
            else {
                //Parent is left and N is right
                this.rotateLeft(treeId, parent);
                //Parent and N are on the left
                this.rotateRight(treeId, grandParent);
            }
            n.red = false;
            grandParent.red = true;
        }
    }

    private void balanceRemove(int treeId, TreeNode<K, V> removedNode)
    {
        TreeNode<K, V> brother = removedNode.getBrother();
        TreeNode<K, V> parent = removedNode.parent;
        TreeNode<K, V> sl;
        TreeNode<K, V> sr;
        boolean slIsBlack;
        boolean srIsBlack;

        do {
            if (brother.red) {
                if (brother.isLeftNode()) {
                    this.rotateRight(treeId, parent);
                    swapColor(parent, brother);
                    brother = parent.left;
                }
                else {
                    this.rotateLeft(treeId, parent);
                    swapColor(parent, brother);
                    brother = parent.right;
                }
            }
            //next case2
            sl = brother.left;
            sr = brother.right;
            slIsBlack = sl == null || !sl.red;
            srIsBlack = sr == null || !sr.red;

            if (!slIsBlack || !srIsBlack) {
                break;
            }

            //case 2.1
            if (parent.red) {
                //2.11
                swapColor(parent, brother);
                return;
            }
            else {
                //case 2.12
                brother.red = true;
                if (this.getRoot(treeId) == parent) {
                    return;
                }
                brother = parent.getBrother();
                parent = parent.parent;
                //continue;
            }
        }
        while (true);

        //case 2.2
        if (brother.isLeftNode()) {
            if (slIsBlack) {
                rotateLeft(treeId, brother);
                swapColor(brother, sr);
                //next to case 2.2.1-1
                sl = brother;
                brother = brother.parent;
            }
            //case 2.2.1-1
            rotateRight(treeId, parent);
            swapColor(parent, brother);
            sl.red = false;
        }
        else {
            if (srIsBlack) {
                rotateRight(treeId, brother);
                swapColor(brother, sl);
                //next to case 2.2.1-2
                sr = brother;
                brother = brother.parent;
            }
            //case2.2.1-2
            rotateLeft(treeId, parent);
            swapColor(parent, brother);
            sr.red = false;
        }
    }

    private void swapColor(TreeNode<K, V> n1, TreeNode<K, V> n2)
    {
        boolean color = n1.red;
        n1.red = n2.red;
        n2.red = color;
    }

    public final Iterator<TreeNode<K, V>> iterator(TreeNode<K, V> root0)
    {
        return new Iterator<TreeNode<K, V>>()
        {
            private TreeNode<K, V> node = root0;
            private final LinkedList<TreeNode<K, V>> queue = new LinkedList<>();

            {
                addNext(root0);
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

    public final void clear(int treeId)
    {
        this.setRoot(treeId, null);
    }

    public final String toString(int treeId)
    {
        TreeNode<K, V> root = this.getRoot(treeId);
        return root == null ? "" : root.toString();
    }

    public abstract static class TreeNode<K, V>
            implements HashEntry<K, V>
    {
        private boolean red = true; //new node default color is red
        private TreeNode<K, V> left;
        private TreeNode<K, V> right;
        private TreeNode<K, V> parent;

        private TreeNode<K, V> hashDuplicated;  //liked list

        public abstract K getKey();

        public abstract V getValue();

        public abstract V setValue(V value);

        public abstract int getHash();

        private boolean isLeftNode()
        {
            return parent.left == this;
        }

        private TreeNode<K, V> getUncle()
        {
            TreeNode<K, V> grandParent = this.parent.parent;
            return parent.isLeftNode() ? grandParent.right : grandParent.left;
        }

        private TreeNode<K, V> getBrother()
        {
            if (this.isLeftNode()) {
                return parent.right;
            }
            else {
                return parent.left;
            }
        }

        public boolean isRed()
        {
            return red;
        }

        public TreeNode<K, V> getParent()
        {
            return parent;
        }

        @Override
        public String toString()
        {
            return toStringHelper(this)
                    .add("key", getKey())
                    .add("color", red ? "red" : "black")
                    .add("value", getValue())
                    .add("left", left.getKey())
                    .add("right", right.getKey())
                    .toString();
        }
    }
}
