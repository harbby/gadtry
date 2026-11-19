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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GraphExportTest
{
    private final Graph<String, Integer> graph = Graph.<String, Integer>builder()
            .addNode("a")
            .addEdge("a", "b", 1)
            .addEdge("b", "c\"x")
            .create();

    @Test
    public void jsonEscapesAndKeepsNullValues()
    {
        String json = graph.export(GraphFormats.json()).pretty(false).asString();
        Assertions.assertEquals("{\"nodes\":["
                + "{\"id\":\"a\",\"label\":\"a\",\"attrs\":{}},"
                + "{\"id\":\"b\",\"label\":\"b\",\"attrs\":{}},"
                + "{\"id\":\"c\\\"x\",\"label\":\"c\\\"x\",\"attrs\":{}}],"
                + "\"edges\":["
                + "{\"source\":\"a\",\"target\":\"b\",\"value\":\"1\"},"
                + "{\"source\":\"b\",\"target\":\"c\\\"x\",\"value\":null}]}", json);
    }

    @Test
    public void jsonNodeAttrsAndCustomIds()
    {
        String json = graph.export(GraphFormats.json())
                .pretty(false)
                .idBy(node -> "id_" + node.length())
                .nodeAttrs(node -> Collections.singletonMap("len", node.length()))
                .asString();
        Assertions.assertTrue(json.contains("{\"id\":\"id_3\",\"label\":\"c\\\"x\",\"attrs\":{\"len\":3}}"), json);
        Assertions.assertTrue(json.contains("{\"source\":\"id_1\",\"target\":\"id_3\",\"value\":null}"), json);
    }

    @Test
    public void dotOutput()
    {
        String dot = graph.export(GraphFormats.dot()).direction(Direction.TOP_TO_BOTTOM).asString();
        Assertions.assertEquals("digraph \"G\" {\n"
                + "  rankdir=TB;\n"
                + "  \"a\" [label=\"a\"];\n"
                + "  \"b\" [label=\"b\"];\n"
                + "  \"c\\\"x\" [label=\"c\\\"x\"];\n"
                + "  \"a\" -> \"b\" [label=\"1\"];\n"
                + "  \"b\" -> \"c\\\"x\";\n"
                + "}\n", dot);
    }

    @Test
    public void mermaidOutput()
    {
        String mermaid = graph.export(GraphFormats.mermaid()).asString();
        Assertions.assertEquals("flowchart LR\n"
                + "    n0[\"a\"]\n"
                + "    n1[\"b\"]\n"
                + "    n2[\"c#quot;x\"]\n"
                + "    n0 -->|\"1\"| n1\n"
                + "    n1 --> n2\n", mermaid);
    }

    @Test
    public void graphmlOutput()
    {
        String xml = graph.export(GraphFormats.graphml()).asString();
        Assertions.assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"));
        Assertions.assertTrue(xml.contains("<graph id=\"G\" edgedefault=\"directed\">"));
        Assertions.assertTrue(xml.contains("<node id=\"c&quot;x\"><data key=\"label\">c&quot;x</data></node>"));
        Assertions.assertTrue(xml.contains("<edge id=\"e0\" source=\"a\" target=\"b\"><data key=\"value\">1</data></edge>"));
        Assertions.assertTrue(xml.contains("<edge id=\"e1\" source=\"b\" target=\"c&quot;x\"/>"));
    }

    @Test
    public void writeToOutputStreamUsesUtf8AndDoesNotClose()
            throws IOException
    {
        Graph<String, Void> chinese = Graph.<String, Void>builder().addEdge("中文", "节点").create();
        TrackingOutputStream out = new TrackingOutputStream();
        chinese.export(GraphFormats.mermaid()).writeTo(out);
        Assertions.assertFalse(out.closed);
        Assertions.assertTrue(new String(out.toByteArray(), UTF_8).contains("n0[\"中文\"]"));

        // jackson closes its target by default; the exporter must still leave the stream open
        TrackingOutputStream canvasOut = new TrackingOutputStream();
        chinese.export(GraphFormats.canvas()).writeTo(canvasOut);
        Assertions.assertFalse(canvasOut.closed);
        Assertions.assertTrue(new String(canvasOut.toByteArray(), UTF_8).contains("中文"));
    }

    @Test
    public void saveCreatesParentDirectories(@TempDir Path dir)
            throws IOException
    {
        Path target = dir.resolve("a/b/graph.json");
        graph.export(GraphFormats.json()).save(target);
        Assertions.assertEquals(graph.export(GraphFormats.json()).asString(), new String(Files.readAllBytes(target), UTF_8));

        Path drawio = dir.resolve("c/graph.drawio");
        graph.export(GraphFormats.drawio()).save(drawio.toFile());
        Assertions.assertTrue(new String(Files.readAllBytes(drawio), UTF_8).contains("<mxfile"));
    }

    @Test
    public void customFormat()
    {
        String text = graph.export(g -> new GraphExporter<String, Integer>(g)
        {
            @Override
            protected void write(Writer writer)
                    throws IOException
            {
                writer.write(graph.nodeCount() + "/" + graph.edgeCount());
            }
        }).asString();
        Assertions.assertEquals("3/2", text);
    }

    private static final class TrackingOutputStream
            extends ByteArrayOutputStream
    {
        private boolean closed;

        @Override
        public void close()
        {
            closed = true;
        }
    }
}
