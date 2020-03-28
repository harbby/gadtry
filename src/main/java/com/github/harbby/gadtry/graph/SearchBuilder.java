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

import com.github.harbby.gadtry.collection.mutable.MutableList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class SearchBuilder<E, R>
{
    public enum Optimizer
    {
        RECURSIVE_DEPTH_FIRST, //递归 深度优先 recursive_depth_first
        BREADTH_FIRST,   //广度优先 breadth_first
        DEPTH_FIRST   //深度优先 depth_first
    }

    private final Graph<E, R> graph;

    private Optimizer optimizer = Optimizer.DEPTH_FIRST;
    private Node<E, R> beginNode;
    private Node<E, R> endNode;
    private Function<Route<E, R>, Boolean> nextRule;
    private Function<SearchContext<E, R>, Boolean> globalRule = erSearchContext -> true;

    public SearchBuilder(Graph<E, R> graph, Node<E, R> root)
    {
        this.graph = requireNonNull(graph, "graph is null");
        this.beginNode = root;
    }

    public SearchBuilder<E, R> optimizer(Optimizer optimizer)
    {
        this.optimizer = requireNonNull(optimizer, "optimizer is null");
        return this;
    }

    public SearchBuilder<E, R> beginNode(String beginNodeId)
    {
        requireNonNull(beginNodeId, "beginNodeId is null");
        this.beginNode = graph.getNode(beginNodeId);
        return this;
    }

    public SearchBuilder<E, R> endNode(String endNodeId)
    {
        requireNonNull(endNodeId, "endNodeId is null");
        this.endNode = graph.getNode(endNodeId);
        return this;
    }

    public SearchBuilder<E, R> nextRule(Function<Route<E, R>, Boolean> nextRule)
    {
        this.nextRule = requireNonNull(nextRule, "nextRule is null");
        return this;
    }

    public SearchBuilder<E, R> globalRule(Function<SearchContext<E, R>, Boolean> globalRule)
    {
        this.globalRule = requireNonNull(globalRule, "globalRule is null");
        return this;
    }

    public SearchResult<E, R> search()
    {
        requireNonNull(nextRule, "nextRule is null");

        SearchContext<E, R> searchContext = new SearchContext<>(nextRule, globalRule);
        Route<E, R> begin = Route.builder(beginNode).create();
        final Deque<Route<E, R>> routes = new LinkedList<>();

        switch (optimizer) {
            case DEPTH_FIRST:
                searchByDepthFirst(routes, searchContext, begin);
                break;
            case BREADTH_FIRST:
                searchByBreadthFirst(routes, searchContext, begin);
                break;
            case RECURSIVE_DEPTH_FIRST:
            default:
                try {
                    searchByRecursiveDepthFirst(routes, searchContext, begin);
                }
                catch (RecursiveExitException ignored) {
                }
        }

        return new SearchResult<E, R>()
        {
            @Override
            public List<Route<E, R>> getRoutes()
            {
                if (endNode != null) {
                    return routes.stream()
                            .filter(x -> endNode.getId().equals(x.getLastNodeId()))
                            .collect(Collectors.toList());
                }
                else {
                    return MutableList.copy(routes);
                }
            }

            @Override
            public long getSearchStartTime()
            {
                return searchContext.getSearchStartTime();
            }

            @Override
            public int getFindNodeNumber()
            {
                return searchContext.getFindNodeNumber();
            }
        };
    }

    /*
     *  递归 深度优先
     * 如果深度过高可能 throws StackOverflowError
     * */
    private static <E, R> void searchByRecursiveDepthFirst(
            Deque<Route<E, R>> routes,
            SearchContext<E, R> context,
            Route<E, R> route)
    {
        for (Edge<E, R> edge : route.getLastNode().nextNodes()) {   //use stream.parallel();
            Route<E, R> newRoute = route.copy().add(edge).create();
            context.setLastRoute(newRoute);
            boolean next = context.getNextRule().apply(newRoute);
            if (next) {
                routes.add(newRoute);
            }
            if (!context.getGlobalRule().apply(context)) {
                throw new RecursiveExitException();
            }

            if (next) {
                searchByRecursiveDepthFirst(routes, context, newRoute);
            }
        }
    }

    private static class RecursiveExitException
            extends RuntimeException
    {
    }

    /**
     * 广度优先 Breadth first
     */
    private static <E, R> void searchByBreadthFirst(
            Deque<Route<E, R>> routes,
            SearchContext<E, R> context,
            Route<E, R> beginNode)
    {
        final Queue<Route<E, R>> nextNodes = new LinkedList<>();
        nextNodes.add(beginNode);

        Route<E, R> route;
        while ((route = nextNodes.poll()) != null) {
            for (Edge<E, R> edge : route.getLastNode().nextNodes()) {   //use stream.parallel();
                Route<E, R> newRoute = route.copy().add(edge).create();
                context.setLastRoute(newRoute);

                if (context.getNextRule().apply(newRoute)) {
                    routes.add(newRoute);
                    nextNodes.add(newRoute);
                }

                if (!context.getGlobalRule().apply(context)) {
                    nextNodes.clear();
                    return;
                }
            }
        }
    }

    /**
     * 深度优先 Depth first
     */
    private static <E, R> void searchByDepthFirst(
            Deque<Route<E, R>> routes,
            SearchContext<E, R> context,
            Route<E, R> beginNode)
    {
        final Deque<Route<E, R>> nextNodes = new LinkedList<>();  //Stack
        nextNodes.add(beginNode);

        Route<E, R> route;
        while ((route = nextNodes.pollLast()) != null) {
            for (Edge<E, R> edge : route.getLastNode().nextNodes()) {   //use stream.parallel();
                Route<E, R> newRoute = route.copy().add(edge).create();
                context.setLastRoute(newRoute);

                if (context.getNextRule().apply(newRoute)) {
                    routes.add(newRoute);
                    nextNodes.add(newRoute);
                }

                if (!context.getGlobalRule().apply(context)) {
                    nextNodes.clear();
                    return;
                }
            }
        }
    }
}
