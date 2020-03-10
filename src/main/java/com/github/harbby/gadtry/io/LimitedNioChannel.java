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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class LimitedNioChannel
        implements SeekableByteChannel
{
    private final SeekableByteChannel readChannel;
    private long left;

    public LimitedNioChannel(SeekableByteChannel readChannel, long limit)
    {
        checkState(limit >= 0L, "limit must be non-negative");
        this.readChannel = requireNonNull(readChannel, "readChannel is null");
        this.left = limit;
    }

    public LimitedNioChannel(SeekableByteChannel readChannel, long position, long limit)
            throws IOException
    {
        this(readChannel, limit);
        readChannel.position(position);
    }

    @Override
    public int read(ByteBuffer dst)
            throws IOException
    {
        if (this.left == 0L) {
            return -1;
        }
        else {
            int result;
            if (dst.remaining() > this.left) {
                ByteBuffer tmp = ByteBuffer.allocate((int) this.left);
                result = this.readChannel.read(tmp);
                tmp.flip();
                dst.put(tmp);
            }
            else {
                result = this.readChannel.read(dst);
            }

            if (result != -1) {
                this.left -= (long) result;
            }

            return result;
        }
    }

    @Override
    public int write(ByteBuffer src)
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position()
            throws IOException
    {
        return readChannel.position();
    }

    @Override
    public SeekableByteChannel position(long newPosition)
            throws IOException
    {
        return readChannel.position(newPosition);
    }

    @Override
    public long size()
            throws IOException
    {
        return (int) Math.min((long) this.readChannel.size(), this.left);
    }

    @Override
    public SeekableByteChannel truncate(long size)
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen()
    {
        return readChannel.isOpen();
    }

    @Override
    public void close()
            throws IOException
    {
        readChannel.close();
    }
}
