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

import com.github.harbby.gadtry.graph.export.GraphExporter;
import com.github.harbby.gadtry.graph.export.GraphFormat;
import com.github.harbby.gadtry.graph.impl.DefaultGraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Immutable directed graph. Build instances with {@link #builder()}.
 * <pre>{@code
 * for (GraphNode<N, E> node : graph) { ... }
 * for (GraphEdge<N, E> edge : graph.edges()) { edge.getInNode(); edge.getOutNode(); edge.getValue(); }
 * graph.forEachEdge((from, to, value) -> ...);
 * graph.traverse().from("a").bfs().values().forEach(...);
 * }</pre>
 *
 * @param <N> node value type, used as the node id
 * @param <E> edge value type
 */
public interface Graph<N, E>
        extends Iterable<GraphNode<N, E>>, Serializable
{
    int nodeCount();

    int edgeCount();

    /**
     * @return all nodes in insertion order (read-only)
     */
    Collection<GraphNode<N, E>> nodes();

    /**
     * @return all edges, grouped by source node in node insertion order (read-only)
     */
    List<GraphEdge<N, E>> edges();

    @Override
    default Iterator<GraphNode<N, E>> iterator()
    {
        return nodes().iterator();
    }

    default Stream<GraphNode<N, E>> nodeStream()
    {
        return nodes().stream();
    }

    default Stream<GraphEdge<N, E>> edgeStream()
    {
        return edges().stream();
    }

    default void forEachEdge(EdgeConsumer<? super N, ? super E> consumer)
    {
        requireNonNull(consumer, "consumer is null");
        for (GraphEdge<N, E> edge : edges()) {
            consumer.accept(edge.getInNode().getValue(), edge.getOutNode().getValue(), edge.getValue());
        }
    }

    Optional<GraphNode<N, E>> findNode(N id);

    /**
     * @throws NoSuchElementException if the node does not exist
     */
    default GraphNode<N, E> getNode(N id)
    {
        return findNode(id).orElseThrow(() -> new NoSuchElementException("NO SUCH Node " + id));
    }

    default boolean containsNode(N id)
    {
        return findNode(id).isPresent();
    }

    default Optional<GraphEdge<N, E>> findEdge(N from, N to)
    {
        return findNode(from).flatMap(node -> node.getOutEdge(to));
    }

    default boolean containsEdge(N from, N to)
    {
        return findEdge(from, to).isPresent();
    }

    default List<GraphNode<N, E>> findNodes(Predicate<? super GraphNode<N, E>> rule)
    {
        requireNonNull(rule, "rule is null");
        return nodeStream().filter(rule).collect(Collectors.toList());
    }

    /**
     * @return nodes without incoming edges, in insertion order
     */
    default List<GraphNode<N, E>> sources()
    {
        return findNodes(GraphNode::isSource);
    }

    /**
     * @return nodes without outgoing edges, in insertion order
     */
    default List<GraphNode<N, E>> sinks()
    {
        return findNodes(GraphNode::isSink);
    }

    default List<GraphNode<N, E>> successors(N id)
    {
        return getNode(id).successors();
    }

    default List<GraphNode<N, E>> predecessors(N id)
    {
        return getNode(id).predecessors();
    }

    default int outDegree(N id)
    {
        return getNode(id).outDegree();
    }

    default int inDegree(N id)
    {
        return getNode(id).inDegree();
    }

    /**
     * Chained node traversal: {@code graph.traverse().from(x).bfs() | dfs() | topological()}.
     */
    default GraphTraversal<N, E> traverse()
    {
        return new GraphTraversal<>(this);
    }

    /**
     * Route (path) search. Without {@code beginNode} the whole graph is searched.
     */
    default SearchBuilder<N, E> search()
    {
        return new SearchBuilder<>(this);
    }

    /**
     * Searches all routes starting at every entry node of the graph.
     * The single-node route of each entry node is offered to {@code rule} too.
     */
    default List<Route<N, E>> searchRuleRoute(Predicate<Route<N, E>> rule)
    {
        return search().nextRule(rule).search().getRoutes();
    }

    default List<Route<N, E>> searchRuleRoute(N in, Predicate<Route<N, E>> rule)
    {
        return search().beginNode(in).nextRule(rule).search().getRoutes();
    }

    default List<Route<N, E>> searchRuleRoute(N in, N out, Predicate<Route<N, E>> rule)
    {
        return search().beginNode(in).endNode(out).nextRule(rule).search().getRoutes();
    }

    /**
     * @throws IllegalArgumentException if the ids do not form a connected route
     */
    Route<N, E> getRoute(N... ids);

    /**
     * @return tree-style text of the whole graph
     */
    List<String> printShow();

    /**
     * @return tree-style text of the graph reachable from {@code id}
     */
    List<String> printShow(N id);

    /**
     * Single export entry point, e.g.
     * {@code graph.export(GraphFormats.json()).save(path)} or {@code graph.export(GraphFormats.canvas()).writeTo(out)}.
     * A custom format can be passed as a constructor reference: {@code graph.export(MyExporter::new)}.
     */
    default <X extends GraphExporter<N, E>> X export(GraphFormat<N, E, X> format)
    {
        requireNonNull(format, "format is null");
        return requireNonNull(format.create(this), "format created a null exporter");
    }

    static <N, E> GraphBuilder<N, E> builder()
    {
        return new GraphBuilder<>();
    }

    @FunctionalInterface
    interface EdgeConsumer<N, E>
    {
        void accept(N from, N to, E value);
    }

    class GraphBuilder<N, E>
    {
        private Map<N, GraphNode<N, E>> nodes = new LinkedHashMap<>();
        // set once create() hands the current nodes to a graph; the next change copies them first
        private boolean shared;

        public GraphBuilder<N, E> addNode(N node)
        {
            requireNonNull(node, "node is null");
            computeIfAbsent(node);
            return this;
        }

        /**
         * Adds {@code from -> to}. Adding the same pair again replaces the edge value.
         */
        public GraphBuilder<N, E> addEdge(N from, N to, E edgeValue)
        {
            requireNonNull(from, "from is null");
            requireNonNull(to, "to is null");
            ensureWritable();
            GraphNode.connect(computeIfAbsent(from), computeIfAbsent(to), edgeValue);
            return this;
        }

        public GraphBuilder<N, E> addEdge(N from, N to)
        {
            return addEdge(from, to, null);
        }

        /**
         * Creates a new immutable graph. The builder can keep being used afterwards
         * without affecting graphs it already created.
         */
        public Graph<N, E> create()
        {
            shared = true;
            return new DefaultGraph<>(nodes);
        }

        private GraphNode<N, E> computeIfAbsent(N id)
        {
            GraphNode<N, E> node = nodes.get(id);
            if (node == null) {
                ensureWritable();
                node = GraphNode.of(id);
                nodes.put(id, node);
            }
            return node;
        }

        private void ensureWritable()
        {
            if (!shared) {
                return;
            }
            Map<N, GraphNode<N, E>> copy = new LinkedHashMap<>();
            for (N id : nodes.keySet()) {
                copy.put(id, GraphNode.of(id));
            }
            for (GraphNode<N, E> node : nodes.values()) {
                GraphNode<N, E> from = copy.get(node.getValue());
                for (GraphEdge<N, E> edge : node.outEdges()) {
                    GraphNode.connect(from, copy.get(edge.getOutNode().getValue()), edge.getValue());
                }
            }
            nodes = copy;
            shared = false;
        }
    }
}
