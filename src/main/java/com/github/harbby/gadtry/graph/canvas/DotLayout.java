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
package com.github.harbby.gadtry.graph.canvas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

public class DotLayout<N, E, N0, E0>
        extends ProcessOptimizer<N, E, N0, E0>
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String temp = "digraph G {\n {edges} \n}";

    public DotLayout(SaveFileBuilder<N, E, N0, E0> saveFileBuilder)
    {
        super(saveFileBuilder);
    }

    @Override
    public SaveFileBuilder.CanvasGraphPO<N0, E0> optimize(GraphNode<N, E> root, int nodeNumber)
    {
        SaveFileBuilder.CanvasGraphPO<N0, E0> canvasGraphPO = new SaveFileBuilder.CanvasGraphPO<>();
        Queue<GraphNode<N, E>> stack = new LinkedList<>();
        stack.add(root);

        final Map<GraphNode<N, E>, Integer> loopedCheck = new HashMap<>();
        StringBuilder dotBuilder = new StringBuilder("digraph G {\n");
        dotBuilder.append("  rankdir=LR;\n");
        // add 孤立节点
        for (GraphEdge<N, E> edge : root.nextNodes()) {
            GraphNode<N, E> child = edge.getOutNode();
            if (child.nextNodes().isEmpty()) {
                Integer childId = loopedCheck.computeIfAbsent(child, k -> loopedCheck.size());
                dotBuilder.append(String.format("  \"%s\";\n", childId));
            }
        }

        GraphNode<N, E> it;
        while ((it = stack.poll()) != null) {
            for (GraphEdge<N, E> edge : it.nextNodes()) {
                GraphNode<N, E> child = edge.getOutNode();
                Integer childId = loopedCheck.computeIfAbsent(child, k -> {
                    stack.add(child);
                    return loopedCheck.size();
                });
                if (!(it instanceof GraphNode.RootNode)) {
                    dotBuilder.append(String.format("  \"%s\" -> \"%s\";\n", loopedCheck.get(it), childId));

                    E0 edgeInfo = saveFileBuilder.createEdgeView(it, edge);
                    EdgeView<N, E, E0> edgeView = new EdgeViewImpl<>(it.getValue(), edge.getOutNode().getValue(), edge.getValue(), edgeInfo);
                    saveFileBuilder.edgeVisitor.accept(edgeView);
                    canvasGraphPO.addEdge(edgeInfo);
                }
            }
        }
        dotBuilder.append("\n}");
        // pacman -S graphviz
        Map<Integer, String> nodePoins = generatePost(dotBuilder.toString());
        for (Map.Entry<GraphNode<N, E>, Integer> entry : loopedCheck.entrySet()) {
            GraphNode<N, E> node = entry.getKey();
            Integer id = entry.getValue();
            String points = requireNonNull(nodePoins.get(id), String.format("not found node id %s", id));
            double x = Double.parseDouble(points.split(",")[0]);
            double y = Double.parseDouble(points.split(",")[1]);
            x = (x * saveFileBuilder.width * saveFileBuilder.xSpacingFactor() * 0.01);
            y = (y * saveFileBuilder.height * saveFileBuilder.ySpacingFactor() * 0.02);
            N0 nodeInfo = saveFileBuilder.createNodeView(node, (int) x, (int) y);
            NodeViewImpl<N, E, N0> nodeView = new NodeViewImpl<>(node, nodeInfo);
            saveFileBuilder.nodeVisitor.accept(nodeView);
            canvasGraphPO.addNode(nodeInfo);
        }
        return canvasGraphPO;
    }

    private Map<Integer, String> generatePost(String dotString)
    {
        JsonNode jsonNode;
        String json;
        try {
            json = sysCall(dotString);
            jsonNode = MAPPER.readTree(json);
        }
        catch (IOException e) {
            throw new IllegalStateException("layout failed", e);
        }
        ArrayNode arrayNode = jsonNode.withArray("objects");
        Map<Integer, String> nodePoins = new HashMap<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            nodePoins.put(node.get("name").asInt(), node.get("pos").asText());
        }
        return nodePoins;
    }

    private static String sysCall(String dotInput)
            throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tjson0");
        Process process = pb.start();

        // 传入 DOT 数据
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(dotInput);
            writer.flush();
        }

        // 读取 Graphviz 输出的 JSON
        StringBuilder jsonOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
        }
        //System.out.println("Graphviz Output: " + jsonOutput.toString());
        return jsonOutput.toString();
    }
}
