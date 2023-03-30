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

import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.Route;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RouteImplTest
{
    @Test
    public void checkNonDeadLoop()
    {
        GraphNode<String, Void> n1 = GraphNode.of("1");
        GraphNode<String, Void> n9 = GraphNode.of("9");

        Route.Builder<String, Void> builder = Route.builder(n1);
        builder.add(GraphEdge.of(n9));
        builder.add(GraphEdge.of(n1));

        Route<String, Void> route = builder.create();
        Assertions.assertTrue(route.containsLoop());
    }

    @Test
    public void edgeToString()
    {
        GraphEdge<String, Void> edge = GraphEdge.of(GraphNode.of("2"));
        Assertions.assertEquals(edge.toString(), "GraphEdge{out=2, value=null}");
    }
}
