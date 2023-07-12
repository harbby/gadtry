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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.github.harbby.gadtry.graph.GraphxTest.JAVA_EXCEPTION_GRAPH;

public class GraphSaveAsCanvasTest
{
    private final String bashPath = "/home/ideal/Documents/Obsidian Vault";

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
                        node.putConf("color", "1");
                    }
                })
                .visitEdge(edge -> {
                    if ("a".equals(edge.getInput()) && "c".equals(edge.getOutput())) {
                        edge.putConf("label", "red_edge");
                        edge.putConf("color", "1");
                    }
                })
                .save(new File(bashPath, "4.canvas"));
    }
}
