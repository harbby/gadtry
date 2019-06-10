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

import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Graph;
import com.github.harbby.gadtry.graph.Node;
import com.github.harbby.gadtry.graph.Route;
import com.github.harbby.gadtry.graph.SearchBuilder;

import java.util.ArrayList;
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
public class DefaultGraph<E, R>
        implements Graph<E, R>
{
    private final Node<E, R> root;
    private final String name;
    private final Map<String, Node<E, R>> nodes;

    public DefaultGraph(
            final String name,
            Node<E, R> root,
            Map<String, Node<E, R>> nodes)
    {
        this.name = name;
        this.root = root;
        this.nodes = nodes;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public List<Route<E, R>> searchRuleRoute(String in, Function<Route<E, R>, Boolean> rule)
    {
        Node<E, R> begin = requireNonNull(nodes.get(in), "NO SUCH Node " + in);

        return new ArrayList<>(new SearchBuilder<>(this, begin)
                .optimizer(SearchBuilder.Optimizer.DEPTH_FIRST)
                .nextRule(rule)
                .search()
                .getRoutes());
    }

    @Override
    public List<Route<E, R>> searchRuleRoute(Function<Route<E, R>, Boolean> rule)
    {
        return new ArrayList<>(new SearchBuilder<>(this, root)
                .optimizer(SearchBuilder.Optimizer.DEPTH_FIRST)
                .nextRule(rule)
                .search()
                .getRoutes());
    }

    @Override
    public Route<E, R> getRoute(String... nodeIds)
    {
        Node<E, R> begin = requireNonNull(nodes.get(nodeIds[0]), "NO SUCH Node " + nodeIds[0]);
        Route.Builder<E, R> route = Route.builder(begin);
        for (int i = 1; i < nodeIds.length; i++) {
            Edge<E, R> edge = begin.getNextNode(nodeIds[i]).orElseThrow(() -> new IllegalArgumentException("NO SUCH ROUTE"));
            route.add(edge);
            begin = edge.getOutNode();
        }
        return route.create();
    }

    @Override
    public Node<E, R> getNode(String id)
    {
        return requireNonNull(nodes.get(id), "NO SUCH Node " + id);
    }

    @Override
    public List<String> printShow()
    {
        List<String> builder = new ArrayList<>();
        builder.add("/");
        List<Node> nodes = root.nextNodes().stream().map(Edge::getOutNode).collect(Collectors.toList());
        GraphUtil.printShow(builder, nodes);
        //builder.forEach(System.out::println);
        return builder;
    }

    @Override
    public Iterable<String> printShow(String id)
    {
        Node<E, R> firstNode = requireNonNull(nodes.get(id), "NO SUCH Node " + id);

        List<String> builder = new ArrayList<>();
        builder.add("/");

        GraphUtil.printShow(builder, firstNode);
        builder.forEach(System.out::println);
        return builder;
    }

    @Override
    public List<Node<E, R>> findNode(Function<Node<E, R>, Boolean> rule)
    {
        return nodes.values().stream().filter(rule::apply).collect(Collectors.toList());
    }

    @Override
    public SearchBuilder<E, R> search()
    {
        return new SearchBuilder<>(this, root);
    }
}
