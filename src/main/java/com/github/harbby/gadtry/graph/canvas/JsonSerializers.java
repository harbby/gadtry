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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class JsonSerializers
{
    static class CanvasEdgePoSerializer
            extends JsonSerializer<CanvasEdgePo<?, ?>>
    {
        @Override
        public void serialize(CanvasEdgePo<?, ?> canvasEdgePo, JsonGenerator gen, SerializerProvider serializers)
                throws IOException
        {
            serializeMap(gen, canvasEdgePo.edgeConfig);
        }
    }

    static class CanvasNodePoSerializer
            extends JsonSerializer<CanvasNodePo<?, ?>>
    {
        @Override
        public void serialize(CanvasNodePo<?, ?> canvasNodePo, JsonGenerator gen, SerializerProvider serializers)
                throws IOException
        {
            serializeMap(gen, canvasNodePo.nodeConfig);
        }
    }

    private static void serializeMap(JsonGenerator jsonGenerator, Map<String, Object> map)
            throws IOException
    {
        jsonGenerator.writeStartObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            jsonGenerator.writeObjectField(entry.getKey(), entry.getValue());
        }
        jsonGenerator.writeEndObject();
    }
}
