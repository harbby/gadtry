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
package com.github.harbby.gadtry.graph.drawio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.LinkedHashMap;
import java.util.Map;

@JacksonXmlRootElement(localName = "mxCell")
public class DrawioMxCell
{
    private static final String root0 = "drawio_$0";
    private static final String root1 = "drawio_$1";

    @JacksonXmlProperty(isAttribute = true, localName = "id")
    private String id;
    @JacksonXmlProperty(isAttribute = true, localName = "value")
    private String value;

    @JsonDeserialize(using = DrawioBuilder.XmlMapFieldDeserializer.class)
    @JsonSerialize(using = DrawioBuilder.XmlMapFieldSerializer.class)
    @JacksonXmlProperty(isAttribute = true, localName = "style")
    private Map<String, String> style = new LinkedHashMap<>();

    @JacksonXmlProperty(isAttribute = true, localName = "parent")
    private String parent = root1;

    @JsonDeserialize(using = DrawioBuilder.MxGeometryDeserializer.class)
    @JsonSerialize(using = DrawioBuilder.MxGeometrySerializer.class)
    private Map<String, String> mxGeometry = new LinkedHashMap<>();

    // edge property
    @JacksonXmlProperty(isAttribute = true, localName = "source")
    private String source;
    @JacksonXmlProperty(isAttribute = true, localName = "target")
    private String target;
    @JacksonXmlProperty(isAttribute = true, localName = "edge")
    private String edge;
    // node property
    @JacksonXmlProperty(isAttribute = true, localName = "vertex")
    private String vertex;

    @JsonIgnore
    public void addStyle(String key, String value)
    {
        this.style.put(key, value);
    }

    @JsonIgnore
    public void setLabel(String value)
    {
        this.value = value;
    }

    @JsonIgnore
    public void addMxGeometry(String key, String value)
    {
        this.mxGeometry.put(key, value);
    }

    static DrawioMxCell ofNode(String id)
    {
        DrawioMxCell drawioNode = new DrawioMxCell();
        drawioNode.vertex = root1;
        drawioNode.id = id;
        return drawioNode;
    }

    static DrawioMxCell ofEdge(String sourceID, String targetID)
    {
        DrawioMxCell drawioEdge = new DrawioMxCell();
        drawioEdge.edge = root1;
        drawioEdge.source = sourceID;
        drawioEdge.target = targetID;
        drawioEdge.id = sourceID + " to " + targetID;
        return drawioEdge;
    }

    static DrawioMxCell ofNodeZero()
    {
        DrawioMxCell drawioNode0 = new DrawioMxCell();
        drawioNode0.id = root0;
        drawioNode0.parent = null;
        return drawioNode0;
    }

    static DrawioMxCell ofNodeOne()
    {
        DrawioMxCell drawioNode1 = new DrawioMxCell();
        drawioNode1.id = root1;
        drawioNode1.parent = root0;
        return drawioNode1;
    }
}
