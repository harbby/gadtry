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
package com.github.harbby.gadtry.jvm;

import java.io.IOException;
import java.io.OutputStream;

public class SystemOutputStream
        extends OutputStream
{
    private final OutputStream out;
    private volatile boolean tryClose;

    public SystemOutputStream(OutputStream out)
    {
        this.out = out;
    }

    @Override
    public void write(int b)
            throws IOException
    {
        this.write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b)
            throws IOException
    {
        this.write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len)
            throws IOException
    {
        if (tryClose) {
            return;
        }
        out.write(1);
        this.writeInt(len - off);
        out.write(buf, off, len);
        out.flush();
    }

    private void writeInt(int v)
            throws IOException
    {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v) & 0xFF);
    }

    @Override
    public void close()
            throws IOException
    {
        tryClose = true;
    }

    public synchronized void release(byte[] resultBytes)
            throws IOException
    {
        out.write(0);
        this.writeInt(resultBytes.length);
        out.write(resultBytes);
        out.flush();
    }
}
