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
package com.github.harbby.gadtry.graph.impl;

import com.github.harbby.gadtry.graph.Graph;
import org.junit.Test;

import java.util.Random;

public class GraphUtilTest
{
    @Test
    public void noStackOverflowErrorPrintShow()
    {
        Graph.GraphBuilder<Integer, Void> graphBuilder = Graph.builder();
        Random random = new Random();
        for (int i = 0; i < 4000; i++) {
            graphBuilder.addNode(i + "", i);
        }
        for (int i = 0; i < 3999; i++) {
            graphBuilder.addEdge(i + "", i + 1 + "");
        }
        graphBuilder.create().printShow().forEach(x -> System.out.println(x));
    }
}
