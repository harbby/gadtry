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
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static com.github.harbby.gadtry.graph.export.Escapes.mermaidLabel;
import static java.util.Objects.requireNonNull;

/**
 * Writes the graph as a mermaid {@code flowchart}. Mermaid ids only allow a restricted character set,
 * so node ids are generated ({@code n0, n1, ...}) in node order and {@link #idBy} is not used;
 * the label carries the text. Has no runtime dependency.
 */
public class MermaidExporter<N, E>
        extends StructuredExporter<N, E, MermaidExporter<N, E>>
{
    private Direction direction = Direction.LEFT_TO_RIGHT;

    public MermaidExporter(Graph<N, E> graph)
    {
        super(graph);
    }

    public MermaidExporter<N, E> direction(Direction direction)
    {
        this.direction = requireNonNull(direction, "direction is null");
        return this;
    }

    @Override
    protected void write(Writer writer)
            throws IOException
    {
        Map<N, String> ids = new HashMap<>();
        writer.write("flowchart " + direction.code() + "\n");
        for (GraphNode<N, E> node : graph.nodes()) {
            N value = node.getValue();
            String id = "n" + ids.size();
            ids.put(value, id);
            writer.write("    " + id + "[\"" + mermaidLabel(labelOf(value)) + "\"]\n");
        }
        for (GraphEdge<N, E> edge : graph.edges()) {
            writer.write("    " + ids.get(edge.getInNode().getValue()) + " -->");
            String label = edgeLabelOf(edge.getValue());
            if (label != null) {
                writer.write("|\"" + mermaidLabel(label) + "\"|");
            }
            writer.write(" " + ids.get(edge.getOutNode().getValue()) + "\n");
        }
    }
}
