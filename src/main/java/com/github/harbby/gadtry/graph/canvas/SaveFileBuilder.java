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

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private ProcessOptimizer.Strategy strategy = ProcessOptimizer.Strategy.PERFECT_DEPTH_V2;

    protected Double xSpacingFactor;
    protected Double ySpacingFactor;

    protected int width;
    protected int height;

    public enum Direction
    {
        left_to_right(1.6, 1.4), up_to_down(1.1, 2.3);

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

    public final SaveFileBuilder<N, E, N0, E0> strategy(ProcessOptimizer.Strategy strategy)
    {
        this.strategy = strategy;
        return this;
    }

    public final void save(File path)
            throws IOException
    {
        ProcessOptimizer<N, E, N0, E0> optimizer = strategy.create(this);
        CanvasGraphPO<N0, E0> canvasGraphPO = optimizer.optimize(root, nodes.size());
        // check and mkdir parent dir
        File parentFile = path.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new NotDirectoryException("mkdir parent dir " + parentFile + " failed");
        }
        this.serialize(canvasGraphPO, path);
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
