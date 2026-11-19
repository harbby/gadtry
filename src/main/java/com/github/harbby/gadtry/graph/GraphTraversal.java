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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * Chained node traversal, created by {@link Graph#traverse()}.
 * <pre>{@code
 * for (GraphNode<String, Void> n : graph.traverse().from("a").bfs()) { ... }
 * List<String> order = graph.traverse().topological().values().collect(Collectors.toList());
 * graph.traverse().from("x").reverse().dfs().stream().filter(...).limit(10)...
 * }</pre>
 * Without {@link #from(Object[])} the whole graph is covered.
 * {@link #bfs()} and {@link #dfs()} are lazy; every node is visited at most once, so cycles are safe.
 */
public final class GraphTraversal<N, E>
{
    private final Graph<N, E> graph;
    private final List<GraphNode<N, E>> starts = new ArrayList<>();
    private boolean reverse;

    public GraphTraversal(Graph<N, E> graph)
    {
        this.graph = requireNonNull(graph, "graph is null");
    }

    /**
     * Start nodes of the traversal. May be called several times.
     *
     * @throws NoSuchElementException if a node does not exist
     */
    @SafeVarargs
    public final GraphTraversal<N, E> from(N... ids)
    {
        requireNonNull(ids, "ids is null");
        for (N id : ids) {
            starts.add(graph.getNode(id));
        }
        return this;
    }

    /**
     * Follow edges backwards (from a node to its predecessors).
     */
    public GraphTraversal<N, E> reverse()
    {
        this.reverse = true;
        return this;
    }

    /**
     * Breadth first order.
     */
    public NodeSequence<N, E> bfs()
    {
        final List<GraphNode<N, E>> seeds = seeds();
        final boolean reverse = this.reverse;
        return new NodeSequence<>(() -> new SearchIterator<>(seeds, reverse, false));
    }

    /**
     * Depth first pre-order.
     */
    public NodeSequence<N, E> dfs()
    {
        final List<GraphNode<N, E>> seeds = seeds();
        final boolean reverse = this.reverse;
        return new NodeSequence<>(() -> new SearchIterator<>(seeds, reverse, true));
    }

    /**
     * Topological order (Kahn). With {@link #from(Object[])} only the reachable sub graph is sorted.
     *
     * @throws IllegalStateException when iterated, if the (sub) graph contains a cycle
     */
    public NodeSequence<N, E> topological()
    {
        final List<GraphNode<N, E>> scope;
        if (starts.isEmpty()) {
            scope = new ArrayList<>(graph.nodes());
        }
        else {
            scope = new ArrayList<>();
            new SearchIterator<>(starts, reverse, false).forEachRemaining(scope::add);
        }
        final boolean reverse = this.reverse;
        return new NodeSequence<>(() -> topologicalSort(scope, reverse).iterator());
    }

    private List<GraphNode<N, E>> seeds()
    {
        if (!starts.isEmpty()) {
            return new ArrayList<>(starts);
        }
        Set<GraphNode<N, E>> seeds = new LinkedHashSet<>(reverse ? graph.sinks() : GraphUtil.entryNodes(graph));
        // nodes that are only reachable inside a cycle not covered above (e.g. reverse traversal of a pure cycle)
        seeds.addAll(graph.nodes());
        return new ArrayList<>(seeds);
    }

    private static <N, E> List<GraphNode<N, E>> neighbors(GraphNode<N, E> node, boolean reverse)
    {
        return reverse ? node.predecessors() : node.successors();
    }

    private static <N, E> List<GraphNode<N, E>> topologicalSort(List<GraphNode<N, E>> scope, boolean reverse)
    {
        Set<GraphNode<N, E>> inScope = new HashSet<>(scope);
        Map<GraphNode<N, E>, Integer> degrees = new HashMap<>();
        Deque<GraphNode<N, E>> ready = new ArrayDeque<>();
        for (GraphNode<N, E> node : scope) {
            int degree = 0;
            for (GraphNode<N, E> prev : neighbors(node, !reverse)) {
                if (inScope.contains(prev)) {
                    degree++;
                }
            }
            degrees.put(node, degree);
            if (degree == 0) {
                ready.add(node);
            }
        }
        List<GraphNode<N, E>> result = new ArrayList<>(scope.size());
        GraphNode<N, E> node;
        while ((node = ready.poll()) != null) {
            result.add(node);
            for (GraphNode<N, E> child : neighbors(node, reverse)) {
                Integer degree = degrees.get(child);
                if (degree != null) {
                    degrees.put(child, degree - 1);
                    if (degree == 1) {
                        ready.add(child);
                    }
                }
            }
        }
        if (result.size() != scope.size()) {
            throw new IllegalStateException("graph contains a cycle, topological order is undefined");
        }
        return result;
    }

    private static final class SearchIterator<N, E>
            implements Iterator<GraphNode<N, E>>
    {
        private final Iterator<GraphNode<N, E>> seeds;
        private final boolean reverse;
        private final boolean depthFirst;
        private final Deque<GraphNode<N, E>> pending = new ArrayDeque<>();
        private final Set<GraphNode<N, E>> visited = new HashSet<>();
        private GraphNode<N, E> nextNode;

        private SearchIterator(List<GraphNode<N, E>> seeds, boolean reverse, boolean depthFirst)
        {
            this.seeds = seeds.iterator();
            this.reverse = reverse;
            this.depthFirst = depthFirst;
        }

        @Override
        public boolean hasNext()
        {
            while (nextNode == null) {
                GraphNode<N, E> node = depthFirst ? pending.pollLast() : pending.pollFirst();
                if (node == null) {
                    if (!seeds.hasNext()) {
                        return false;
                    }
                    node = seeds.next();
                }
                if (!visited.add(node)) {
                    continue;
                }
                List<GraphNode<N, E>> children = neighbors(node, reverse);
                if (depthFirst) {
                    // push in reverse so that the first child is visited first
                    Collections.reverse(children);
                }
                for (GraphNode<N, E> child : children) {
                    if (!visited.contains(child)) {
                        pending.add(child);
                    }
                }
                nextNode = node;
            }
            return true;
        }

        @Override
        public GraphNode<N, E> next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            GraphNode<N, E> node = nextNode;
            nextNode = null;
            return node;
        }
    }

    /**
     * Re-iterable result of a traversal.
     */
    public static final class NodeSequence<N, E>
            implements Iterable<GraphNode<N, E>>
    {
        private final Supplier<Iterator<GraphNode<N, E>>> iteratorFactory;

        private NodeSequence(Supplier<Iterator<GraphNode<N, E>>> iteratorFactory)
        {
            this.iteratorFactory = iteratorFactory;
        }

        @Override
        public Iterator<GraphNode<N, E>> iterator()
        {
            return iteratorFactory.get();
        }

        public Stream<GraphNode<N, E>> stream()
        {
            return StreamSupport.stream(spliterator(), false);
        }

        /**
         * @return stream of node values (ids)
         */
        public Stream<N> values()
        {
            return stream().map(GraphNode::getValue);
        }

        public List<GraphNode<N, E>> toList()
        {
            return stream().collect(Collectors.toList());
        }
    }
}
