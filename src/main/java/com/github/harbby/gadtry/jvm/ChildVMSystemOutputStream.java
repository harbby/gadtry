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

import java.io.PrintStream;

import static com.github.harbby.gadtry.jvm.JVMLauncherImpl.VM_HEADER;

public class ChildVMSystemOutputStream
        extends PrintStream
{
    private final PrintStream out;
    private volatile boolean tryClose;

    public ChildVMSystemOutputStream(PrintStream out)
    {
        super(out);
        this.out = out;
    }

    public void writeVmHeader()
    {
        out.write(VM_HEADER, 0, VM_HEADER.length);
    }

    @Override
    public void write(int b)
    {
        if (tryClose) {
            return;
        }
        this.writeInt(1);
        out.write(b);
    }

    @Override
    public void write(byte[] b)
    {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] buf, int off, int len)
    {
        if (tryClose) {
            return;
        }
        this.writeInt(len - off);
        out.write(buf, off, len);
        out.flush();
    }

    private void writeInt(int v)
    {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v) & 0xFF);
    }

    public void release(boolean failed, byte[] resultBytes)
    {
        tryClose = true;

        this.writeInt(failed ? -2 : -1);
        this.writeInt(resultBytes.length);
        out.write(resultBytes, 0, resultBytes.length);
        out.flush();
    }
}
