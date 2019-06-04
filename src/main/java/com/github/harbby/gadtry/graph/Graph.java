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

import com.github.harbby.gadtry.graph.impl.DefaultGraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Strings.isNotBlank;
import static com.github.harbby.gadtry.graph.Edge.createEdge;
import static java.util.Objects.requireNonNull;

public interface Graph<E, R>
        extends Serializable
{
    String getName();

    List<String> printShow();

    /**
     * 打印graph结构
     *
     * @param id 已指定id为起点
     * @return graph text
     */
    Iterable<String> printShow(String id);

    /**
     * 搜索出in到out符合规则的所有路径
     *
     * @param in 搜索起点
     * @param out 搜索终点
     * @param rule 规则
     * @return 搜索到的路径
     */
    default List<Route<E, R>> searchRuleRoute(String in, String out, Function<Route<E, R>, Boolean> rule)
    {
        return searchRuleRoute(in, rule).stream()
                .filter(x -> out.equals(x.getEndNodeId()))
                .collect(Collectors.toList());
    }

    List<Route<E, R>> searchRuleRoute(String in, Function<Route<E, R>, Boolean> rule);

    List<Route<E, R>> searchRuleRoute(Function<Route<E, R>, Boolean> rule);

    Route<E, R> getRoute(String... ids);

    static <E, R> GraphBuilder<E, R> builder()
    {
        return new GraphBuilder<>();
    }

    public static class GraphBuilder<E, R>
    {
        private final Map<String, Node.Builder<E, R>> rootNodes = new HashMap<>();
        private final Map<String, Node.Builder<E, R>> nodes = new HashMap<>();
        private String name;

        public GraphBuilder<E, R> name(String name)
        {
            this.name = name;
            return this;
        }

        public GraphBuilder<E, R> addNode(String nodeId, String name)
        {
            return addNode(nodeId, name, null);
        }

        public GraphBuilder<E, R> addNode(String nodeId)
        {
            return addNode(nodeId, "", null);
        }

        public GraphBuilder<E, R> addNode(String nodeId, String name, E nodeData)
        {
            checkState(isNotBlank(nodeId), "nodeId is null or empty");
            nodes.computeIfAbsent(nodeId, key -> {
                Node.Builder<E, R> node = Node.builder(nodeId, name, nodeData);
                rootNodes.put(nodeId, node);
                return node;
            });
            return this;
        }

        public GraphBuilder<E, R> addNode(String nodeId, E nodeData)
        {
            return addNode(nodeId, "", nodeData);
        }

        public GraphBuilder<E, R> addEdge(String node1, String node2, R edgeData)
        {
            Node.Builder<E, R> inNode = requireNonNull(nodes.get(node1), "Unable to create edge because " + node1 + " does not exist");
            Node.Builder<E, R> outNode = requireNonNull(nodes.get(node2), "Unable to create edge because " + node2 + " does not exist");
            Edge<E, R> eEdge = createEdge(outNode.build(), edgeData);
            inNode.addNextNode(eEdge);
            rootNodes.remove(node2);  //从根节点列表中删除
            return this;
        }

        public GraphBuilder<E, R> addEdge(String node1, String node2)
        {
            return addEdge(node1, node2, null);
        }

        public Graph<E, R> create()
        {
            final Node.Builder<E, R> root = Node.builder("/", "", null);
            for (Node.Builder<E, R> node : rootNodes.values()) {
                Edge<E, R> edge = createEdge(node.build(), null);
                root.addNextNode(edge);
            }

            Map<String, Node<E, R>> nodeMap = nodes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().build()));
            return new DefaultGraph<>(name, root.build(), nodeMap);
        }
    }
}
