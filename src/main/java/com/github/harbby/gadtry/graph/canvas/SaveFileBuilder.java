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

import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.drawio.DrawioMxCell;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.harbby.gadtry.base.MoreObjects.getNonNull;
import static java.util.Objects.requireNonNull;

public abstract class SaveFileBuilder<N, E, N0, E0>
{
    protected Function<N, String> idSelector = String::valueOf;
    protected Direction direction = Direction.left_to_right;
    protected Consumer<NodeView<N, N0>> nodeVisitor = nNodeContext -> {};
    protected Consumer<EdgeView<N, E, E0>> edgeVisitor = nNodeContext -> {};

    protected Double xSpacingFactor;
    protected Double ySpacingFactor;

    protected int width;
    protected int height;

    public enum Direction
    {
        left_to_right(1.6, 1.4),
        up_to_down(1.1, 2.3);

        private final double xSpacingFactor;
        private final double ySpacingFactor;

        Direction(double x, double y)
        {
            this.xSpacingFactor = x;
            this.ySpacingFactor = y;
        }
    }

    private final GraphNode<N, E> root;
    private final Map<N, GraphNode<N, E>> nodes;

    protected SaveFileBuilder(GraphNode<N, E> root, Map<N, GraphNode<N, E>> nodes, int defaultWidth, int defaultHeight)
    {
        this.root = root;
        this.nodes = nodes;
        this.width = defaultWidth;
        this.height = defaultHeight;
    }

    protected abstract E0 createEdgeView(GraphNode<N, E> from, GraphEdge<N, E> edge);

    protected abstract N0 createNodeView(GraphNode<N, E> node, int depth, int index);

    protected abstract void serialize(CanvasGraphPO<N0, E0> graphPO, File path)
            throws IOException;

    public SaveFileBuilder<N, E, N0, E0> direction(Direction direction)
    {
        this.direction = requireNonNull(direction, "direction is null");
        return this;
    }

    public SaveFileBuilder<N, E, N0, E0> idBy(Function<N, String> idSelector)
    {
        this.idSelector = requireNonNull(idSelector, "idSelector is null");
        return this;
    }

    protected final long generateXY(int depth, int index)
    {
        double xSpacingFactor = getNonNull(this.xSpacingFactor, direction.xSpacingFactor);
        double ySpacingFactor = getNonNull(this.ySpacingFactor, direction.ySpacingFactor);
        int x;
        int y;
        if (this.direction == Direction.left_to_right) {
            x = (int) (depth * width * xSpacingFactor);
            y = (int) (index * height * ySpacingFactor);
        }
        else {
            y = (int) (depth * height * ySpacingFactor);
            x = (int) (index * width * xSpacingFactor);
        }
        return (((long) x) << Integer.SIZE) | (long) y;
    }

    public final void save(File path)
            throws IOException
    {
        CanvasGraphPO<N0, E0> canvasGraphPO = new CanvasGraphPO<>();
        Queue<WrapperNode<N, E>> stack = new LinkedList<>();
        stack.add(new WrapperNode<>(root, -1, 0));

        int[] depthIndexArray = new int[nodes.size()];
        java.util.Arrays.fill(depthIndexArray, 0);
        final Map<N, WrapperNode<N, E>> loopedCheck = new HashMap<>();
        WrapperNode<N, E> wrapper;
        while ((wrapper = stack.poll()) != null) {
            GraphNode<N, E> parentNode = wrapper.node;
            final int depth = wrapper.depth + 1;
            for (GraphEdge<N, E> edge : parentNode.nextNodes()) {
                GraphNode<N, E> node = edge.getOutNode();
                WrapperNode<N, E> wrapperNode = loopedCheck.get(node.getValue());
                if (wrapperNode == null) {
                    // add node to tree
                    wrapperNode = new WrapperNode<>(node, depth, depthIndexArray[depth]);
                    loopedCheck.put(node.getValue(), wrapperNode);
                    stack.add(wrapperNode);
                }
                else {
                    wrapperNode.depth = Math.max(wrapperNode.depth, depth);
                }
                depthIndexArray[depth]++;
                if (!(parentNode instanceof GraphNode.RootNode)) {
                    E0 edgeInfo = this.createEdgeView(parentNode, edge);
                    EdgeView<N, E, E0> edgeView = new EdgeViewImpl<>(parentNode.getValue(), edge.getOutNode().getValue(), edge.getValue(), edgeInfo);
                    this.edgeVisitor.accept(edgeView);
                    canvasGraphPO.addEdge(edgeInfo);
                }
            }
        }
        for (WrapperNode<N, E> wrapperNode : loopedCheck.values()) {
            N0 nodeInfo = this.createNodeView(wrapperNode.node, wrapperNode.depth, wrapperNode.index);
            NodeViewImpl<N, E, N0> nodeView = new NodeViewImpl<>(wrapperNode.node, nodeInfo);
            this.nodeVisitor.accept(nodeView);
            canvasGraphPO.addNode(nodeInfo);
        }
        // check and mkdir parent dir
        File parentFile = path.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new NotDirectoryException("mkdir parent dir " + parentFile + " failed");
        }
        this.serialize(canvasGraphPO, path);
    }

    static class WrapperNode<N, E>
    {
        private final transient GraphNode<N, E> node;
        private int depth;
        private final int index;

        WrapperNode(GraphNode<N, E> node, int depth, int index)
        {
            this.node = node;
            this.index = index;
            this.depth = depth;
        }
    }

    protected static final class NodeViewImpl<N, E, N0>
            implements NodeView<N, N0>
    {
        private final transient GraphNode<N, E> node;
        private final N0 fileNode;

        public NodeViewImpl(GraphNode<N, E> node, N0 fileNode)
        {
            this.node = node;
            this.fileNode = fileNode;
        }

        @Override
        public N0 getInfo()
        {
            return fileNode;
        }

        @Override
        public String toString()
        {
            return node.toString();
        }

        @Override
        public N getNode()
        {
            return node.getValue();
        }
    }

    protected static class EdgeViewImpl<N, E, E0>
            implements EdgeView<N, E, E0>
    {
        private final N input;
        private final N output;
        private final E value;
        private final E0 edgeConfig;

        public EdgeViewImpl(N input, N output, E value, E0 edgeConfig)
        {
            this.input = input;
            this.output = output;
            this.value = value;
            this.edgeConfig = edgeConfig;
        }

        @Override
        public N getInput()
        {
            return input;
        }

        @Override
        public N getOutput()
        {
            return output;
        }

        @Override
        public E getValue()
        {
            return value;
        }

        @Override
        public E0 getInfo()
        {
            return edgeConfig;
        }
    }

    protected static class CanvasGraphPO<N0, E0>
    {
        private final List<N0> nodes = new ArrayList<>();
        private final List<E0> edges = new ArrayList<>();

        public void addEdge(E0 edge)
        {
            edges.add(edge);
        }

        public void addNode(N0 node)
        {
            nodes.add(node);
        }

        public List<E0> getEdges()
        {
            return edges;
        }

        public List<N0> getNodes()
        {
            return nodes;
        }
    }
}
