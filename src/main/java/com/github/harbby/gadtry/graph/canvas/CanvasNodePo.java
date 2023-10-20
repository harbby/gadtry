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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class CanvasNodePo
{
    private final String id;
    private final Map<String, Object> nodeConfig = new HashMap<>();

    public CanvasNodePo(String id)
    {
        this.id = id;
        this.putConf("id", id);
    }

    @Override
    public String toString()
    {
        return id;
    }

    @JsonIgnore
    public void setColor(String color)
    {
        this.putConf("color", color);
    }

    @JsonIgnore
    public void setLabel(String label)
    {
        this.putConf("text", label);
    }

    @JsonIgnore
    public void putConf(String key, Object value)
    {
        nodeConfig.put(key, value);
    }

    @JsonAnyGetter
    private Map<String, Object> getNodeConfig()
    {
        return nodeConfig;
    }
}
