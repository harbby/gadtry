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

import com.github.harbby.gadtry.graph.GraphNode;

public abstract class ProcessOptimizer<N, E, N0, E0>
{
    public enum Strategy
    {
        DEFAULT,
        PERFECT_DEPTH,
        PERFECT_DEPTH_V2;

        <N, E, N0, E0> ProcessOptimizer<N, E, N0, E0> create(SaveFileBuilder<N, E, N0, E0> builder)
        {
            switch (this) {
                case DEFAULT:
                    return new DefaultOptimizer<>(builder);
                case PERFECT_DEPTH:
                    return new DepthPerfectOptimizer<>(builder);
                case PERFECT_DEPTH_V2:
                    return new DepthPerfectOptimizerV2<>(builder);
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    protected final SaveFileBuilder<N, E, N0, E0> saveFileBuilder;

    ProcessOptimizer(SaveFileBuilder<N, E, N0, E0> saveFileBuilder)
    {
        this.saveFileBuilder = saveFileBuilder;
    }

    abstract SaveFileBuilder.CanvasGraphPO<N0, E0> optimize(GraphNode<N, E> root, int nodeNumber);

    protected static class WrapperNode<N, E>
    {
        protected final transient GraphNode<N, E> node;
        protected int depth;
        protected int index;

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
}
