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

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.github.harbby.gadtry.base.MoreObjects;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.canvas.EdgeView;
import com.github.harbby.gadtry.graph.canvas.NodeView;
import com.github.harbby.gadtry.graph.canvas.SaveFileBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

public class DrawioBuilder<N, E>
        extends SaveFileBuilder<N, E, DrawioMxCell, DrawioMxCell>
{
    static final XmlMapper XML_MAPPER = new XmlMapper(new XmlFactory(new WstxInputFactory(), new WstxOutputFactory()), new JacksonXmlModule());

    private boolean enableCompress = true;
    private String nodeFillColor = "#dae8fc";
    private String nodeStrokeColor = "#6c8ebf";

    private String edgeFillColor = "#888888";
    private String edgeStrokeColor = "#888888";

    private String edgeStyle;

    public DrawioBuilder(GraphNode<N, E> root, Map<N, GraphNode<N, E>> nodes)
    {
        super(root, nodes, 200, 40);
    }

    public DrawioBuilder<N, E> nodeWidth(int width)
    {
        this.width = width;
        return this;
    }

    public DrawioBuilder<N, E> nodeHeight(int height)
    {
        this.height = height;
        return this;
    }

    public DrawioBuilder<N, E> compressed(boolean enableCompress)
    {
        this.enableCompress = enableCompress;
        return this;
    }

    public DrawioBuilder<N, E> nodeFillColor(String color)
    {
        this.nodeFillColor = color;
        return this;
    }

    public DrawioBuilder<N, E> nodeStrokeColor(String color)
    {
        this.nodeStrokeColor = color;
        return this;
    }

    public DrawioBuilder<N, E> edgeFillColor(String color)
    {
        this.edgeFillColor = color;
        return this;
    }

    public DrawioBuilder<N, E> edgeStrokeColor(String color)
    {
        this.edgeStrokeColor = color;
        return this;
    }

    public DrawioBuilder<N, E> visitNode(Consumer<NodeView<N, DrawioMxCell>> consumer)
    {
        this.nodeVisitor = requireNonNull(consumer, "nodeVisitor is null");
        return this;
    }

    public DrawioBuilder<N, E> visitEdge(Consumer<EdgeView<N, E, DrawioMxCell>> consumer)
    {
        this.edgeVisitor = requireNonNull(consumer, "nodeVisitor is null");
        return this;
    }

    public DrawioBuilder<N, E> xSpacingFactor(double xSpacingFactor)
    {
        this.xSpacingFactor = xSpacingFactor;
        return this;
    }

    public DrawioBuilder<N, E> ySpacingFactor(double ySpacingFactor)
    {
        this.ySpacingFactor = ySpacingFactor;
        return this;
    }

    public DrawioBuilder<N, E> edgeStyle(String edgeStyle)
    {
        this.edgeStyle = requireNonNull(edgeStyle, "edgeStyle is null");
        return this;
    }

    @Override
    public DrawioBuilder<N, E> direction(Direction direction)
    {
        super.direction(direction);
        return this;
    }

    @Override
    protected DrawioMxCell createEdgeView(GraphNode<N, E> from, GraphEdge<N, E> edge)
    {
        String sourceID = this.idSelector.apply(from.getValue());
        String targetID = this.idSelector.apply(edge.getOutNode().getValue());
        String edgeValue = edge.getValue() == null ? null : edge.getValue().toString();
        DrawioMxCell drawioEdge = DrawioMxCell.ofEdge(sourceID, targetID);
        drawioEdge.setLabel(edgeValue);

        drawioEdge.addMxGeometry("as", "geometry");
        drawioEdge.addMxGeometry("relative", "1");
        edgeStyle = MoreObjects.getNonNull(edgeStyle, () -> direction == Direction.left_to_right ? "entityRelationEdgeStyle" : "elbowEdgeStyle");
        switch (edgeStyle) {
            case "entityRelationEdgeStyle":
                drawioEdge.addStyle("edgeStyle", edgeStyle);
                drawioEdge.addStyle("curved", "1");
                break;
            case "elbowEdgeStyle":
            case "orthogonalEdgeStyle":
                if (direction == Direction.left_to_right) {
                    drawioEdge.addStyle("edgeStyle", "elbowEdgeStyle");
                }
                else {
                    drawioEdge.addStyle("edgeStyle", "orthogonalEdgeStyle");
                }
                drawioEdge.addStyle("rounded", "1");
                break;
            default:
                drawioEdge.addStyle("edgeStyle", edgeStyle);
                drawioEdge.addStyle("curved", "0");
        }
        if (direction == Direction.left_to_right) {
            drawioEdge.addStyle("exitX", "1");
            drawioEdge.addStyle("exitY", "0.5");
            drawioEdge.addStyle("entryX", "0");
            drawioEdge.addStyle("entryY", "0.5");
        }
        else {
            drawioEdge.addStyle("exitX", "0.5");
            drawioEdge.addStyle("exitY", "1");
            drawioEdge.addStyle("entryX", "0.5");
            drawioEdge.addStyle("entryY", "0");
        }

        drawioEdge.addStyle("fillColor", edgeFillColor);
        drawioEdge.addStyle("strokeColor", edgeStrokeColor);
        return drawioEdge;
    }

    @Override
    protected DrawioMxCell createNodeView(GraphNode<N, E> node, int x, int y)
    {
        String id = this.idSelector.apply(node.getValue());
        DrawioMxCell drawioNode = DrawioMxCell.ofNode(id);
        drawioNode.setLabel(String.valueOf(node.getValue()));

        drawioNode.addMxGeometry("x", String.valueOf(x));
        drawioNode.addMxGeometry("y", String.valueOf(y));
        drawioNode.addMxGeometry("width", String.valueOf(this.width));
        drawioNode.addMxGeometry("height", String.valueOf(this.height));
        drawioNode.addMxGeometry("as", "geometry");
        drawioNode.addStyle("rounded", "1");
        drawioNode.addStyle("fillColor", nodeFillColor);
        drawioNode.addStyle("strokeColor", nodeStrokeColor);
        return drawioNode;
    }

    @Override
    protected void serialize(CanvasGraphPO<DrawioMxCell, DrawioMxCell> graphPO, File path)
            throws IOException
    {
        DrawioMxfile drawioMxfile = new DrawioMxfile();
        drawioMxfile.compressed = this.enableCompress;
        drawioMxfile.diagram = new DrawioDiagram();
        drawioMxfile.diagram.pageName = "page-1";
        drawioMxfile.diagram.enableCompress = this.enableCompress;

        List<DrawioMxCell> root = new ArrayList<>();
        root.add(DrawioMxCell.ofNodeZero());
        root.add(DrawioMxCell.ofNodeOne());
        //--------
        root.addAll(graphPO.getEdges());
        root.addAll(graphPO.getNodes());
        MxGraphModel mxGraphModel = new MxGraphModel();
        mxGraphModel.root = root;
        drawioMxfile.diagram.mxGraphModel = mxGraphModel;

        XML_MAPPER.writeValue(path, drawioMxfile);
    }

    @JacksonXmlRootElement(localName = "mxGraphModel")
    @JsonSerialize(using = MxGraphModelSerializer.class)
    static class MxGraphModel
    {
        private Map<String, String> attributes;

        @JacksonXmlProperty
        private List<DrawioMxCell> root;

        @JsonAnySetter
        public void anySetter(String key, String value)
        {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            this.attributes.put(key, value);
        }
    }

    static class MxGraphModelSerializer
            extends JsonSerializer<MxGraphModel>
    {
        @Override
        public void serialize(MxGraphModel value, JsonGenerator oldGenerator, SerializerProvider serializers)
                throws IOException
        {
            ToXmlGenerator xmlGenerator = (ToXmlGenerator) oldGenerator;
            Map<String, String> attributes = value.attributes;
            xmlGenerator.writeStartObject();
            if (attributes != null) {
                xmlGenerator.setNextIsAttribute(true);
                for (Map.Entry<String, String> entry : value.attributes.entrySet()) {
                    xmlGenerator.writeStringField(entry.getKey(), entry.getValue());
                }
                xmlGenerator.setNextIsAttribute(false);
            }
            xmlGenerator.writeFieldName("root");
            xmlGenerator.writeStartObject();
            //
            xmlGenerator.writeFieldName("mxCell");
            xmlGenerator.writeStartArray();
            for (DrawioMxCell mxCell : value.root) {
                xmlGenerator.writeObject(mxCell);
            }
            xmlGenerator.writeEndArray();
            xmlGenerator.writeEndObject();
            xmlGenerator.writeEndObject();
        }
    }

    @JsonSerialize(using = DiagramSerializer.class)
    static class DrawioDiagram
    {
        @JsonIgnore
        private boolean enableCompress;

        @JacksonXmlProperty
        private MxGraphModel mxGraphModel;

        @JacksonXmlProperty(isAttribute = true)
        private String id = UUID.randomUUID().toString().replace("-", "");

        @JacksonXmlProperty(isAttribute = true, localName = "name")
        private String pageName;
    }

    private static String getNowTime()
    {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        LocalDateTime localDateTime = LocalDateTime.now();
        return inputFormatter.format(localDateTime);
    }

    @JacksonXmlRootElement(localName = "mxfile")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DrawioMxfile
    {
        @JacksonXmlProperty(isAttribute = true)
        private String version = "16.5.1";

        @JacksonXmlProperty(isAttribute = true)
        private boolean compressed = false;

        @JacksonXmlProperty(isAttribute = true)
        private String modified = getNowTime();

        @JacksonXmlProperty(isAttribute = true)
        private String host = "Electron";

        @JacksonXmlProperty(isAttribute = true)
        private String etag = UUID.randomUUID().toString().replace("-", "");

        @JacksonXmlProperty(isAttribute = true)
        private String type = "device";

        @JacksonXmlProperty
        private DrawioDiagram diagram;
    }

    static class XmlMapFieldSerializer
            extends JsonSerializer<Map<String, String>>
    {
        @Override
        public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException
        {
            if (value == null || value.isEmpty()) {
                gen.writeString((String) null);
                return;
            }
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : value.entrySet()) {
                builder.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
            gen.writeString(builder.toString());
        }
    }

    static class MxGeometrySerializer
            extends JsonSerializer<Map<String, String>>
    {
        @Override
        public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException
        {
            if (value == null || value.isEmpty()) {
                return;
            }
            ToXmlGenerator xmlGenerator = (ToXmlGenerator) gen;
            xmlGenerator.setNextIsAttribute(true);
            xmlGenerator.writeStartObject();
            for (Map.Entry<String, String> entry : value.entrySet()) {
                xmlGenerator.writeStringField(entry.getKey(), entry.getValue());
            }
            xmlGenerator.writeEndObject();
            xmlGenerator.setNextIsAttribute(false);
        }
    }

    static class MxGeometryDeserializer
            extends JsonDeserializer<Map<String, String>>
    {
        @Override
        public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException
        {
            FromXmlParser xmlParser = (FromXmlParser) p;
            return xmlParser.readValueAs(new TypeReference<Map<String, String>>()
            {
            });
        }
    }

    static class XmlMapFieldDeserializer
            extends JsonDeserializer<Map<String, String>>
    {
        @Override
        public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException
        {
            String text = p.getText();
            String[] splitArr = text.split(";");
            if (splitArr.length == 0) {
                return Collections.emptyMap();
            }
            Map<String, String> map = new LinkedHashMap<>();
            for (String kv : splitArr) {
                String[] kvSplit = kv.split("=");
                checkState(kvSplit.length == 2, "Encountered wrong k=v configuration: " + kv);
                map.put(kvSplit[0], kvSplit[1]);
            }
            return map;
        }
    }

    static class DiagramSerializer
            extends JsonSerializer<DrawioDiagram>
    {
        @Override
        public void serialize(DrawioDiagram value, JsonGenerator oldGenerator, SerializerProvider serializers)
                throws IOException
        {
            final ToXmlGenerator xmlGenerator = (ToXmlGenerator) oldGenerator;
            xmlGenerator.setNextIsAttribute(true);
            xmlGenerator.writeStartObject();
            xmlGenerator.writeStringField("id", value.id);
            xmlGenerator.writeStringField("pageName", value.pageName);
            xmlGenerator.setNextIsAttribute(false);

            ByteArrayOutputStream outputStream = null;
            if (value.enableCompress) {
                outputStream = new ByteArrayOutputStream();
                ToXmlGenerator tempGenerator = XML_MAPPER.getFactory().createGenerator(outputStream);
                tempGenerator.writeObject(value.mxGraphModel);
                tempGenerator.close();
                String xml = outputStream.toString();
                String rs = mxGraphModelEncode(xml);
                xmlGenerator.writeRaw(rs);
            }
            else {
                xmlGenerator.writeObjectField("mxGraphModel", value.mxGraphModel);
            }
            xmlGenerator.writeEndObject();
        }
    }

    static String mxGraphModelEncode(String xml)
    {
        String urlEncoded = URLEncoder.encode(xml, StandardCharsets.UTF_8).replace("+", "%20");
        Deflater compresser = new Deflater(DEFAULT_COMPRESSION, true);
        compresser.setInput(urlEncoded.getBytes(StandardCharsets.UTF_8));
        compresser.finish();
        byte[] bytes = new byte[urlEncoded.length()];
        int compressedDataLength = compresser.deflate(bytes);
        compresser.end();
        ByteBuffer buffer = Base64.getEncoder().encode(ByteBuffer.wrap(bytes, 0, compressedDataLength));
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }
}
