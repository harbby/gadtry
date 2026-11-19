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
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.export.Direction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Layout computed by the external graphviz {@code dot} program (plain text output, no extra java dependency).
 * Node sizes and spacing from {@link LayoutOptions} are passed to dot, so the result is in pixels.
 * <p>
 * Failures (missing program, non-zero exit code, timeout) are reported as unchecked exceptions
 * that include dot's stderr. Use {@link GraphLayouts#auto()} to fall back to {@link SugiyamaLayout}.
 */
public final class DotLayout
        implements GraphLayout
{
    private static final double POINTS_PER_INCH = 72;
    private static final long PROBE_TIMEOUT_MILLIS = 5_000;
    private static final Map<String, Boolean> AVAILABLE = new ConcurrentHashMap<>();

    private final String dotPath;
    private final long timeoutMillis;

    public DotLayout()
    {
        this("dot", 60_000);
    }

    /**
     * @param dotPath name on the PATH or absolute path of the dot executable
     * @param timeoutMillis maximum time one layout may take
     */
    public DotLayout(String dotPath, long timeoutMillis)
    {
        this.dotPath = requireNonNull(dotPath, "dotPath is null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive, got " + timeoutMillis);
        }
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * @return true if {@code dot -V} runs successfully; the result is cached per dot path
     */
    public boolean isAvailable()
    {
        return AVAILABLE.computeIfAbsent(dotPath, DotLayout::probe);
    }

    private static boolean probe(String dotPath)
    {
        try {
            return run(Arrays.asList(dotPath, "-V"), "", PROBE_TIMEOUT_MILLIS).exitCode == 0;
        }
        catch (IOException | RuntimeException e) {
            return false;
        }
    }

    @Override
    public <N, E> Map<N, Point> layout(Graph<N, E> graph, LayoutOptions options)
    {
        requireNonNull(graph, "graph is null");
        requireNonNull(options, "options is null");
        List<GraphNode<N, E>> nodes = new ArrayList<>(graph.nodes());
        if (nodes.isEmpty()) {
            return new LinkedHashMap<>();
        }
        String output;
        try {
            ProcessResult result = run(Arrays.asList(dotPath, "-Tplain"), toDot(graph, nodes, options), timeoutMillis);
            if (result.exitCode != 0) {
                throw new IllegalStateException("dot exited with code " + result.exitCode + ": " + result.stderr.trim());
            }
            output = result.stdout;
        }
        catch (IOException e) {
            throw new UncheckedIOException("dot layout failed: " + e.getMessage(), e);
        }
        return parse(output, nodes, options);
    }

    private static <N, E> String toDot(Graph<N, E> graph, List<GraphNode<N, E>> nodes, LayoutOptions options)
    {
        boolean leftToRight = options.getDirection() == Direction.LEFT_TO_RIGHT;
        double rankGap = options.rankStep() - (leftToRight ? options.getNodeWidth() : options.getNodeHeight());
        double nodeGap = options.crossStep() - (leftToRight ? options.getNodeHeight() : options.getNodeWidth());

        Map<N, Integer> ids = new HashMap<>(nodes.size() * 2);
        StringBuilder dot = new StringBuilder("digraph G {\n");
        dot.append("  rankdir=").append(options.getDirection().code()).append(";\n");
        dot.append(String.format(Locale.ROOT, "  ranksep=%.4f;\n  nodesep=%.4f;\n",
                inches(Math.max(rankGap, 1)), inches(Math.max(nodeGap, 1))));
        dot.append(String.format(Locale.ROOT, "  node [shape=box, fixedsize=true, label=\"\", width=%.4f, height=%.4f];\n",
                inches(options.getNodeWidth()), inches(options.getNodeHeight())));
        for (GraphNode<N, E> node : nodes) {
            int id = ids.size();
            ids.put(node.getValue(), id);
            dot.append("  ").append(id).append(";\n");
        }
        for (GraphEdge<N, E> edge : graph.edges()) {
            dot.append("  ").append(ids.get(edge.getInNode().getValue()))
                    .append(" -> ").append(ids.get(edge.getOutNode().getValue())).append(";\n");
        }
        return dot.append("}\n").toString();
    }

    private static double inches(double pixels)
    {
        return pixels / POINTS_PER_INCH;
    }

    /**
     * Plain format: {@code graph scale width height} and {@code node name x y width height ...},
     * centers in inches with the origin at the bottom-left.
     */
    private static <N, E> Map<N, Point> parse(String output, List<GraphNode<N, E>> nodes, LayoutOptions options)
    {
        double graphHeight = 0;
        Map<Integer, double[]> centers = new HashMap<>(nodes.size() * 2);
        for (String line : output.split("\n")) {
            String[] tokens = line.trim().split("\\s+");
            if ("graph".equals(tokens[0]) && tokens.length >= 4) {
                graphHeight = Double.parseDouble(tokens[3]) * POINTS_PER_INCH;
            }
            else if ("node".equals(tokens[0]) && tokens.length >= 4) {
                String name = tokens[1].replace("\"", "");
                centers.put(Integer.parseInt(name), new double[] {
                        Double.parseDouble(tokens[2]) * POINTS_PER_INCH,
                        Double.parseDouble(tokens[3]) * POINTS_PER_INCH});
            }
        }
        Map<N, Point> result = new LinkedHashMap<>(nodes.size() * 2);
        for (int i = 0; i < nodes.size(); i++) {
            double[] center = centers.get(i);
            if (center == null) {
                throw new IllegalStateException("dot output has no position for node " + nodes.get(i).getValue());
            }
            double x = center[0] - options.getNodeWidth() / 2;
            double y = graphHeight - center[1] - options.getNodeHeight() / 2;
            result.put(nodes.get(i).getValue(), Point.of(x, y));
        }
        return result;
    }

    private static ProcessResult run(List<String> command, String input, long timeoutMillis)
            throws IOException
    {
        Process process = new ProcessBuilder(command).start();
        StreamCollector stdout = new StreamCollector(process.getInputStream());
        StreamCollector stderr = new StreamCollector(process.getErrorStream());
        stdout.start();
        stderr.start();
        try {
            try (Writer writer = new OutputStreamWriter(process.getOutputStream(), UTF_8)) {
                writer.write(input);
            }
            if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                throw new IOException(command.get(0) + " timed out after " + timeoutMillis + " ms");
            }
            stdout.join(timeoutMillis);
            stderr.join(timeoutMillis);
            return new ProcessResult(process.exitValue(), stdout.text(), stderr.text());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted while waiting for " + command.get(0));
        }
        finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static final class ProcessResult
    {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        private ProcessResult(int exitCode, String stdout, String stderr)
        {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

    private static final class StreamCollector
            extends Thread
    {
        private final InputStream input;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        private StreamCollector(InputStream input)
        {
            this.input = input;
            setDaemon(true);
        }

        @Override
        public void run()
        {
            byte[] bytes = new byte[8192];
            try (InputStream in = input) {
                int n;
                while ((n = in.read(bytes)) != -1) {
                    synchronized (buffer) {
                        buffer.write(bytes, 0, n);
                    }
                }
            }
            catch (IOException ignored) {
                // the process was killed or closed its stream; keep what was read
            }
        }

        private String text()
        {
            synchronized (buffer) {
                return new String(buffer.toByteArray(), UTF_8);
            }
        }
    }
}
