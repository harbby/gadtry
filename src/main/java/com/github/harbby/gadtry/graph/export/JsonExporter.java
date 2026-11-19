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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static com.github.harbby.gadtry.graph.export.Escapes.jsonString;
import static java.util.Objects.requireNonNull;

/**
 * Writes {@code {"nodes":[{"id","label","attrs"}], "edges":[{"source","target","value"}]}}.
 * Has no runtime dependency.
 */
public class JsonExporter<N, E>
        extends StructuredExporter<N, E, JsonExporter<N, E>>
{
    private Function<? super N, ? extends Map<String, ?>> attrsSelector = node -> Collections.emptyMap();
    private boolean pretty = true;

    public JsonExporter(Graph<N, E> graph)
    {
        super(graph);
    }

    /**
     * Extra node attributes. Numbers, booleans and null are written as JSON literals, everything else as strings.
     */
    public JsonExporter<N, E> nodeAttrs(Function<? super N, ? extends Map<String, ?>> attrsSelector)
    {
        this.attrsSelector = requireNonNull(attrsSelector, "attrsSelector is null");
        return this;
    }

    public JsonExporter<N, E> pretty(boolean pretty)
    {
        this.pretty = pretty;
        return this;
    }

    @Override
    protected void write(Writer writer)
            throws IOException
    {
        String nl = pretty ? "\n" : "";
        String indent1 = pretty ? "  " : "";
        String indent2 = pretty ? "    " : "";
        String sep = pretty ? ", " : ",";
        String colon = pretty ? ": " : ":";

        writer.write("{" + nl);
        writer.write(indent1 + "\"nodes\"" + colon + "[" + nl);
        Iterator<GraphNode<N, E>> nodes = graph.nodes().iterator();
        while (nodes.hasNext()) {
            N node = nodes.next().getValue();
            writer.write(indent2 + "{\"id\"" + colon + jsonString(idOf(node)));
            writer.write(sep + "\"label\"" + colon + jsonString(labelOf(node)));
            writer.write(sep + "\"attrs\"" + colon);
            writeAttrs(writer, attrsSelector.apply(node), sep, colon);
            writer.write("}");
            writer.write(nodes.hasNext() ? "," + nl : nl);
        }
        writer.write(indent1 + "]," + nl);
        writer.write(indent1 + "\"edges\"" + colon + "[" + nl);
        Iterator<GraphEdge<N, E>> edges = graph.edges().iterator();
        while (edges.hasNext()) {
            GraphEdge<N, E> edge = edges.next();
            String value = edgeLabelOf(edge.getValue());
            writer.write(indent2 + "{\"source\"" + colon + jsonString(idOf(edge.getInNode().getValue())));
            writer.write(sep + "\"target\"" + colon + jsonString(idOf(edge.getOutNode().getValue())));
            writer.write(sep + "\"value\"" + colon + (value == null ? "null" : jsonString(value)));
            writer.write("}");
            writer.write(edges.hasNext() ? "," + nl : nl);
        }
        writer.write(indent1 + "]" + nl);
        writer.write("}" + nl);
    }

    private static void writeAttrs(Writer writer, Map<String, ?> attrs, String sep, String colon)
            throws IOException
    {
        writer.write("{");
        if (attrs != null) {
            boolean first = true;
            for (Map.Entry<String, ?> entry : attrs.entrySet()) {
                if (!first) {
                    writer.write(sep);
                }
                first = false;
                writer.write(jsonString(String.valueOf(entry.getKey())));
                writer.write(colon);
                writer.write(jsonValue(entry.getValue()));
            }
        }
        writer.write("}");
    }

    private static String jsonValue(Object value)
    {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return jsonString(value.toString());
            }
            return value.toString();
        }
        return jsonString(String.valueOf(value));
    }
}
