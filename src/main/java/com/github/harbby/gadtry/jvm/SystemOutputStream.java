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

import com.github.harbby.gadtry.base.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class SystemOutputStream
        extends PrintStream
{
    private final OutputStream out;
    private volatile boolean tryClose;

    public SystemOutputStream(OutputStream out)
    {
        super(out);
        this.out = out;
    }

    @Override
    public void write(int b)
    {
        try {
            this.write(new byte[] {(byte) b});
        }
        catch (IOException e) {
            Platform.throwException(e);
        }
    }

    @Override
    public void write(byte[] b)
            throws IOException
    {
        this.write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len)
    {
        if (tryClose) {
            return;
        }
        try {
            out.write(1);
            this.writeInt(len - off);
            out.write(buf, off, len);
            out.flush();
        }
        catch (IOException e) {
            Platform.throwException(e);
        }
    }

    private void writeInt(int v)
            throws IOException
    {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v) & 0xFF);
    }

    public synchronized void release(boolean failed, byte[] resultBytes)
            throws IOException
    {
        tryClose = true;

        out.write(failed ? 2 : 0);
        this.writeInt(resultBytes.length);
        out.write(resultBytes);
        out.flush();
    }
}
