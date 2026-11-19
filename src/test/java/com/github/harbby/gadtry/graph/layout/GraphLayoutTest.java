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
package com.github.harbby.gadtry.graph.layout;

import com.github.harbby.gadtry.graph.Graph;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.export.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphLayoutTest
{
    private static final LayoutOptions LR = new LayoutOptions(Direction.LEFT_TO_RIGHT, 100, 40, 160, 60);
    private static final LayoutOptions TB = new LayoutOptions(Direction.TOP_TO_BOTTOM, 100, 40, 160, 60);

    private static final Graph<String, Void> DAG = Graph.<String, Void>builder()
            .addEdge("a", "b")
            .addEdge("a", "c")
            .addEdge("b", "d")
            .addEdge("c", "d")
            .addEdge("a", "d")
            .addEdge("d", "e")
            .addEdge("x", "e")
            .create();

    private static final Graph<String, Void> MIXED = Graph.<String, Void>builder()
            .addEdge("a", "b")
            .addEdge("b", "c")
            .addEdge("c", "a")
            .addEdge("c", "d")
            .addEdge("d", "d")
            .addNode("lonely")
            .addEdge("f", "g")
            .create();

    @Test
    void sugiyamaKeepsEdgeDirectionLeftToRight()
    {
        Map<String, Point> points = GraphLayouts.sugiyama().layout(DAG, LR);
        assertComplete(DAG, points);
        for (GraphEdge<String, Void> edge : DAG.edges()) {
            Assertions.assertTrue(points.get(edge.getInNode().getValue()).getX() < points.get(edge.getOutNode().getValue()).getX(), edge.toString());
        }
    }

    @Test
    void sugiyamaKeepsEdgeDirectionTopToBottom()
    {
        Map<String, Point> points = GraphLayouts.sugiyama().layout(DAG, TB);
        assertComplete(DAG, points);
        for (GraphEdge<String, Void> edge : DAG.edges()) {
            Assertions.assertTrue(points.get(edge.getInNode().getValue()).getY() < points.get(edge.getOutNode().getValue()).getY(), edge.toString());
        }
    }

    @Test
    void sugiyamaHandlesCyclesSelfLoopsAndComponents()
    {
        Map<String, Point> points = GraphLayouts.sugiyama().layout(MIXED, LR);
        assertComplete(MIXED, points);
        Assertions.assertEquals(points, GraphLayouts.sugiyama().layout(MIXED, LR));
    }

    @Test
    void sugiyamaEmptyGraph()
    {
        Graph<String, Void> empty = Graph.<String, Void>builder().create();
        Assertions.assertTrue(GraphLayouts.sugiyama().layout(empty, LR).isEmpty());
    }

    @Test
    void sugiyamaRemovesSimpleCrossing()
    {
        Graph<String, Void> graph = Graph.<String, Void>builder()
                .addEdge("a1", "b2")
                .addEdge("a2", "b1")
                .addEdge("r", "a1")
                .addEdge("r", "a2")
                .create();
        Map<String, Point> points = GraphLayouts.sugiyama().layout(graph, LR);
        boolean aOrder = points.get("a1").getY() < points.get("a2").getY();
        boolean bOrder = points.get("b2").getY() < points.get("b1").getY();
        Assertions.assertEquals(aOrder, bOrder);
    }

    @Test
    void dotLayoutWhenAvailable()
    {
        DotLayout dot = new DotLayout();
        Assumptions.assumeTrue(dot.isAvailable(), "graphviz dot not installed");
        Map<String, Point> points = dot.layout(DAG, LR);
        assertComplete(DAG, points);
        for (GraphEdge<String, Void> edge : DAG.edges()) {
            Assertions.assertTrue(points.get(edge.getInNode().getValue()).getX() < points.get(edge.getOutNode().getValue()).getX(), edge.toString());
        }
        assertComplete(MIXED, dot.layout(MIXED, TB));
    }

    @Test
    void autoFallsBackToSugiyamaWithoutDot()
    {
        DotLayout missing = new DotLayout("/nonexistent/gadtry/dot", 1000);
        Assertions.assertFalse(missing.isAvailable());
        Assertions.assertEquals(GraphLayouts.sugiyama().layout(MIXED, LR), GraphLayouts.auto(missing).layout(MIXED, LR));
    }

    private static void assertComplete(Graph<String, Void> graph, Map<String, Point> points)
    {
        Assertions.assertEquals(graph.nodeCount(), points.size());
        List<String> names = new ArrayList<>(points.keySet());
        for (int i = 0; i < names.size(); i++) {
            Point p = points.get(names.get(i));
            Assertions.assertTrue(graph.containsNode(names.get(i)));
            for (int j = i + 1; j < names.size(); j++) {
                Point q = points.get(names.get(j));
                boolean apart = Math.abs(p.getX() - q.getX()) >= 100 - 1e-6 || Math.abs(p.getY() - q.getY()) >= 40 - 1e-6;
                Assertions.assertTrue(apart, names.get(i) + " overlaps " + names.get(j));
            }
        }
    }
}
