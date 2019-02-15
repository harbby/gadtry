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

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class GraphxTest
{
    @Test
    public void test1()
            throws Exception
    {
        Graph<Void> graph = Graph.<Void>builder()
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

        //graph.run();
        TimeUnit.MILLISECONDS.sleep(300);
        graph.printShow();
    }
}
