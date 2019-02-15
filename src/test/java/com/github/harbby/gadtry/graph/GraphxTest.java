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

import com.github.harbby.gadtry.graph.impl.NodeOperator;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.harbby.gadtry.base.Checks.checkState;

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

        graph.printShow().forEach(System.out::println);
        TimeUnit.MILLISECONDS.sleep(300);
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
    }

    @Test
    public void testAddOperatorReturn6()
            throws Exception
    {
        AtomicInteger integer = new AtomicInteger();
        Graph<NodeOperator<Integer>, Data> graph = Graph.<NodeOperator<Integer>, Data>builder()
                .addNode("a1", "a1", new NodeOperator<>(v -> {
                    return 1;
                }))
                .addNode("a2", "a2", new NodeOperator<>(v -> {
                    integer.set(v + 2);
                    return integer.get();
                }))
                .addNode("a3", "a3", new NodeOperator<>(v -> {
                    integer.set(v + 3);
                    return integer.get();
                }))
                .addEdge("a1", "a2")
                .addEdge("a2", "a3")
                //.addEdge("a3", "a2")
                .create();
        graph.searchRuleRoute(route -> {
            NodeOperator<Integer> parentNode = route.getLastNode().getData();
            route.getEndNode().getData().action(parentNode);
            checkState(route.containsDeadRecursion(), "The Graph contains Dead Recursion: " + route);
            return true;
        });

        graph.printShow().forEach(System.out::println);
        Assert.assertEquals(6, integer.get());
    }
}
