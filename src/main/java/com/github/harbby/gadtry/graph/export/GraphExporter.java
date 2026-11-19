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

import com.github.harbby.gadtry.graph.Graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Base class of every graph export format. Subclasses only implement {@link #write(Writer)};
 * all output targets are derived from it and always use UTF-8.
 * <p>
 * The {@code writeTo} methods flush but never close the given stream or writer.
 */
public abstract class GraphExporter<N, E>
{
    protected final Graph<N, E> graph;

    protected GraphExporter(Graph<N, E> graph)
    {
        this.graph = requireNonNull(graph, "graph is null");
    }

    protected abstract void write(Writer writer)
            throws IOException;

    public final void writeTo(Writer writer)
            throws IOException
    {
        requireNonNull(writer, "writer is null");
        write(new NonClosingWriter(writer));
        writer.flush();
    }

    public final void writeTo(OutputStream outputStream)
            throws IOException
    {
        requireNonNull(outputStream, "outputStream is null");
        writeTo(new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8)));
    }

    /**
     * Writes to {@code path}, creating missing parent directories and replacing an existing file.
     */
    public final void save(Path path)
            throws IOException
    {
        requireNonNull(path, "path is null");
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(path, UTF_8)) {
            writeTo(writer);
        }
    }

    public final void save(File file)
            throws IOException
    {
        requireNonNull(file, "file is null");
        save(file.toPath());
    }

    public final String asString()
    {
        StringWriter writer = new StringWriter();
        try {
            writeTo(writer);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return writer.toString();
    }

    private static final class NonClosingWriter
            extends FilterWriter
    {
        private NonClosingWriter(Writer out)
        {
            super(out);
        }

        @Override
        public void close()
                throws IOException
        {
            flush();
        }
    }
}
