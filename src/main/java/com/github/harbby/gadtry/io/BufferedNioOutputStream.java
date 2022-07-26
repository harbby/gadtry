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

import com.github.harbby.gadtry.base.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;

public final class BufferedNioOutputStream
        extends OutputStream
{
    private final ByteBuffer buffer;
    private final SeekableByteChannel channel;
    private long position;

    public BufferedNioOutputStream(SeekableByteChannel channel, int buffSize)
    {
        this(channel, buffSize, true);
    }

    public BufferedNioOutputStream(SeekableByteChannel channel, int buffSize, boolean enablePageAligned)
    {
        checkArgument(buffSize > 0, "Buffer size <= 0");
        this.channel = channel;
        if (enablePageAligned) {
            this.buffer = Platform.allocateDirectBuffer(buffSize, Platform.pageSize());  //Direct memory disk page aligned
        }
        else {
            this.buffer = Platform.allocateDirectBuffer(buffSize);
        }
    }

    public BufferedNioOutputStream(SeekableByteChannel channel)
    {
        this(channel, 8192);
    }

    @Override
    public void write(int b)
            throws IOException
    {
        position++;
        if (buffer.remaining() == 0) {
            this.flush();
        }
        buffer.put((byte) b);
    }

    @Override
    public void write(byte[] b)
            throws IOException
    {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len)
            throws IOException
    {
        position += len;
        int remaining = buffer.remaining();
        if (remaining > len) {
            buffer.put(b, off, len);
            return;
        }
        else if (remaining == len) {
            buffer.put(b, off, len);
            this.flush();
            return;
        }
        //else
        int left = len;
        while (left > remaining) {
            buffer.put(b, off + len - left, remaining);
            this.flush();
            left -= remaining;
            remaining = buffer.remaining();
        }
        buffer.put(b, off + len - left, left);
    }

    @Override
    public void flush()
            throws IOException
    {
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    public final long position()
    {
        return position;
    }

    @Override
    public void close()
            throws IOException
    {
        try (SeekableByteChannel ignored = this.channel) {
            this.flush();
        }
        finally {
            Platform.freeDirectBuffer(buffer);
        }
    }
}
