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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GraphxTest
{
    private final Graph<String, Void> graph = Graph.<String, Void>builder()
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

    public static final Graph<String, Void> JAVA_EXCEPTION_GRAPH = Graph.<String, Void>builder()
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

    @Test
    public void testCreateGraph1()
    {
        List<? extends Route<?, ?>> list = graph.searchRuleRoute(route -> true);
        Assertions.assertEquals(list.size(), 6);
        graph.printShow().forEach(System.out::println);

        Assertions.assertEquals(graph.getNode("a2").nextNodes().size(), 2);

        try {
            graph.getNode("a100");
            Assertions.fail();
        }
        catch (NullPointerException e) {
            Assertions.assertEquals(e.getMessage(), "NO SUCH Node a100");
        }
    }

    @Test
    public void getLastEdge()
    {
        Route<?, ?> route = graph.getRoute("a1", "a2", "a5");
        Assertions.assertEquals(route.getLastEdge().getOutNode().getValue(), "a5");
        try {
            Route.builder(route.getLastNode()).create().getLastEdge();
            Assertions.fail();
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "this Route only begin node");
        }
    }

    @Test
    public void getLastNode()
    {
        Route<?, ?> route = graph.getRoute("a1", "a2", "a5");
        Assertions.assertEquals(route.getLastNode(2).getValue(), "a1");
        try {
            route.getLastNode(3).getValue();
            Assertions.fail();
        }
        catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void routeEqualsReturnTrue()
    {
        Route<?, ?> route1 = graph.getRoute("a1", "a2", "a5");
        Route<?, ?> route2 = graph.getRoute("a1", "a2", "a5");
        Assertions.assertEquals(route1.hashCode(), route2.hashCode());
        Assertions.assertEquals(route1, route2);
        Assertions.assertEquals(route1, route1);
        Assertions.assertFalse(route1.equals(null));
        Assertions.assertFalse(route1.equals(""));

        Assertions.assertNotEquals(graph.getRoute("a1", "a2", "a5"), graph.getRoute("a2", "a5"));
        Assertions.assertNotEquals(graph.getRoute("a1", "a2", "a5"), graph.getRoute("a1", "a2"));
    }

    @Test
    public void testRoute()
    {
        Graph<String, Void> graph = JAVA_EXCEPTION_GRAPH;
        graph.printShow().forEach(System.out::println);

        List<? extends Route<?, ?>> routes = graph.searchRuleRoute("Throwable", route -> {
            return !route.getLastNode(1).getValue().equals("NoClassDefFoundError");
        });

        routes = routes.stream().filter(route1 -> {
            return route1.getLastNode().getValue().equals("NoClassDefFoundError");
        }).collect(Collectors.toList());

        Route<?, ?> route = routes.get(0);
        GraphNode node = route.getLastNode(1);
        Assertions.assertEquals(node.getValue(), "Error");
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
        Assertions.assertNotNull(bytes);
    }

    @Test
    public void testAddOperatorReturn6()
    {
        AtomicInteger db = new AtomicInteger();  //mock db

        NodeOperator<Integer> source = new NodeOperator<>("source", v -> {   //模拟PipeLine管道soruce
            return 1;
        });
        NodeOperator<Integer> a2 = new NodeOperator<>("a2", v -> {   //模拟PipeLine管道TransForm
            return v + 2;
        });
        NodeOperator<Integer> a3 = new NodeOperator<>("a3", v -> {   //模拟PipeLine管道TransForm
            return v + 3;
        });
        NodeOperator<Integer> sink = new NodeOperator<>("sink", v -> {   //模拟PipeLine管道Sink
            db.set(v);
            return null;
        });

        Graph<NodeOperator<Integer>, Void> graph = Graph.<NodeOperator<Integer>, Void>builder()
                .addNode(source)
                .addNode(a2)
                .addNode(a3)
                .addNode(sink)
                .addEdge(source, a2)
                .addEdge(a2, a3)
                .addEdge(a3, sink)
                .create();

        NodeOperator.runGraph(graph);
        graph.printShow().forEach(System.out::println);
        Assertions.assertEquals(6, db.get());
    }
}
