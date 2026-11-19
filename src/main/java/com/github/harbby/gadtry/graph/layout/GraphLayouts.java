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
package com.github.harbby.gadtry.graph.layout;

import com.github.harbby.gadtry.graph.Graph;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Built-in layouts.
 */
public final class GraphLayouts
{
    private static final SugiyamaLayout SUGIYAMA = new SugiyamaLayout();

    private GraphLayouts() {}

    /**
     * Pure-Java layered layout; works everywhere. This is the default of the canvas and drawio exporters.
     */
    public static GraphLayout sugiyama()
    {
        return SUGIYAMA;
    }

    /**
     * graphviz {@code dot} found on the PATH; fails if it is not installed.
     */
    public static GraphLayout dot()
    {
        return new DotLayout();
    }

    /**
     * graphviz {@code dot} at a custom location.
     */
    public static GraphLayout dot(String dotPath, long timeoutMillis)
    {
        return new DotLayout(dotPath, timeoutMillis);
    }

    /**
     * Uses graphviz {@code dot} when it is available and falls back to {@link #sugiyama()}
     * if it is missing or fails.
     */
    public static GraphLayout auto()
    {
        return auto(new DotLayout());
    }

    public static GraphLayout auto(DotLayout dot)
    {
        requireNonNull(dot, "dot is null");
        return new GraphLayout()
        {
            @Override
            public <N, E> Map<N, Point> layout(Graph<N, E> graph, LayoutOptions options)
            {
                if (dot.isAvailable()) {
                    try {
                        return dot.layout(graph, options);
                    }
                    catch (RuntimeException ignored) {
                        // fall back to the pure-Java layout
                    }
                }
                return SUGIYAMA.layout(graph, options);
            }
        };
    }
}
