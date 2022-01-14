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

public interface Route<N, E>
{
    public List<N> getIds();

    public Route.Builder<N, E> copy();

    public boolean containsLoop();

    public Deque<GraphEdge<N, E>> getEdges();

    public int size();

    public default GraphNode<N, E> getLastNode()
    {
        return getLastNode(0);
    }

    public GraphNode<N, E> getLastNode(int index);

    public GraphEdge<N, E> getLastEdge();

    public default N getLastNodeId()
    {
        return getLastNode().getValue();
    }

    public static <N, E> Builder<N, E> builder(GraphNode<N, E> begin)
    {
        return new Builder<>(begin);
    }

    public static class Builder<N, E>
    {
        private final GraphNode<N, E> begin;
        private final Deque<GraphEdge<N, E>> edges = new LinkedList<>();

        public Builder(GraphNode<N, E> begin)
        {
            this.begin = begin;
        }

        public Builder<N, E> add(GraphEdge<N, E> edge)
        {
            this.edges.add(edge);
            return this;
        }

        public Builder<N, E> addAll(Collection<GraphEdge<N, E>> edges)
        {
            this.edges.addAll(edges);
            return this;
        }

        public Route<N, E> create()
        {
            return new RouteImpl<>(begin, edges);
        }
    }
}
