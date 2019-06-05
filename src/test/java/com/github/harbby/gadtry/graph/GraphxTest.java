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

import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.graph.impl.NodeOperator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GraphxTest
{
    @Test
    public void testCreateGraph1()
            throws Exception
    {
        Graph<?, ?> graph = Graph.builder()
                .name("test1")
                .addNode("a1")
                .addNode("a0")
                .addNode("a22")
                .addNode("a3")

                .addEdge("a1", "a22")
                .addEdge("a1", "a3")
                .addEdge("a0", "a3")
                //-----------------------------------------
                .addNode("a4")
                .addNode("a5")
                .addNode("a6")

                .addEdge("a22", "a4")
                .addEdge("a22", "a5")
                .addEdge("a3", "a6")
                .create();

        Assert.assertEquals("test1", graph.getName());
        graph.printShow().forEach(System.out::println);
    }

    @Test
    public void testRoute()
    {
        Graph<?, ?> graph = Graph.builder()
                .addNode("Throwable")
                .addNode("Exception")
                .addNode("IOException")
                .addNode("FileNotFoundException")

                .addNode("RuntimeException")
                .addNode("UnsupportedOperationException")
                .addNode("IllegalArgumentException")

                .addNode("Error")
                .addNode("OutOfMemoryError")
                .addNode("NoClassDefFoundError")

                .addEdge("Throwable", "Exception")
                .addEdge("Throwable", "Error")

                .addEdge("Exception", "IOException")
                .addEdge("Exception", "FileNotFoundException")
                .addEdge("Exception", "RuntimeException")
                .addEdge("RuntimeException", "UnsupportedOperationException")
                .addEdge("RuntimeException", "IllegalArgumentException")

                .addEdge("Error", "OutOfMemoryError")
                .addEdge("Error", "NoClassDefFoundError")
                .create();

        List<? extends Route<?, ?>> routes = graph.searchRuleRoute("Throwable", route -> {
            return !route.getLastNode().getId().equals("NoClassDefFoundError");
        });

        routes = routes.stream().filter(route1->{
            return route1.getEndNode().getId().equals("NoClassDefFoundError");
        }).collect(Collectors.toList());

        Route<?, ?> route = routes.get(0);
        Node node = route.getLastEdge();
        Assert.assertEquals(node.getId(), "Error");
    }

    @Test
    public void testJavaExceptionGraph()
            throws Exception
    {
        Graph<?, ?> graph = Graph.builder()
                .addNode("Throwable")
                .addNode("Exception")
                .addNode("IOException")
                .addNode("FileNotFoundException")

                .addNode("RuntimeException")
                .addNode("UnsupportedOperationException")
                .addNode("IllegalArgumentException")

                .addNode("Error")
                .addNode("OutOfMemoryError")
                .addNode("NoClassDefFoundError")

                .addEdge("Throwable", "Exception")
                .addEdge("Throwable", "Error")

                .addEdge("Exception", "IOException")
                .addEdge("Exception", "FileNotFoundException")
                .addEdge("Exception", "RuntimeException")
                .addEdge("RuntimeException", "UnsupportedOperationException")
                .addEdge("RuntimeException", "IllegalArgumentException")

                .addEdge("Error", "OutOfMemoryError")
                .addEdge("Error", "NoClassDefFoundError")
                .create();

        graph.printShow().forEach(System.out::println);
        byte[] bytes = Serializables.serialize(graph);
        Assert.assertNotNull(bytes);
    }

    @Test
    public void testAddOperatorReturn6()
            throws Exception
    {
        AtomicInteger integer = new AtomicInteger();
        Graph<NodeOperator<Integer>, Void> graph = Graph.<NodeOperator<Integer>, Void>builder()
                .addNode("a1", new NodeOperator<>(v -> {   //模拟PipeLine管道soruce
                    return 1;
                }))
                .addNode("a2", new NodeOperator<>(v -> {      //模拟PipeLine管道TransForm
                    return v + 2;
                }))
                .addNode("a3", new NodeOperator<>(v -> {      //模拟PipeLine管道TransForm
                    return v + 3;
                }))
                .addNode("a4", new NodeOperator<>(v -> {    //模拟PipeLine管道Sink
                    integer.set(v);
                    return null;
                }))
                .addEdge("a1", "a2")
                .addEdge("a2", "a3")
                .addEdge("a3", "a4")
                .create();

        NodeOperator.runGraph(graph);
        graph.printShow().forEach(System.out::println);
        Assert.assertEquals(6, integer.get());
    }
}
