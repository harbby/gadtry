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
package com.github.harbby.gadtry.graph.impl;

import com.github.harbby.gadtry.base.Iterators;
import com.github.harbby.gadtry.base.Lazys;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.Route;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public class RouteImpl<N, E>
        implements Route<N, E>
{
    private final GraphNode<N, E> begin;
    private final Deque<GraphEdge<N, E>> edges;
    private final Supplier<Boolean> containsLoop;
    private final Supplier<List<N>> nodeIds;

    public RouteImpl(GraphNode<N, E> begin, Deque<GraphEdge<N, E>> edges)
    {
        this.begin = begin;
        this.edges = edges;
        this.containsLoop = Lazys.of(() -> findLoop(begin, edges));
        this.nodeIds = Lazys.of(() -> {
            List<N> list = new ArrayList<>(this.size() + 1);
            list.add(begin.getValue());
            this.edges.forEach(erEdge -> list.add(erEdge.getOutNode().getValue()));
            return list;
        });
    }

    @Override
    public Route.Builder<N, E> copy()
    {
        return Route.builder(begin).addAll(this.edges);
    }

    @Override
    public List<N> getIds()
    {
        return nodeIds.get();
    }

    private static <N, E> boolean findLoop(GraphNode<N, E> begin, Deque<GraphEdge<N, E>> edges)
    {
        if (edges.isEmpty()) {
            return false;
        }
        GraphNode<N, E> lastNode = edges.getLast().getOutNode();
        N lastNodeValue = lastNode.getValue();
        if (!(begin instanceof GraphNode.RootNode) && Objects.equals(begin.getValue(), lastNodeValue)) {
            return true;
        }
        Iterator<GraphEdge<N, E>> iterator = edges.descendingIterator();
        iterator.next();
        while (iterator.hasNext()) {
            if (Objects.equals(lastNodeValue, iterator.next().getOutNode().getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsLoop()
    {
        return containsLoop.get();
    }

    @Override
    public Deque<GraphEdge<N, E>> getEdges()
    {
        return edges;
    }

    @Override
    public int size()
    {
        return edges.size();
    }

    @Override
    public GraphNode<N, E> getLastNode(int index)
    {
        if (this.size() == index) {
            return begin;
        }
        else if (this.size() > index) {
            Iterator<GraphEdge<N, E>> iterator = this.edges.descendingIterator();
            return Iterators.getFirst(iterator, index).getOutNode();
        }
        else {
            throw new NoSuchElementException(String.valueOf(index));
        }
    }

    @Override
    public GraphEdge<N, E> getLastEdge()
    {
        if (edges.isEmpty()) {
            throw new IllegalStateException("this Route only begin node");
        }
        return edges.getLast();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(begin, edges);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        RouteImpl<?, ?> other = (RouteImpl<?, ?>) obj;
        return Objects.equals(this.begin, other.begin) && Objects.equals(this.edges, other.edges);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("begin", begin)
                .add("route", getIds().stream().map(String::valueOf).collect(Collectors.joining("-")))
                .toString();
    }
}
