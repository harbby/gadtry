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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GraphNode<N, E>
        implements Serializable
{
    private final Map<N, GraphEdge<N, E>> nextNodes = new HashMap<>();
    private final N value;

    private GraphNode(N value)
    {
        this.value = value;
    }

    public static <N, E> GraphNode<N, E> of(N value)
    {
        return new GraphNode<>(value);
    }

    public N getValue()
    {
        return value;
    }

    public Collection<GraphEdge<N, E>> nextNodes()
    {
        return nextNodes.values();
    }

    public Optional<GraphEdge<N, E>> getNextNode(N id)
    {
        return Optional.ofNullable(nextNodes.get(id));
    }

    public void addNextNode(GraphNode<N, E> out, E edgeValue)
    {
        GraphEdge<N, E> edge = GraphEdge.of(out, edgeValue);
        nextNodes.put(edge.getOutNode().getValue(), edge);
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

    public static class RootNode<N, E>
            extends GraphNode<N, E>
    {
        RootNode()
        {
            super(null);
        }
    }
}
