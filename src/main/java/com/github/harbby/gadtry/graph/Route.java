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
package com.github.harbby.gadtry.graph;

import com.github.harbby.gadtry.collection.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Route
{
    private final Node begin;
    private final List<Edge> edges = new ArrayList<>();

    public Route(Node begin) {this.begin = begin;}

    public void add(Edge edge)
    {
        edges.add(edge);
    }

    public List<String> getIds()
    {
        return ImmutableList.<String>builder().add(begin.getId())
                .addAll(edges.stream().map(x -> x.getOutNode().getId()).collect(Collectors.toList()))
                .build();
    }

    public List<Edge> getEdges()
    {
        return edges;
    }

    public int size()
    {
        return edges.size();
    }

    public Node getLastNode()
    {
        return getLastEdge().getOutNode();
    }

    public Edge getLastEdge()
    {
        return edges.get(edges.size() - 1);
    }

    public String getLastNodeId()
    {
        return getLastNode().getId();
    }

    @Override
    public Route clone()
    {
        Route route = new Route(begin);
        route.edges.addAll(this.edges);
        return route;
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

        Route other = (Route) obj;
        return Objects.equals(this.begin, other.begin) && Objects.equals(this.edges, other.edges);
    }

    @Override
    public String toString()
    {
        //todo: use guava toStringHelper()
        Map<String, Object> string = new HashMap<>();
        string.put("begin", begin);
        string.put("route", String.join("-", getIds()));
        return "(" + this.getClass().getSimpleName() + ")" + string.toString();
    }
}
