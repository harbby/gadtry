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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A vertex of a {@link Graph}. Knows both its outgoing and incoming edges.
 * Instances are created and wired by {@link Graph.GraphBuilder}; the public API is read-only.
 */
public final class GraphNode<N, E>
        implements Serializable
{
    // HashMap keeps the historical iteration order that search results depend on
    private final Map<N, GraphEdge<N, E>> outEdges = new HashMap<>();
    // a plain list keeps the per-edge memory cost low on large graphs
    private final List<GraphEdge<N, E>> inEdges = new ArrayList<>(0);
    private final N value;

    private GraphNode(N value)
    {
        this.value = value;
    }

    public static <N, E> GraphNode<N, E> of(N value)
    {
        return new GraphNode<>(value);
    }

    /**
     * Creates the edge {@code from -> to}, replacing any existing edge between the same pair.
     */
    static <N, E> GraphEdge<N, E> connect(GraphNode<N, E> from, GraphNode<N, E> to, E edgeValue)
    {
        requireNonNull(from, "from is null");
        requireNonNull(to, "to is null");
        GraphEdge<N, E> edge = GraphEdge.of(from, to, edgeValue);
        GraphEdge<N, E> replaced = from.outEdges.put(to.value, edge);
        if (replaced != null) {
            to.inEdges.remove(replaced);
        }
        to.inEdges.add(edge);
        return edge;
    }

    public N getValue()
    {
        return value;
    }

    public Collection<GraphEdge<N, E>> outEdges()
    {
        return Collections.unmodifiableCollection(outEdges.values());
    }

    public Collection<GraphEdge<N, E>> inEdges()
    {
        return Collections.unmodifiableList(inEdges);
    }

    public Optional<GraphEdge<N, E>> getOutEdge(N target)
    {
        return Optional.ofNullable(outEdges.get(target));
    }

    public Optional<GraphEdge<N, E>> getInEdge(N source)
    {
        for (GraphEdge<N, E> edge : inEdges) {
            if (Objects.equals(edge.getInNode().value, source)) {
                return Optional.of(edge);
            }
        }
        return Optional.empty();
    }

    public List<GraphNode<N, E>> successors()
    {
        return outEdges.values().stream().map(GraphEdge::getOutNode).collect(Collectors.toList());
    }

    public List<GraphNode<N, E>> predecessors()
    {
        return inEdges.stream().map(GraphEdge::getInNode).collect(Collectors.toList());
    }

    public int outDegree()
    {
        return outEdges.size();
    }

    public int inDegree()
    {
        return inEdges.size();
    }

    /**
     * @return true if no edge points to this node
     */
    public boolean isSource()
    {
        return inEdges.isEmpty();
    }

    /**
     * @return true if this node has no outgoing edge
     */
    public boolean isSink()
    {
        return outEdges.isEmpty();
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }
}
