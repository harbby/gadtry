/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.graph.impl;

import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultNode<T>
        implements Node<T>
{
    private final String id;
    private Map<String, Edge<T>> nextNodes = new HashMap<>();

    protected DefaultNode(String id) {this.id = id;}

    public static <E> DefaultNode<E> of(String id)
    {
        return new DefaultNode<E>(id);
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void addNextNode(Edge<T> edge)
    {
        nextNodes.put(edge.getOutNode().getId(), edge);
    }

    @Override
    public Collection<Edge<T>> nextNodes()
    {
        return nextNodes.values();
    }

    @Override
    public Optional<Edge<T>> getNextNode(String id)
    {
        return Optional.ofNullable(nextNodes.get(id));
    }

    @Override
    public void action(Node parentNode)
    {
        if (parentNode == null) { //根节点
            System.out.println("我是: 根节点" + toString());
        }
        else {  //叶子节点
            System.out.println("我是:" + toString() + "来自:" + parentNode.toString() + "-->" + toString());
        }
    }

    @Override
    public String toString()
    {
        return "node:" + getId();
    }
}
