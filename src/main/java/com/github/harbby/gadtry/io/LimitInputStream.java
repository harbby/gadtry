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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public final class LimitInputStream
        extends FilterInputStream
{
    private long left;
    private long mark = -1L;

    public LimitInputStream(InputStream in, long limit)
    {
        super(requireNonNull(in));
        checkState(limit >= 0L, "limit must be non-negative");
        this.left = limit;
    }

    public int available()
            throws IOException
    {
        return (int) Math.min(this.in.available(), this.left);
    }

    @Override
    public boolean markSupported()
    {
        return in.markSupported();
    }

    public synchronized void mark(int readLimit)
    {
        this.in.mark(readLimit);
        this.mark = this.left;
    }

    public int read()
            throws IOException
    {
        if (this.left == 0L) {
            return -1;
        }
        else {
            int result = this.in.read();
            if (result != -1) {
                this.left--;
            }

            return result;
        }
    }

    public int read(byte[] b, int off, int len)
            throws IOException
    {
        if (len == 0) {
            return 0;
        }
        else if (this.left == 0L) {
            return -1;
        }
        else {
            len = (int) Math.min(len, this.left);
            int result = this.in.read(b, off, len);
            if (result != -1) {
                this.left -= result;
            }

            return result;
        }
    }

    public synchronized void reset()
            throws IOException
    {
        if (!this.in.markSupported()) {
            throw new IOException("Mark not supported");
        }
        else if (this.mark == -1L) {
            throw new IOException("Mark not set");
        }
        else {
            this.in.reset();
            this.left = this.mark;
        }
    }

    public long skip(long n)
            throws IOException
    {
        long min = Math.min(n, this.left);
        long skipped = this.in.skip(min);
        this.left = this.left - skipped;
        return skipped;
    }
}
