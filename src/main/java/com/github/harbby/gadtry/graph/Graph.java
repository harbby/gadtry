/*
 * Copyright (C) 2018 The Harbby Authors
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
import com.github.harbby.gadtry.graph.impl.DefaultNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Graph<E>
{
    String getName();

    void printShow()
            throws Exception;

    /**
     * 打印graph结构
     *
     * @param id 已指定id为起点
     */
    void printShow(String id);

    void run()
            throws Exception;

    void runParallel()
            throws Exception;

    /**
     * 搜索出in到out符合规则的所有路径
     *
     * @param in 搜索起点
     * @param out 搜索终点
     * @param rule 规则
     * @return 搜索到的路径
     */
    List<Route> searchRuleRoute(String in, String out, Function<Route, Boolean> rule);

    List<Route> searchRuleRoute(String in, Function<Route, Boolean> rule);

    Route getRoute(String... ids);

    static <E> GraphBuilder<E> builder()
    {
        return new GraphBuilder<>();
    }

    public static class GraphBuilder<E>
    {
        private final Map<String, Node<E>> rootNodes = new HashMap<>();
        private final Map<String, Node<E>> nodes = new HashMap<>();
        private String name;

        public GraphBuilder<E> name(String name)
        {
            this.name = name;
            return this;
        }

        public GraphBuilder<E> addNode(String nodeId)
        {
            Node<E> node = DefaultNode.of(nodeId);
            nodes.put(node.getId(), node);
            rootNodes.put(node.getId(), node);
            return this;
        }

        public GraphBuilder<E> addNode(Node<E> node)
        {
            nodes.put(node.getId(), node);
            rootNodes.put(node.getId(), node);
            return this;
        }

        public GraphBuilder<E> addEdge(String node1, String node2, Edge<E> eEdge)
        {
            Node<E> inNode = requireNonNull(nodes.get(node1), "Unable to create edge because " + node1 + " does not exist");
            Node<E> outNode = requireNonNull(nodes.get(node2), "Unable to create edge because " + node2 + " does not exist");
            eEdge.setOutNode(outNode);
            inNode.addNextNode(eEdge);
            rootNodes.remove(outNode.getId());  //从根节点列表中删除
            return this;
        }

        public GraphBuilder<E> addEdge(String node1, String node2)
        {
            Edge<E> eEdge = new Edge<>();
            return addEdge(node1, node2, eEdge);
        }

        public Graph<E> create()
        {
            final Node<E> root = DefaultNode.of("/");
            rootNodes.values().forEach(node -> {
                Edge<E> edge = new Edge<>();
                edge.setOutNode(node);
                root.addNextNode(edge);
            });
            return new DefaultGraph<>(name, root, nodes);
        }
    }
}
