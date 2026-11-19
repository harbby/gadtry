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
package com.github.harbby.gadtry.graph.export;

import com.github.harbby.gadtry.graph.Graph;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Base class of the layout-free text formats (JSON, DOT, GraphML, Mermaid).
 *
 * @param <T> concrete exporter type, so the fluent setters keep returning it
 */
public abstract class StructuredExporter<N, E, T extends StructuredExporter<N, E, T>>
        extends GraphExporter<N, E>
{
    private Function<? super N, String> idSelector = String::valueOf;
    private Function<? super N, String> labelSelector = String::valueOf;
    private Function<? super E, String> edgeLabelSelector = value -> value == null ? null : String.valueOf(value);

    protected StructuredExporter(Graph<N, E> graph)
    {
        super(graph);
    }

    @SuppressWarnings("unchecked")
    protected final T self()
    {
        return (T) this;
    }

    /**
     * Node id written to the output. Must be unique per node; defaults to {@code String.valueOf(node)}.
     */
    public T idBy(Function<? super N, String> idSelector)
    {
        this.idSelector = requireNonNull(idSelector, "idSelector is null");
        return self();
    }

    /**
     * Human-readable node label; defaults to {@code String.valueOf(node)}.
     */
    public T labelBy(Function<? super N, String> labelSelector)
    {
        this.labelSelector = requireNonNull(labelSelector, "labelSelector is null");
        return self();
    }

    /**
     * Edge label; return {@code null} to omit it. Defaults to {@code String.valueOf(value)} for non-null values.
     */
    public T edgeLabelBy(Function<? super E, String> edgeLabelSelector)
    {
        this.edgeLabelSelector = requireNonNull(edgeLabelSelector, "edgeLabelSelector is null");
        return self();
    }

    protected final String idOf(N node)
    {
        return requireNonNull(idSelector.apply(node), "node id is null");
    }

    protected final String labelOf(N node)
    {
        String label = labelSelector.apply(node);
        return label == null ? "" : label;
    }

    protected final String edgeLabelOf(E value)
    {
        return edgeLabelSelector.apply(value);
    }
}
