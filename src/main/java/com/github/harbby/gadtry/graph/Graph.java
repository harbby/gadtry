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
package com.github.harbby.gadtry.graph;

import com.github.harbby.gadtry.graph.canvas.CanvasBuilder;
import com.github.harbby.gadtry.graph.canvas.SaveFileBuilder;
import com.github.harbby.gadtry.graph.drawio.DrawioBuilder;
import com.github.harbby.gadtry.graph.impl.DefaultGraph;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Graph<N, E>
        extends Serializable
{
    public void addNode(N node);

    public void addEdge(N n1, N n2);

    List<String> printShow();

    default void saveAsCanvas(String path)
            throws IOException
    {
        saveAsCanvas(new File(path));
    }

    CanvasBuilder<N, E> saveAsCanvas();

    DrawioBuilder<N, E> saveAsDrawio();

    void saveAsCanvas(File path)
            throws IOException;

    public <T extends SaveFileBuilder<N, E, ?, ?>> T saveAsTypeBuilder(Class<T> builderType);

    /**
     * 打印graph结构
     *
     * @param id 已指定id为起点
     * @return graph text
     */
    Iterable<String> printShow(N id);

    /**
     * 搜索出in到out符合规则的所有路径
     *
     * @param in   搜索起点
     * @param out  搜索终点
     * @param rule 规则
     * @return 搜索到的路径
     */
    default List<Route<N, E>> searchRuleRoute(N in, N out, Function<Route<N, E>, Boolean> rule)
    {
        return searchRuleRoute(in, rule).stream()
                .filter(x -> out.equals(x.getLastNodeId()))
                .collect(Collectors.toList());
    }

    public List<GraphNode<N, E>> findNode(Function<GraphNode<N, E>, Boolean> rule);

    public SearchBuilder<N, E> search();

    List<Route<N, E>> searchRuleRoute(N in, Function<Route<N, E>, Boolean> rule);

    List<Route<N, E>> searchRuleRoute(Function<Route<N, E>, Boolean> rule);

    Route<N, E> getRoute(N... ids);

    public GraphNode<N, E> getNode(N id);

    static <N, E> GraphBuilder<N, E> builder()
    {
        return new GraphBuilder<>();
    }

    public static class GraphBuilder<N, E>
    {
        private final Map<N, GraphNode<N, E>> rootNodes = new HashMap<>();
        private final Map<N, GraphNode<N, E>> nodes = new HashMap<>();

        public GraphBuilder<N, E> addNode(N nodeData)
        {
            computeIfAbsentNode(nodeData);
            return this;
        }

        private GraphNode<N, E> computeIfAbsentNode(N nodeData)
        {
            return nodes.computeIfAbsent(nodeData, key -> {
                GraphNode<N, E> node = GraphNode.of(nodeData);
                rootNodes.put(nodeData, node);
                return node;
            });
        }

        public GraphBuilder<N, E> addEdge(N in, N out, E edgeData)
        {
            GraphNode<N, E> inNode = computeIfAbsentNode(in);
            GraphNode<N, E> outNode = computeIfAbsentNode(out);
            inNode.addNextNode(outNode, edgeData);
            rootNodes.remove(out);  //从根节点列表中删除
            return this;
        }

        public GraphBuilder<N, E> addEdge(N node1, N node2)
        {
            return addEdge(node1, node2, null);
        }

        public Graph<N, E> create()
        {
            final GraphNode<N, E> root = new GraphNode.RootNode<>();
            for (GraphNode<N, E> node : rootNodes.values()) {
                root.addNextNode(node, null);
            }

            Map<N, GraphNode<N, E>> nodeMap = nodes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new DefaultGraph<>(root, nodeMap);
        }
    }
}
