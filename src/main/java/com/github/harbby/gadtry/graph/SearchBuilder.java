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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class SearchBuilder<N, E>
{
    public enum Mode
    {
        RECURSIVE_DEPTH_FIRST, //递归 深度优先 recursive_depth_first
        BREADTH_FIRST,   //广度优先 breadth_first
        DEPTH_FIRST   //深度优先 depth_first
    }

    private final Graph<N, E> graph;

    private Mode mode = Mode.DEPTH_FIRST;
    private GraphNode<N, E> beginNode;
    private GraphNode<N, E> endNode;
    private Function<Route<N, E>, Boolean> nextRule;
    private Function<SearchContext<N, E>, Boolean> globalRule = erSearchContext -> true;

    public SearchBuilder(Graph<N, E> graph, GraphNode<N, E> root)
    {
        this.graph = requireNonNull(graph, "graph is null");
        this.beginNode = root;
    }

    public SearchBuilder<N, E> mode(Mode mode)
    {
        this.mode = requireNonNull(mode, "mode is null");
        return this;
    }

    public SearchBuilder<N, E> beginNode(N beginNodeId)
    {
        requireNonNull(beginNodeId, "beginNodeId is null");
        this.beginNode = graph.getNode(beginNodeId);
        return this;
    }

    public SearchBuilder<N, E> endNode(N endNodeId)
    {
        requireNonNull(endNodeId, "endNodeId is null");
        this.endNode = graph.getNode(endNodeId);
        return this;
    }

    public SearchBuilder<N, E> nextRule(Function<Route<N, E>, Boolean> nextRule)
    {
        this.nextRule = requireNonNull(nextRule, "nextRule is null");
        return this;
    }

    public SearchBuilder<N, E> globalRule(Function<SearchContext<N, E>, Boolean> globalRule)
    {
        this.globalRule = requireNonNull(globalRule, "globalRule is null");
        return this;
    }

    public SearchResult<N, E> search()
    {
        requireNonNull(nextRule, "nextRule is null");

        SearchContext<N, E> searchContext = new SearchContext<>(nextRule, globalRule);
        Route<N, E> begin = Route.builder(beginNode).create();
        final LinkedList<Route<N, E>> routes = new LinkedList<>();

        switch (mode) {
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

        return new SearchResult<N, E>()
        {
            @Override
            public List<Route<N, E>> getRoutes()
            {
                if (endNode != null) {
                    return routes.stream()
                            .filter(x -> Objects.equals(endNode.getValue(), x.getLastNodeId()))
                            .collect(Collectors.toList());
                }
                else {
                    return routes;
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
    private static <N, E> void searchByRecursiveDepthFirst(
            Deque<Route<N, E>> routes,
            SearchContext<N, E> context,
            Route<N, E> route)
    {
        for (GraphEdge<N, E> edge : route.getLastNode().nextNodes()) {   //use stream.parallel();
            Route<N, E> newRoute = route.copy().add(edge).create();
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
    private static <N, E> void searchByBreadthFirst(
            Deque<Route<N, E>> routes,
            SearchContext<N, E> context,
            Route<N, E> beginNode)
    {
        final Queue<Route<N, E>> nextNodes = new LinkedList<>();
        nextNodes.add(beginNode);

        Route<N, E> route;
        while ((route = nextNodes.poll()) != null) {
            for (GraphEdge<N, E> edge : route.getLastNode().nextNodes()) {   //use stream.parallel();
                Route<N, E> newRoute = route.copy().add(edge).create();
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
     * Depth first
     */
    private static <N, E> void searchByDepthFirst(
            Deque<Route<N, E>> routes,
            SearchContext<N, E> context,
            Route<N, E> beginNode)
    {
        final Deque<Route<N, E>> nextNodes = new LinkedList<>();  //Stack
        nextNodes.add(beginNode);

        Route<N, E> route;
        while ((route = nextNodes.pollLast()) != null) {
            for (GraphEdge<N, E> edge : route.getLastNode().nextNodes()) {   //use stream.parallel();
                Route<N, E> newRoute = route.copy().add(edge).create();
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
