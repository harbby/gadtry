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

import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;
import com.github.harbby.gadtry.graph.Route;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public class RouteImpl<E, R>
        implements Route<E, R>
{
    private final Node<E, R> begin;
    private final List<Edge<E, R>> edges;

    public RouteImpl(Node<E, R> begin, List<Edge<E, R>> edges)
    {
        this.begin = begin;
        this.edges = ImmutableList.copy(edges);
    }

    @Override
    public List<String> getIds()
    {
        return ImmutableList.<String>builder().add(begin.getId())
                .addAll(edges.stream().map(x -> x.getOutNode().getId()).collect(Collectors.toList()))
                .build();
    }

    /**
     * 检测死递归
     *
     * @return true不存在死递归
     */
    @Override
    public boolean containsDeadRecursion()
    {
        List<String> names = this.getIds().subList(0, this.size() - 1); //
        return !names.contains(this.getEndNodeId());  //如果出现两次则无须继续递归查找
    }

    @Override
    public List<Edge<E, R>> getEdges()
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
    public Node<E, R> getLastNode()
    {
        return this.size() > 1 ? this.getEdges().get(this.size() - 2).getOutNode() : begin;
    }

    @Override
    public Node<E, R> getLastEdge()
    {
        return this.size() > 1 ? this.getEdges().get(this.size() - 2).getOutNode() : begin;
    }

    /**
     * 最后一个
     */
    @Override
    public Node<E, R> getEndNode()
    {
        return getEndEdge().getOutNode();
    }

    @Override
    public Edge<E, R> getEndEdge()
    {
        return edges.get(edges.size() - 1);
    }

    @Override
    public String getEndNodeId()
    {
        return getEndNode().getId();
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
