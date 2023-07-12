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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class CanvasBuilder<N, E>
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GraphNode<N, E> root;
    private final Map<N, GraphNode<N, E>> nodes;
    private int width = 300;
    private int height = 70;
    private double xSpacingFactor = 1.5;
    private double ySpacingFactor = 1.4;
    private String nodeColor = "0";
    private String edgeColor = "0";
    private String nodeType = "text";
    private String edgeType = "text";

    private Consumer<CanvasNodePo<N, E>> nodeVisitor = nNodeContext -> {};
    private Consumer<CanvasEdgePo<N, E>> edgeVisitor = nNodeContext -> {};

    public CanvasBuilder(GraphNode<N, E> root, Map<N, GraphNode<N, E>> nodes)
    {
        this.root = root;
        this.nodes = nodes;
    }

    public CanvasBuilder<N, E> nodeWidth(int width)
    {
        this.width = width;
        return this;
    }

    public CanvasBuilder<N, E> nodeHeight(int height)
    {
        this.height = height;
        return this;
    }

    public CanvasBuilder<N, E> xSpacingFactor(double xSpacingFactor)
    {
        this.xSpacingFactor = xSpacingFactor;
        return this;
    }

    public CanvasBuilder<N, E> ySpacingFactor(double ySpacingFactor)
    {
        this.ySpacingFactor = ySpacingFactor;
        return this;
    }

    public CanvasBuilder<N, E> NodeColor(String color)
    {
        this.nodeColor = color;
        return this;
    }

    public CanvasBuilder<N, E> edgeColor(String color)
    {
        this.edgeColor = color;
        return this;
    }

    public CanvasBuilder<N, E> visitNode(Consumer<CanvasNodePo<N, E>> consumer)
    {
        this.nodeVisitor = requireNonNull(consumer, "nodeVisitor is null");
        return this;
    }

    public CanvasBuilder<N, E> visitEdge(Consumer<CanvasEdgePo<N, E>> consumer)
    {
        this.edgeVisitor = requireNonNull(consumer, "nodeVisitor is null");
        return this;
    }

    private CanvasNodePo<N, E> createCanvasNode(GraphNode<N, E> node, int depth, int index)
    {
        CanvasNodePo<N, E> nodePo = new CanvasNodePo<>(node, depth, index);
        nodePo.nodeConfig.put("width", this.width);
        nodePo.nodeConfig.put("height", this.height);
        nodePo.nodeConfig.put("color", this.nodeColor);
        nodePo.nodeConfig.put("type", this.nodeType);
        String id = node.toString();
        nodePo.nodeConfig.put("text", id);
        nodePo.nodeConfig.put("id", id);

        boolean hasChildren = !node.nextNodes().isEmpty();
        nodePo.nodeConfig.put("hasChildren", hasChildren);
        int x = (int) (depth * this.width * this.xSpacingFactor);
        int y = index * (int) (this.height * this.ySpacingFactor);
        nodePo.nodeConfig.put("x", x);
        nodePo.nodeConfig.put("y", y);
        this.nodeVisitor.accept(nodePo);
        return nodePo;
    }

    private CanvasEdgePo<N, E> createCanvasEdge(GraphNode<N, E> from, GraphEdge<N, E> edge)
    {
        Map<String, Object> edgeConfig = new HashMap<>();
        N in = from.getValue();
        N out = edge.getOutNode().getValue();
        String fromNode = in.toString();
        String toNode = out.toString();
        String id = fromNode + " to " + toNode;
        edgeConfig.put("id", id);
        edgeConfig.put("fromNode", fromNode);
        edgeConfig.put("toNode", toNode);
        edgeConfig.put("fromSide", "right");
        edgeConfig.put("toSide", "left");
        edgeConfig.put("color", this.edgeColor);
        edgeConfig.put("type", this.edgeType);
        CanvasEdgePo<N, E> canvasEdgePo = new CanvasEdgePo<>(in, out, edge.getValue(), edgeConfig);
        this.edgeVisitor.accept(canvasEdgePo);
        return canvasEdgePo;
    }

    public void save(File path)
            throws IOException
    {
        CanvasGraphPO<N, E> canvasGraphPO = new CanvasGraphPO<>();
        Queue<CanvasNodePo<N, E>> stack = new LinkedList<>();
        stack.add(this.createCanvasNode(root, -1, 0));

        int[] depthIndexArray = new int[nodes.size()];
        java.util.Arrays.fill(depthIndexArray, 0);
        Set<N> loopedCheck = new HashSet<>();
        CanvasNodePo<N, E> wrapper;
        while ((wrapper = stack.poll()) != null) {
            GraphNode<N, E> parentNode = wrapper.node;
            final int depth = wrapper.depth + 1;
            for (GraphEdge<N, E> edge : parentNode.nextNodes()) {
                GraphNode<N, E> node = edge.getOutNode();
                if (loopedCheck.add(node.getValue())) {
                    // add node to tree
                    CanvasNodePo<N, E> nodePo = this.createCanvasNode(node, depth, depthIndexArray[depth]);
                    canvasGraphPO.addNode(nodePo);
                    stack.add(nodePo);
                }
                depthIndexArray[depth]++;
                if (!(parentNode instanceof GraphNode.RootNode)) {
                    canvasGraphPO.addEdge(this.createCanvasEdge(parentNode, edge));
                }
            }
        }
        MAPPER.writerFor(CanvasGraphPO.class).writeValue(path, canvasGraphPO);
    }

    public static class CanvasGraphPO<N, E>
    {
        private final List<CanvasNodePo<N, E>> nodes = new ArrayList<>();
        private final List<CanvasEdgePo<N, E>> edges = new ArrayList<>();

        public void addEdge(CanvasEdgePo<N, E> edge)
        {
            edges.add(edge);
        }

        public void addNode(CanvasNodePo<N, E> node)
        {
            nodes.add(node);
        }

        public List<CanvasEdgePo<N, E>> getEdges()
        {
            return edges;
        }

        public List<CanvasNodePo<N, E>> getNodes()
        {
            return nodes;
        }
    }
}
