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

import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DepthPerfectOptimizer<N, E, N0, E0>
        extends ProcessOptimizer<N, E, N0, E0>
{
    DepthPerfectOptimizer(SaveFileBuilder<N, E, N0, E0> saveFileBuilder)
    {
        super(saveFileBuilder);
    }

    @Override
    public SaveFileBuilder.CanvasGraphPO<N0, E0> optimize(GraphNode<N, E> root, int nodeNumber)
    {
        SaveFileBuilder.CanvasGraphPO<N0, E0> canvasGraphPO = new SaveFileBuilder.CanvasGraphPO<>();
        Queue<WrapperNode<N, E>> stack = new LinkedList<>();
        stack.add(new WrapperNode<>(root, -1, 0));

        int[] depthIndexArray = new int[nodeNumber << 1];
        java.util.Arrays.fill(depthIndexArray, 0);
        final Map<N, WrapperNode<N, E>> nodeMap = new HashMap<>();
        final Set<Tuple2<N, N>> looped = new HashSet<>();
        WrapperNode<N, E> it;
        while ((it = stack.poll()) != null) {
            final GraphNode<N, E> parentNode = it.node;
            final int depth = it.depth + 1;
            for (GraphEdge<N, E> edge : parentNode.nextNodes()) {
                GraphNode<N, E> node = edge.getOutNode();
                WrapperNode<N, E> wrapperNode = nodeMap.get(node.getValue());
                if (wrapperNode == null) {
                    // add node to tree
                    wrapperNode = new WrapperNode<>(node, depth, depthIndexArray[depth]);
                    nodeMap.put(node.getValue(), wrapperNode);
                    depthIndexArray[depth]++;
                }
                else {
                    boolean loopCheck = looped.add(Tuple2.of(parentNode.getValue(), edge.getOutNode().getValue()));
                    if (!loopCheck) {
                        continue;
                    }
                    if (wrapperNode.depth < depth) {
                        wrapperNode.depth = depth;
                        wrapperNode.index = depthIndexArray[depth]++;
                    }
                }
                stack.add(wrapperNode);
                if (!(parentNode instanceof GraphNode.RootNode)) {
                    E0 edgeInfo = saveFileBuilder.createEdgeView(parentNode, edge);
                    EdgeView<N, E, E0> edgeView = new EdgeViewImpl<>(parentNode.getValue(), edge.getOutNode().getValue(), edge.getValue(), edgeInfo);
                    saveFileBuilder.edgeVisitor.accept(edgeView);
                    canvasGraphPO.addEdge(edgeInfo);
                }
            }
        }
        for (WrapperNode<N, E> wrapperNode : nodeMap.values()) {
            N0 nodeInfo = saveFileBuilder.createNodeView(wrapperNode.node, wrapperNode.depth, wrapperNode.index);
            NodeViewImpl<N, E, N0> nodeView = new NodeViewImpl<>(wrapperNode.node, nodeInfo);
            saveFileBuilder.nodeVisitor.accept(nodeView);
            canvasGraphPO.addNode(nodeInfo);
        }
        return canvasGraphPO;
    }
}
