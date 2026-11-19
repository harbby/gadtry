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

import com.github.harbby.gadtry.graph.impl.GraphUtil;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Route search. Without {@link #beginNode(Object)} every entry node of the graph is used as a start,
 * and its single-node route is offered to {@code nextRule} as well.
 */
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
    private Predicate<Route<N, E>> nextRule;
    private Predicate<SearchContext<N, E>> globalRule = context -> true;

    public SearchBuilder(Graph<N, E> graph)
    {
        this.graph = requireNonNull(graph, "graph is null");
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

    public SearchBuilder<N, E> nextRule(Predicate<Route<N, E>> nextRule)
    {
        this.nextRule = requireNonNull(nextRule, "nextRule is null");
        return this;
    }

    public SearchBuilder<N, E> globalRule(Predicate<SearchContext<N, E>> globalRule)
    {
        this.globalRule = requireNonNull(globalRule, "globalRule is null");
        return this;
    }

    public SearchResult<N, E> search()
    {
        requireNonNull(nextRule, "nextRule is null");

        SearchContext<N, E> searchContext = new SearchContext<>(nextRule, globalRule);
        final LinkedList<Route<N, E>> routes = new LinkedList<>();
        final List<Route<N, E>> starts = new ArrayList<>();
        boolean stopped = false;
        if (beginNode != null) {
            starts.add(Route.builder(beginNode).create());
        }
        else {
            stopped = !offerEntryRoutes(starts, routes, searchContext);
        }

        if (!stopped) {
            switch (mode) {
                case DEPTH_FIRST:
                    searchByDepthFirst(routes, searchContext, starts);
                    break;
                case BREADTH_FIRST:
                    searchByBreadthFirst(routes, searchContext, starts);
                    break;
                case RECURSIVE_DEPTH_FIRST:
                default:
                    try {
                        for (Route<N, E> start : starts) {
                            searchByRecursiveDepthFirst(routes, searchContext, start);
                        }
                    }
                    catch (RecursiveExitException ignored) {
                    }
            }
        }

        final GraphNode<N, E> end = this.endNode;
        return new SearchResult<N, E>()
        {
            @Override
            public List<Route<N, E>> getRoutes()
            {
                if (end != null) {
                    return routes.stream()
                            .filter(x -> Objects.equals(end.getValue(), x.getLastNodeId()))
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

    /**
     * @return false if the global rule stopped the search
     */
    private boolean offerEntryRoutes(List<Route<N, E>> starts, Deque<Route<N, E>> routes, SearchContext<N, E> context)
    {
        for (GraphNode<N, E> entry : GraphUtil.entryNodes(graph)) {
            Route<N, E> route = Route.builder(entry).create();
            context.setLastRoute(route);
            if (context.getNextRule().test(route)) {
                routes.add(route);
                starts.add(route);
            }
            if (!context.getGlobalRule().test(context)) {
                return false;
            }
        }
        return true;
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
        for (GraphEdge<N, E> edge : route.getLastNode().outEdges()) {
            Route<N, E> newRoute = route.copy().add(edge).create();
            context.setLastRoute(newRoute);
            boolean next = context.getNextRule().test(newRoute);
            if (next) {
                routes.add(newRoute);
            }
            if (!context.getGlobalRule().test(context)) {
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
        private RecursiveExitException()
        {
            super(null, null, false, false);
        }
    }

    /**
     * 广度优先 Breadth first
     */
    private static <N, E> void searchByBreadthFirst(
            Deque<Route<N, E>> routes,
            SearchContext<N, E> context,
            List<Route<N, E>> starts)
    {
        final Queue<Route<N, E>> nextNodes = new LinkedList<>(starts);

        Route<N, E> route;
        while ((route = nextNodes.poll()) != null) {
            for (GraphEdge<N, E> edge : route.getLastNode().outEdges()) {
                Route<N, E> newRoute = route.copy().add(edge).create();
                context.setLastRoute(newRoute);

                if (context.getNextRule().test(newRoute)) {
                    routes.add(newRoute);
                    nextNodes.add(newRoute);
                }

                if (!context.getGlobalRule().test(context)) {
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
            List<Route<N, E>> starts)
    {
        final Deque<Route<N, E>> nextNodes = new LinkedList<>(starts);  //Stack

        Route<N, E> route;
        while ((route = nextNodes.pollLast()) != null) {
            for (GraphEdge<N, E> edge : route.getLastNode().outEdges()) {
                Route<N, E> newRoute = route.copy().add(edge).create();
                context.setLastRoute(newRoute);

                if (context.getNextRule().test(newRoute)) {
                    routes.add(newRoute);
                    nextNodes.add(newRoute);
                }

                if (!context.getGlobalRule().test(context)) {
                    return;
                }
            }
        }
    }
}
