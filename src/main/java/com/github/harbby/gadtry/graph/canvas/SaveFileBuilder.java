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

import com.github.harbby.gadtry.graph.Graph;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.export.Direction;
import com.github.harbby.gadtry.graph.export.GraphExporter;
import com.github.harbby.gadtry.graph.layout.GraphLayout;
import com.github.harbby.gadtry.graph.layout.GraphLayouts;
import com.github.harbby.gadtry.graph.layout.LayoutOptions;
import com.github.harbby.gadtry.graph.layout.Point;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.harbby.gadtry.base.MoreObjects.getNonNull;
import static java.util.Objects.requireNonNull;

/**
 * Base class of the visual formats (canvas, drawio): a {@link GraphLayout} places the nodes,
 * subclasses turn nodes and edges into file elements.
 */
public abstract class SaveFileBuilder<N, E, N0, E0>
        extends GraphExporter<N, E>
{
    protected Function<N, String> idSelector = String::valueOf;
    protected Direction direction = Direction.LEFT_TO_RIGHT;
    protected Consumer<NodeView<N, N0>> nodeVisitor = nodeView -> {};
    protected Consumer<EdgeView<N, E, E0>> edgeVisitor = edgeView -> {};
    private GraphLayout layout = GraphLayouts.sugiyama();

    protected Double xSpacingFactor;
    protected Double ySpacingFactor;

    protected int width;
    protected int height;

    protected SaveFileBuilder(Graph<N, E> graph, int defaultWidth, int defaultHeight)
    {
        super(graph);
        this.width = defaultWidth;
        this.height = defaultHeight;
    }

    protected abstract E0 createEdgeView(GraphNode<N, E> from, GraphEdge<N, E> edge);

    protected abstract N0 createNodeView(GraphNode<N, E> node, int x, int y);

    protected abstract void serialize(CanvasGraphPO<N0, E0> graphPO, Writer writer)
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

    protected double xSpacingFactor()
    {
        return getNonNull(this.xSpacingFactor, direction == Direction.LEFT_TO_RIGHT ? 1.6 : 1.1);
    }

    protected double ySpacingFactor()
    {
        return getNonNull(this.ySpacingFactor, direction == Direction.LEFT_TO_RIGHT ? 1.4 : 2.3);
    }

    /**
     * Node placement algorithm, default {@link GraphLayouts#sugiyama()} (pure java, no external tools).
     */
    public final SaveFileBuilder<N, E, N0, E0> layout(GraphLayout layout)
    {
        this.layout = requireNonNull(layout, "layout is null");
        return this;
    }

    @Override
    protected final void write(Writer writer)
            throws IOException
    {
        LayoutOptions options = new LayoutOptions(direction, width, height,
                width * xSpacingFactor(), height * ySpacingFactor());
        Map<N, Point> positions = layout.layout(graph, options);

        CanvasGraphPO<N0, E0> graphPO = new CanvasGraphPO<>();
        for (GraphNode<N, E> node : graph.nodes()) {
            Point point = requireNonNull(positions.get(node.getValue()), "layout missing node " + node.getValue());
            N0 nodeInfo = createNodeView(node, (int) Math.round(point.getX()), (int) Math.round(point.getY()));
            nodeVisitor.accept(new NodeViewImpl<>(node, nodeInfo));
            graphPO.addNode(nodeInfo);
        }
        for (GraphEdge<N, E> edge : graph.edges()) {
            E0 edgeInfo = createEdgeView(edge.getInNode(), edge);
            edgeVisitor.accept(new EdgeViewImpl<>(edge.getInNode().getValue(), edge.getOutNode().getValue(), edge.getValue(), edgeInfo));
            graphPO.addEdge(edgeInfo);
        }
        this.serialize(graphPO, writer);
    }

    private static final class NodeViewImpl<N, E, N0>
            implements NodeView<N, N0>
    {
        private final GraphNode<N, E> node;
        private final N0 fileNode;

        private NodeViewImpl(GraphNode<N, E> node, N0 fileNode)
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
        public N getNode()
        {
            return node.getValue();
        }

        @Override
        public String toString()
        {
            return node.toString();
        }
    }

    private static final class EdgeViewImpl<N, E, E0>
            implements EdgeView<N, E, E0>
    {
        private final N input;
        private final N output;
        private final E value;
        private final E0 edgeConfig;

        private EdgeViewImpl(N input, N output, E value, E0 edgeConfig)
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

    public static class CanvasGraphPO<N0, E0>
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
