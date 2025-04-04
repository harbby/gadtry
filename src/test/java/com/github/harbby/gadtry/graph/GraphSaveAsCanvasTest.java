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

import com.github.harbby.gadtry.graph.canvas.ProcessOptimizer;
import com.github.harbby.gadtry.graph.canvas.SaveFileBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.github.harbby.gadtry.graph.GraphxTest.JAVA_EXCEPTION_GRAPH;

public class GraphSaveAsCanvasTest
{
    private final String bashPath = "/tmp/graphs";

    @Test
    void save_exception_graph_as_canvas_test()
            throws IOException
    {
        JAVA_EXCEPTION_GRAPH.saveAsCanvas(new File(bashPath, "1.canvas"));
    }

    @Test
    void save_graph_as_canvas_test2()
            throws IOException
    {
        GraphDemoTest.TRAIN_GRAPH.saveAsCanvas(new File(bashPath, "2.canvas"));
    }

    @Test
    void moreInputParentTree_as_canvas()
            throws IOException
    {
        Graph<String, ?> graph = Graph.<String, Object>builder()
                .addNode("a")
                .addNode("b")
                .addNode("c")
                .addNode("e")
                .addEdge("a", "b")
                .addEdge("a", "c", 1)
                .addEdge("c", "b")
                .addEdge("c", "e")
                .addEdge("b", "e")
                .create();
        graph.printShow().forEach(System.out::println);
        graph.saveAsCanvas()
                .NodeColor("4")
                .nodeWidth(150)
                .nodeHeight(50)
                .xSpacingFactor(2)
                .ySpacingFactor(4)
                .visitNode(node -> {
                    if ("a".equals(node.getNode())) {
                        node.getInfo().setColor("1");
                    }
                })
                .visitEdge(edge -> {
                    if ("a".equals(edge.getInput()) && "c".equals(edge.getOutput())) {
                        edge.getInfo().setLabel("red_edge");  //"label"
                        edge.getInfo().setColor("1");
                    }
                })
                .save(new File(bashPath, "4.canvas"));
    }

    @Test
    public void flowDepthOptimize1()
            throws IOException
    {
        Graph<String, Void> graph = Graph.<String, Void>builder()
                .addEdge("a", "b")
                .addEdge("b", "c")
                .addEdge("a", "c")
                .create();
        graph.saveAsCanvas()
                .direction(SaveFileBuilder.Direction.up_to_down)
                .strategy(ProcessOptimizer.Strategy.DEFAULT)
                .save(new File(bashPath, "5.canvas"));
    }

    @Test
    public void flowDepthOptimize2()
            throws IOException
    {
        Graph<String, Void> graph = Graph.<String, Void>builder()
                .addEdge("p1", "a")
                .addEdge("a", "b")
                .addEdge("p2", "c")
                .addEdge("c", "a")
                .create();
        graph.saveAsCanvas().save(new File(bashPath, "6.canvas"));
    }

    @Test
    public void flowLoopDepthOptimize3()
            throws IOException
    {
        Graph<String, Void> graph = Graph.<String, Void>builder()
                .addEdge("a", "b")
                .addEdge("b", "c")
                .addEdge("c", "a")
                .create();
        graph.saveAsCanvas().save(new File(bashPath, "7.canvas"));
    }

    @Test
    void dotDemo()
            throws IOException
    {
        Graph<String, Void> graph = Graph.<String, Void>builder()
                .addNode("0")
                .addEdge("1", "3")
                .addEdge("2", "4")
                .addEdge("3", "5")
                .addEdge("3", "6")
                .addEdge("3", "7")
                .addEdge("3", "8")
                .addEdge("3", "9")
                .addEdge("3", "10")
                .addEdge("4", "11")
                .addEdge("5", "12")
                .addEdge("6", "12")
                .addEdge("7", "12")
                .addEdge("8", "12")
                .addEdge("9", "12")
                .addEdge("10", "12")
                .addEdge("11", "13")
                .addEdge("12", "14")
                .addEdge("13", "12")
                .addEdge("14", "15")
                .addEdge("15", "16")
                .create();
        graph.saveAsCanvas().save(new File(bashPath, "8.canvas"));
        /*
        digraph G {
            rankdir=LR;
            "0";
            "1" -> "3";
            "2" -> "4";
            "3" -> "5";
            "3" -> "6";
            "3" -> "7";
            "3" -> "8";
            "3" -> "9";
            "3" -> "10";
            "4" -> "11";
            "5" -> "12";
            "6" -> "12";
            "7" -> "12";
            "8" -> "12";
            "9" -> "12";
            "10" -> "12";
            "11" -> "13";
            "12" -> "14";
            "13" -> "12";
            "14" -> "15";
            "15" -> "16";
        }
        */
    }
}
