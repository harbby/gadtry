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
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class CanvasBuilder<N, E>
        extends SaveFileBuilder<N, E, CanvasNodePo, CanvasEdgePo>
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String nodeColor = "0";
    private String edgeColor = "0";
    private String nodeType = "text";
    private String edgeType = "text";

    public CanvasBuilder(GraphNode<N, E> root, Map<N, GraphNode<N, E>> nodes)
    {
        super(root, nodes, 300, 50);
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

    public CanvasBuilder<N, E> nodeType(String type)
    {
        this.nodeType = type;
        return this;
    }

    public CanvasBuilder<N, E> edgeType(String type)
    {
        this.edgeType = type;
        return this;
    }

    public CanvasBuilder<N, E> visitNode(Consumer<NodeView<N, CanvasNodePo>> consumer)
    {
        this.nodeVisitor = requireNonNull(consumer, "nodeVisitor is null");
        return this;
    }

    public CanvasBuilder<N, E> visitEdge(Consumer<EdgeView<N, E, CanvasEdgePo>> consumer)
    {
        this.edgeVisitor = requireNonNull(consumer, "nodeVisitor is null");
        return this;
    }

    protected CanvasNodePo createNodeView(GraphNode<N, E> node, int depth, int index)
    {
        String id = this.idSelector.apply(node.getValue());
        CanvasNodePo nodePo = new CanvasNodePo(id);
        nodePo.putConf("width", this.width);
        nodePo.putConf("height", this.height);
        nodePo.putConf("type", this.nodeType);

        nodePo.setColor(this.nodeColor);
        nodePo.setLabel(String.valueOf(node.getValue()));

        boolean hasChildren = !node.nextNodes().isEmpty();
        nodePo.putConf("hasChildren", hasChildren);
        long xy = generateXY(depth, index);
        int x = (int) (xy >>> Integer.SIZE);
        int y = (int) (xy);
        nodePo.putConf("x", x);
        nodePo.putConf("y", y);
        return nodePo;
    }

    protected CanvasEdgePo createEdgeView(GraphNode<N, E> from, GraphEdge<N, E> edge)
    {
        final String sourceID = this.idSelector.apply(from.getValue());
        final String targetID = this.idSelector.apply(edge.getOutNode().getValue());
        final String edgeValue = edge.getValue() == null ? null : edge.getValue().toString();
        final String id = sourceID + " to " + targetID;
        CanvasEdgePo canvasEdgePo = new CanvasEdgePo(id);

        canvasEdgePo.putConf("fromNode", sourceID);
        canvasEdgePo.putConf("toNode", targetID);
        if (direction == Direction.left_to_right) {
            canvasEdgePo.putConf("fromSide", "right");
            canvasEdgePo.putConf("toSide", "left");
        }
        else {
            canvasEdgePo.putConf("fromSide", "bottom");
            canvasEdgePo.putConf("toSide", "top");
        }
        canvasEdgePo.setColor(this.edgeColor);
        canvasEdgePo.setLabel(edgeValue);
        canvasEdgePo.putConf("type", this.edgeType);
        return canvasEdgePo;
    }

    @Override
    protected void serialize(CanvasGraphPO<CanvasNodePo, CanvasEdgePo> graphPO, File path)
            throws IOException
    {
        MAPPER.writerFor(CanvasGraphPO.class).writeValue(path, graphPO);
    }
}
