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

import com.github.harbby.gadtry.io.IOUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.github.harbby.gadtry.jvm.JVMLauncherImpl.VM_HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class ChildVMChannelInputStream
        extends InputStream
        implements Closeable
{
    private final InputStream in;
    private final BufferedReader reader;
    private int length = 0;
    private int index = 0;
    private byte[] result = new byte[0];
    private boolean isSuccess;
    private boolean isDone = false;

    public ChildVMChannelInputStream(InputStream inputStream)
            throws IOException
    {
        this.in = requireNonNull(inputStream, "inputStream is null");
        this.reader = new BufferedReader(new InputStreamReader(this));
        this.checkVMHeader();
    }

    private void checkVMHeader()
            throws IOException
    {
        //check child state
        byte[] bytes = IOUtils.readNBytes(in, VM_HEADER.length);
        if (!Arrays.equals(VM_HEADER, bytes)) {
            //check child jvm header failed
            byte[] failedByes = IOUtils.readAllBytes(in);
            this.isDone = true;
            this.isSuccess = false;
            byte[] errorMsg = com.github.harbby.gadtry.base.Arrays.mergeByPrimitiveArray(bytes, failedByes);
            throw new JVMException(new String(errorMsg, UTF_8));
        }
    }

    private final ByteBuffer builder = ByteBuffer.allocate(10_0000);

    @Override
    public int read()
            throws IOException
    {
        if (isDone) {
            return in.read();
        }
        if (index < length) {
            index++;
            return in.read();
        }
        this.length = readInt(); //don't is 0

        if (length > 0) {
            this.index = 1;
            return in.read();
        }
        isDone = true;
        if (this.length == -1) {
            this.isSuccess = true;
            int len = this.readInt();
            this.result = IOUtils.readLengthBytes(in, len);
            return in.read();
        }
        else if (this.length == -2) {
            int len = this.readInt();
            this.result = IOUtils.readLengthBytes(in, len);
            this.isSuccess = false;
            return in.read();
        }
        else {
            throw new UnsupportedEncodingException("Protocol error " + this.length);
        }
    }

    @Override
    public int read(byte[] b, int off, int len)
            throws IOException
    {
        if (b == null) {
            throw new NullPointerException();
        }
        else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        else if (len == 0) {
            return 0;
        }

        int c = this.read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;

        int i = 1;
        for (; i < len && c != '\n' && c != '\r'; i++) {
            c = this.read();
            if (c == -1) {
                break;
            }
            b[off + i] = (byte) c;
        }
        return i;
    }

    private int readInt()
            throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            //child is System.exit() ?
            throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    public String readLine()
            throws IOException
    {
        return reader.readLine();
    }

    public byte[] readResult()
    {
        return result;
    }

    public boolean isSuccess()
    {
        return isSuccess;
    }

    @Override
    public void close()
            throws IOException
    {
        in.close();
    }
}
