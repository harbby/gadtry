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

import com.github.harbby.gadtry.graph.Graph;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Default immutable graph implementation.
 */
public class DefaultGraph<N, E>
        implements Graph<N, E>
{
    private final Map<N, GraphNode<N, E>> nodes;
    private final List<GraphEdge<N, E>> edges;

    public DefaultGraph(Map<N, GraphNode<N, E>> nodes)
    {
        this.nodes = requireNonNull(nodes, "nodes is null");
        List<GraphEdge<N, E>> edgeList = new ArrayList<>();
        for (GraphNode<N, E> node : nodes.values()) {
            edgeList.addAll(node.outEdges());
        }
        this.edges = Collections.unmodifiableList(edgeList);
    }

    @Override
    public int nodeCount()
    {
        return nodes.size();
    }

    @Override
    public int edgeCount()
    {
        return edges.size();
    }

    @Override
    public Collection<GraphNode<N, E>> nodes()
    {
        return Collections.unmodifiableCollection(nodes.values());
    }

    @Override
    public List<GraphEdge<N, E>> edges()
    {
        return edges;
    }

    @Override
    public Optional<GraphNode<N, E>> findNode(N id)
    {
        return Optional.ofNullable(nodes.get(id));
    }

    @SafeVarargs
    @Override
    public final Route<N, E> getRoute(N... nodeIds)
    {
        requireNonNull(nodeIds, "nodeIds is null");
        if (nodeIds.length == 0) {
            throw new IllegalArgumentException("nodeIds is empty");
        }
        GraphNode<N, E> begin = getNode(nodeIds[0]);
        Route.Builder<N, E> route = Route.builder(begin);
        for (int i = 1; i < nodeIds.length; i++) {
            GraphEdge<N, E> edge = begin.getOutEdge(nodeIds[i]).orElseThrow(() -> new IllegalArgumentException("NO SUCH ROUTE"));
            route.add(edge);
            begin = edge.getOutNode();
        }
        return route.create();
    }

    @Override
    public List<String> printShow()
    {
        return GraphUtil.printShow(GraphUtil.entryNodes(this));
    }

    @Override
    public List<String> printShow(N id)
    {
        return GraphUtil.printShow(getNode(id));
    }
}
