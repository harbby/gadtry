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

import static com.github.harbby.gadtry.graph.export.Escapes.xml;

/**
 * Writes the graph as a directed GraphML document with a {@code label} key for nodes
 * and a {@code value} key for edges. Has no runtime dependency.
 */
public class GraphMLExporter<N, E>
        extends StructuredExporter<N, E, GraphMLExporter<N, E>>
{
    public GraphMLExporter(Graph<N, E> graph)
    {
        super(graph);
    }

    @Override
    protected void write(Writer writer)
            throws IOException
    {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns"
                + " http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n");
        writer.write("  <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n");
        writer.write("  <key id=\"value\" for=\"edge\" attr.name=\"value\" attr.type=\"string\"/>\n");
        writer.write("  <graph id=\"G\" edgedefault=\"directed\">\n");
        for (GraphNode<N, E> node : graph.nodes()) {
            N value = node.getValue();
            writer.write("    <node id=\"" + xml(idOf(value)) + "\">");
            writer.write("<data key=\"label\">" + xml(labelOf(value)) + "</data>");
            writer.write("</node>\n");
        }
        int index = 0;
        for (GraphEdge<N, E> edge : graph.edges()) {
            writer.write("    <edge id=\"e" + index++ + "\" source=\"" + xml(idOf(edge.getInNode().getValue()))
                    + "\" target=\"" + xml(idOf(edge.getOutNode().getValue())) + "\"");
            String label = edgeLabelOf(edge.getValue());
            if (label == null) {
                writer.write("/>\n");
            }
            else {
                writer.write("><data key=\"value\">" + xml(label) + "</data></edge>\n");
            }
        }
        writer.write("  </graph>\n");
        writer.write("</graphml>\n");
    }
}
