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
package com.github.harbby.gadtry.io;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class ByteBufferInputStream
        extends InputStream
{
    private final ByteBuffer[] buffers;
    private int index;
    private int mark;
    private int markIndex;

    public ByteBufferInputStream(ByteBuffer... buffers)
    {
        requireNonNull(buffers, "buffers is null");
        this.buffers = Stream.of(buffers).filter(Buffer::hasRemaining).toArray(ByteBuffer[]::new);
    }

    @Override
    public int available()
    {
        return buffers[index].remaining();
    }

    /**
     * @return byte.toInt
     * Returns:
     * the next byte of data, or -1 if the end of the stream is reached.
     * Throws:
     * IOException – if an I/O error occurs.
     */
    @Override
    public int read()
    {
        if (!buffers[index].hasRemaining()) {
            if (index < buffers.length - 1) {
                index++;
            }
            else {
                return -1;
            }
        }
        return buffers[index].get() & 0xFF;
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        this.markIndex = index;
        this.mark = buffers[index].position();
    }

    @Override
    public synchronized void reset()
    {
        for (int i = markIndex; i < buffers.length; i++) {
            buffers[i].position(0);
        }
        this.index = markIndex;
        buffers[markIndex].position(mark);
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }
}
