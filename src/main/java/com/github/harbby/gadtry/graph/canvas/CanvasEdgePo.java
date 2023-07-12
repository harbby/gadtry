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

import java.util.Map;

@JsonSerialize(using = JsonSerializers.CanvasEdgePoSerializer.class)
public class CanvasEdgePo<N, E>
{
    private final N input;
    private final N output;
    private final E value;
    final Map<String, Object> edgeConfig;

    public CanvasEdgePo(N input, N output, E value, Map<String, Object> edgeConfig)
    {
        this.input = input;
        this.output = output;
        this.value = value;
        this.edgeConfig = edgeConfig;
    }

    public N getInput()
    {
        return input;
    }

    public N getOutput()
    {
        return output;
    }

    public E getValue()
    {
        return value;
    }

    public void putConf(String key, Object value)
    {
        edgeConfig.put(key, value);
    }
}
