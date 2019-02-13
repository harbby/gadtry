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
import com.github.harbby.gadtry.graph.Graph;
import com.github.harbby.gadtry.graph.GraphUtil;
import com.github.harbby.gadtry.graph.Node;
import com.github.harbby.gadtry.graph.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * 默认graph
 * 采用普通左二叉树遍历法
 * default 采用普通串行遍历(非并行)
 */
public class DefaultGraph<E>
        implements Graph<E>
{
    private final Node<E> root;
    private final String name;
    private final Map<String, Node<E>> nodes;

    public DefaultGraph(
            final String name,
            Node<E> root,
            Map<String, Node<E>> nodes)
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
    public void run()
    {
        System.out.println("Traversing the entire graph from the root node...");
        serach(root, false);
    }

    @Override
    public void runParallel()
    {
        System.out.println("Traversing the entire graph from the root node...");
        serach(root, true);
    }

    @Override
    public List<Route> searchRuleRoute(String in, String out, Function<Route, Boolean> rule)
    {
        if (!nodes.containsKey(out)) {
            throw new IllegalArgumentException("NO SUCH ROUTE" + out);
        }

        return searchRuleRoute(in, rule).stream()
                .filter(x -> out.equals(x.getLastNodeId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Route> searchRuleRoute(String in, Function<Route, Boolean> rule)
    {
        Node<?> begin = requireNonNull(nodes.get(in), "NO SUCH Node " + in);
        List<Route> routes = new ArrayList<>();
        Route header = new Route(begin);

        searchRoute(routes, ImmutableList.copy(begin.nextNodes()), rule, header);

        return ImmutableList.copy(routes);
    }

    @Override
    public Route getRoute(String... nodeIds)
    {
        Node<?> begin = requireNonNull(nodes.get(nodeIds[0]), "NO SUCH Node " + nodeIds[0]);
        Route route = new Route(begin);
        for (int i = 1; i < nodeIds.length; i++) {
            Edge edge = begin.getNextNode(nodeIds[i]).orElseThrow(() -> new IllegalArgumentException("NO SUCH ROUTE"));
            route.add(edge);
            begin = edge.getOutNode();
        }
        return route;
    }

    @Override
    public void printShow()
    {
        List<String> builder = new ArrayList<>();
        builder.add("/");
        List<Node> nodes = root.nextNodes().stream().map(Edge::getOutNode).collect(Collectors.toList());
        GraphUtil.printShow(builder, nodes);
        builder.forEach(System.out::println);
    }

    @Override
    public void printShow(String id)
    {
        Node<?> firstNode = requireNonNull(nodes.get(id), "NO SUCH Node " + id);

        List<String> builder = new ArrayList<>();
        builder.add("/");

        GraphUtil.printShow(builder, firstNode);
        builder.forEach(System.out::println);
    }

    private static void searchRoute(List<Route> routes, List<Edge> edges, Function<Route, Boolean> rule, Route header)
    {
        for (Edge edge : edges) {
            Route newRoute = header.clone();
            Node<?> node = edge.getOutNode();
            newRoute.add(edge);

            if (rule.apply(newRoute)) {
                routes.add(newRoute);
                List<Edge> next = ImmutableList.copy(node.nextNodes());
                searchRoute(routes, next, rule, newRoute);
            }
        }
    }

    private static <E> void serach(Node<E> node, boolean parallel)
    {
        Collection<Edge<E>> nodes = node.nextNodes();
        Stream<Edge<E>> stream = nodes.stream();
        if (parallel) {
            stream = stream.parallel();
        }
        stream.forEach(x -> {
            x.getOutNode().action(node);
            serach(x.getOutNode(), parallel);
        });
    }
}
