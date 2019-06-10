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

import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;
import com.github.harbby.gadtry.graph.Route;
import org.junit.Assert;
import org.junit.Test;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class RouteImplTest
{
    @Test
    public void checkNonDeadLoop()
    {
        Route<Void, Void> route = createTestRoute("1", "9", "1");
        Assert.assertTrue(route.findDeadLoop());
    }

    @Test
    public void edgeToString()
    {
        Edge edge = Edge.createEdge(Node.builder("1").build(), Node.builder("2").build());
        Assert.assertEquals(edge.toString(), "EdgeImpl{inNode=node:1, outNode=node:2, edgeData=null}");
    }

    public static Route<Void, Void> createTestRoute(String... ids)
    {
        checkState(ids.length > 0);
        Route.Builder<Void, Void> builder = Route.builder(Node.<Void, Void>builder(ids[0]).build());
        for (int i = 1; i < ids.length; i++) {
            builder.add(Edge.createEdge(Node.<Void, Void>builder(ids[i - 1]).build(), Node.<Void, Void>builder(ids[i]).build()));
        }

        return builder.create();
    }
}
