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

public abstract class SingleRedBlackTree<K, V>
        extends RedBlackTree<K, V>
{
    private TreeNode<K, V> root;

    @Override
    public TreeNode<K, V> getRoot(int treeId)
    {
        return root;
    }

    @Override
    public void setRoot(int treeId, TreeNode<K, V> root)
    {
        this.root = root;
    }

    public V get(Object key, int hash)
    {
        return get(root, key, hash);
    }

    public boolean containsKey(Object key, int hash)
    {
        return containsKey(root, key, hash);
    }

    public V put(K key, V value, int hash)
    {
        return this.put(0, key, value, hash);
    }

    public V remove(K key, int hash)
    {
        return this.remove(0, key, hash);
    }

    public void clear()
    {
        this.clear(0);
    }

    public Iterator<TreeNode<K, V>> iterator()
    {
        return this.iterator(root);
    }
}
