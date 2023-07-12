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
package com.github.harbby.gadtry.graph.canvas;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.harbby.gadtry.graph.GraphNode;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = JsonSerializers.CanvasNodePoSerializer.class)
public class CanvasNodePo<N, E>
{
    final transient GraphNode<N, E> node;
    final int depth;
    final int index;
    final Map<String, Object> nodeConfig = new HashMap<>();

    public CanvasNodePo(GraphNode<N, E> node, int depth, int index)
    {
        this.node = node;
        this.depth = depth;
        this.index = index;
    }

    public final N getNode()
    {
        return node.getValue();
    }

    public final void putConf(String key, Object value)
    {
        nodeConfig.put(key, value);
    }

    @Override
    public String toString()
    {
        return node.toString();
    }
}
