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
import com.github.harbby.gadtry.graph.SearchBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * 默认graph
 * 采用普通左二叉树遍历法
 * default 采用普通串行遍历(非并行)
 */
public class DefaultGraph<N, E>
        implements Graph<N, E>
{
    private final GraphNode<N, E> root;
    private final Map<N, GraphNode<N, E>> nodes;

    public DefaultGraph(
            GraphNode<N, E> root,
            Map<N, GraphNode<N, E>> nodes)
    {
        this.root = root;
        this.nodes = nodes;
    }

    @Override
    public void addNode(N node)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEdge(N n1, N n2)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Route<N, E>> searchRuleRoute(N in, Function<Route<N, E>, Boolean> rule)
    {
        GraphNode<N, E> begin = requireNonNull(nodes.get(in), "NO SUCH Node " + in);

        return new SearchBuilder<>(this, begin)
                .mode(SearchBuilder.Mode.DEPTH_FIRST)
                .nextRule(rule)
                .search()
                .getRoutes();
    }

    @Override
    public List<Route<N, E>> searchRuleRoute(Function<Route<N, E>, Boolean> rule)
    {
        return new SearchBuilder<>(this, root)
                .mode(SearchBuilder.Mode.DEPTH_FIRST)
                .nextRule(rule)
                .search()
                .getRoutes();
    }

    @SafeVarargs
    @Override
    public final Route<N, E> getRoute(N... nodeIds)
    {
        GraphNode<N, E> begin = requireNonNull(nodes.get(nodeIds[0]), "NO SUCH Node " + nodeIds[0]);
        Route.Builder<N, E> route = Route.builder(begin);
        for (int i = 1; i < nodeIds.length; i++) {
            GraphEdge<N, E> edge = begin.getNextNode(nodeIds[i]).orElseThrow(() -> new IllegalArgumentException("NO SUCH ROUTE"));
            route.add(edge);
            begin = edge.getOutNode();
        }
        return route.create();
    }

    @Override
    public GraphNode<N, E> getNode(N id)
    {
        return requireNonNull(nodes.get(id), "NO SUCH Node " + id);
    }

    @Override
    public List<String> printShow()
    {
        List<GraphNode<?, ?>> nodes = root.nextNodes().stream().map(GraphEdge::getOutNode).collect(Collectors.toList());
        return GraphUtil.printShow(nodes);
    }

    @Override
    public Iterable<String> printShow(N id)
    {
        GraphNode<N, E> firstNode = requireNonNull(nodes.get(id), "NO SUCH Node " + id);
        List<String> builder = GraphUtil.printShow(firstNode);
        builder.forEach(System.out::println);
        return builder;
    }

    @Override
    public List<GraphNode<N, E>> findNode(Function<GraphNode<N, E>, Boolean> rule)
    {
        return nodes.values().stream().filter(rule::apply).collect(Collectors.toList());
    }

    @Override
    public SearchBuilder<N, E> search()
    {
        return new SearchBuilder<>(this, root);
    }
}
