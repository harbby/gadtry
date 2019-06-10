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
import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;
import com.github.harbby.gadtry.graph.Route;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public class RouteImpl<E, R>
        implements Route<E, R>
{
    private final Node<E, R> begin;
    private final Deque<Edge<E, R>> edges;
    private final Supplier<Boolean> findDeadLoop;   //如果出现两次则说明发现循环
    private final Supplier<List<String>> nodeIds;

    public RouteImpl(Node<E, R> begin, Deque<Edge<E, R>> edges)
    {
        this.begin = begin;
        this.edges = edges;
        this.findDeadLoop = Lazys.goLazy(() -> {
            Edge<E, R> lastEdge = getLastEdge();
            return begin.getId().equals(lastEdge.getOutNode().getId()) ||
                    edges.stream().anyMatch(erEdge -> erEdge != lastEdge && erEdge.getOutNode().getId()
                            .equals(getLastNode().getId())); //如果出现两次则无须继续递归查找
        });
        this.nodeIds = Lazys.goLazy(() -> {
            List<String> list = new ArrayList<>(this.size() + 1);
            list.add(begin.getId());
            this.edges.forEach(erEdge -> {
                list.add(erEdge.getOutNode().getId());
            });
            return list;
        });
    }

    @Override
    public Route.Builder<E, R> copy()
    {
        return Route.builder(begin).addAll(this.edges);
    }

    @Override
    public List<String> getIds()
    {
        return nodeIds.get();
    }

    /**
     * 检测死递归
     *
     * @return true不存在死递归
     */
    @Override
    public boolean findDeadLoop()
    {
        return findDeadLoop.get();
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
    public Node<E, R> getLastNode(int index)
    {
        Iterator<Edge<E, R>> iterator = this.edges.descendingIterator();

        if (this.size() == index) {
            return begin;
        }
        else if (this.size() > index) {
            return Iterators.getFirst(iterator, index).getOutNode();
        }
        else {
            throw new NoSuchElementException(String.valueOf(index));
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
