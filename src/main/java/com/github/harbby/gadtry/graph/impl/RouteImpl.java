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
import com.github.harbby.gadtry.collection.mutable.MutableList;
import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;
import com.github.harbby.gadtry.graph.Route;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public class RouteImpl<E, R>
        implements Route<E, R>
{
    private final Node<E, R> begin;
    private final Deque<Edge<E, R>> edges;

    public RouteImpl(Node<E, R> begin, Deque<Edge<E, R>> edges)
    {
        this.begin = begin;
        this.edges = edges;
    }

    @Override
    public Route.Builder<E, R> copy()
    {
        return Route.builder(begin).addAll(this.edges);
    }

    @Override
    public List<String> getIds()
    {
        return MutableList.<String>builder().add(begin.getId())
                .addAll(edges.stream().map(x -> x.getOutNode().getId()).collect(Collectors.toList()))
                .build();
    }

    /**
     * 检测死递归
     *
     * @return true不存在死递归
     */
    @Override
    public boolean checkDeadLoop()
    {
        List<String> names = this.getIds().subList(0, this.size() - 1); //
        return !names.contains(this.getLastNodeId());  //如果出现两次则无须继续递归查找
    }

    @Override
    public Deque<Edge<E, R>> getEdges()
    {
        return edges;
    }

    @Override
    public int size()
    {
        return edges.size();
    }

    /**
     * 上一个
     */
    @Override
    public Node<E, R> getLastNode(int n)
    {
        Iterator<Edge<E, R>> iterator = this.edges.descendingIterator();

        if (this.size() == n) {
            return begin;
        }
        else if (this.size() > n) {
            return Iterators.getFirst(iterator, n).getOutNode();
        }
        else {
            throw new NoSuchElementException(String.valueOf(n));
        }
    }

    @Override
    public Edge<E, R> getLastEdge()
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

        RouteImpl other = (RouteImpl) obj;
        return Objects.equals(this.begin, other.begin) && Objects.equals(this.edges, other.edges);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("begin", begin)
                .add("route", String.join("-", getIds()))
                .toString();
    }
}
