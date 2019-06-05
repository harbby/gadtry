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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GraphxTest
{
    private final Graph<?, ?> graph = Graph.builder()
            .name("test1")
            .addNode("a1")
            .addNode("a2")
            .addNode("a3")

            .addEdge("a1", "a2")
            .addEdge("a1", "a3")
            //-----------------------------------------
            .addNode("a4")
            .addNode("a5")
            .addNode("a6")

            .addEdge("a2", "a4")
            .addEdge("a2", "a5")
            .addEdge("a3", "a6")
            .create();

    @Test
    public void testCreateGraph1()
    {
        Assert.assertEquals("test1", graph.getName());
        List<? extends Route<?, ?>> list = graph.searchRuleRoute(route -> true);
        Assert.assertEquals(list.size(), 6);
        graph.printShow().forEach(System.out::println);
    }

    @Test
    public void getLastEdge()
    {
        Route<?, ?> route = graph.getRoute("a1", "a2", "a5");
        Assert.assertEquals(route.getLastEdge().getOutNode().getId(), "a5");
        try {
            Route.builder(route.getLastNode()).create().getLastEdge();
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "this Route only begin node");
        }
    }

    @Test
    public void getLastNode()
    {
        Route<?, ?> route = graph.getRoute("a1", "a2", "a5");
        Assert.assertEquals(route.getLastNode(2).getId(), "a1");
        try {
            route.getLastNode(3).getId();
            Assert.fail();
        }
        catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void routeEqualsReturnTrue()
    {
        Route<?, ?> route1 = graph.getRoute("a1", "a2", "a5");
        Route<?, ?> route2 = graph.getRoute("a1", "a2", "a5");
        Assert.assertEquals(route1.hashCode(), route2.hashCode());
        Assert.assertEquals(route1, route2);
        Assert.assertEquals(route1, route1);
        Assert.assertFalse(route1.equals(null));
        Assert.assertFalse(route1.equals(""));

        Assert.assertNotEquals(graph.getRoute("a1", "a2", "a5"), graph.getRoute("a2", "a5"));
        Assert.assertNotEquals(graph.getRoute("a1", "a2", "a5"), graph.getRoute("a1", "a2"));
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
            return !route.getLastNode(1).getId().equals("NoClassDefFoundError");
        });

        routes = routes.stream().filter(route1 -> {
            return route1.getLastNode().getId().equals("NoClassDefFoundError");
        }).collect(Collectors.toList());

        Route<?, ?> route = routes.get(0);
        Node node = route.getLastNode(1);
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
