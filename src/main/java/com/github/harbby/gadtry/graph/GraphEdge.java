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

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

/**
 * A directed edge {@code inNode -> outNode} carrying an optional value.
 */
public final class GraphEdge<N, E>
        implements Serializable
{
    private final GraphNode<N, E> inNode;
    private final GraphNode<N, E> outNode;
    private final E value;

    private GraphEdge(GraphNode<N, E> inNode, GraphNode<N, E> outNode, E value)
    {
        this.inNode = requireNonNull(inNode, "inNode is null");
        this.outNode = requireNonNull(outNode, "outNode is null");
        this.value = value;
    }

    public static <N, E> GraphEdge<N, E> of(GraphNode<N, E> inNode, GraphNode<N, E> outNode, E value)
    {
        return new GraphEdge<>(inNode, outNode, value);
    }

    public static <N, E> GraphEdge<N, E> of(GraphNode<N, E> inNode, GraphNode<N, E> outNode)
    {
        return of(inNode, outNode, null);
    }

    public GraphNode<N, E> getInNode()
    {
        return inNode;
    }

    public GraphNode<N, E> getOutNode()
    {
        return outNode;
    }

    public E getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("in", inNode)
                .add("out", outNode)
                .add("value", value)
                .toString();
    }
}
