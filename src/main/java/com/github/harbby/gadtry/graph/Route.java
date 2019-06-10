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
package com.github.harbby.gadtry.graph;

import com.github.harbby.gadtry.graph.impl.RouteImpl;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public interface Route<E, R>
{
    public List<String> getIds();

    public Route.Builder<E, R> copy();

    /**
     * 检测死递归
     *
     * @return true表示不存在死递归
     */
    public boolean findDeadLoop();

    public Deque<Edge<E, R>> getEdges();

    public int size();

    /**
     * @return return最后一个Node
     */
    public default Node<E, R> getLastNode()
    {
        return getLastNode(0);
    }

    public Node<E, R> getLastNode(int index);

    public Edge<E, R> getLastEdge();

    public default String getLastNodeId()
    {
        return getLastNode().getId();
    }

    public static <E, R> Builder<E, R> builder(Node<E, R> begin)
    {
        return new Builder<>(begin);
    }

    public static class Builder<E, R>
    {
        private final Node<E, R> begin;
        private final Deque<Edge<E, R>> edges = new LinkedList<>();

        public Builder(Node<E, R> begin)
        {
            this.begin = begin;
        }

        public Builder<E, R> add(Edge<E, R> edge)
        {
            this.edges.add(edge);
            return this;
        }

        public Builder<E, R> addAll(Collection<Edge<E, R>> edges)
        {
            this.edges.addAll(edges);
            return this;
        }

        public Route<E, R> create()
        {
            return new RouteImpl<>(begin, edges);
        }
    }
}
