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
package com.github.harbby.gadtry.graph.export;

import com.github.harbby.gadtry.graph.canvas.CanvasBuilder;
import com.github.harbby.gadtry.graph.drawio.DrawioBuilder;

/**
 * Built-in export formats, used with {@code graph.export(GraphFormats.json())}.
 * <p>
 * json, dot, graphml and mermaid have no runtime dependency.
 * canvas and drawio compute a layout and need jackson (and jackson-dataformat-xml for drawio) on the classpath.
 */
public final class GraphFormats
{
    private GraphFormats() {}

    public static <N, E> GraphFormat<N, E, JsonExporter<N, E>> json()
    {
        return JsonExporter::new;
    }

    public static <N, E> GraphFormat<N, E, DotExporter<N, E>> dot()
    {
        return DotExporter::new;
    }

    public static <N, E> GraphFormat<N, E, GraphMLExporter<N, E>> graphml()
    {
        return GraphMLExporter::new;
    }

    public static <N, E> GraphFormat<N, E, MermaidExporter<N, E>> mermaid()
    {
        return MermaidExporter::new;
    }

    /**
     * Obsidian JSON Canvas ({@code .canvas}).
     */
    public static <N, E> GraphFormat<N, E, CanvasBuilder<N, E>> canvas()
    {
        return CanvasBuilder::new;
    }

    /**
     * diagrams.net / draw.io ({@code .drawio}).
     */
    public static <N, E> GraphFormat<N, E, DrawioBuilder<N, E>> drawio()
    {
        return DrawioBuilder::new;
    }
}
