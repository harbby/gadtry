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
package com.github.harbby.gadtry.jcodec;

import com.github.harbby.gadtry.base.Strings;
import com.github.harbby.gadtry.io.IOUtils;

import java.io.OutputStream;

import static com.github.harbby.gadtry.StaticAssert.DEBUG;

public abstract class AbstractOutputView
        extends OutputStream
        implements OutputView
{
    protected final byte[] buffer;
    protected int offset;

    public AbstractOutputView()
    {
        //64k
        this(1 << 16);
    }

    public AbstractOutputView(int buffSize)
    {
        this.buffer = new byte[buffSize];
        this.offset = 0;
    }

    protected void require(int required)
    {
        int ramming = buffer.length - offset;
        if (required > ramming) {
            this.flush();
        }
    }

    @Override
    public abstract void flush()
            throws JcodecException;

    @Override
    public abstract void close()
            throws JcodecException;

    @Override
    public void write(int b)
    {
        require(1);
        buffer[offset++] = (byte) b;
    }

    @Override
    public void write(byte[] b)
    {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len)
    {
        int ramming = buffer.length - offset;
        while (len > ramming) {
            System.arraycopy(b, off, buffer, offset, ramming);
            offset = buffer.length;
            this.flush();
            off += ramming;
            len -= ramming;
            ramming = buffer.length;
        }
        System.arraycopy(b, off, buffer, offset, len);
        offset += len;
    }

    @Override
    public void writeBoolean(boolean v)
    {
        this.writeByte(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v)
    {
        this.write(v);
    }

    @Override
    public void writeShort(int v)
    {
        require(2);
        buffer[offset++] = (byte) ((v >>> 8) & 0xFF);
        buffer[offset++] = (byte) ((v) & 0xFF);
    }

    @Override
    public void writeChar(int v)
    {
        this.writeShort(v);
    }

    @Override
    public void writeInt(int v)
    {
        require(4);
        buffer[offset++] = (byte) ((v >>> 24) & 0xFF);
        buffer[offset++] = (byte) ((v >>> 16) & 0xFF);
        buffer[offset++] = (byte) ((v >>> 8) & 0xFF);
        buffer[offset++] = (byte) ((v) & 0xFF);
    }

    @Override
    public void writeLong(long v)
    {
        require(8);
        buffer[offset++] = (byte) (v >>> 56);
        buffer[offset++] = (byte) (v >>> 48);
        buffer[offset++] = (byte) (v >>> 40);
        buffer[offset++] = (byte) (v >>> 32);
        buffer[offset++] = (byte) (v >>> 24);
        buffer[offset++] = (byte) (v >>> 16);
        buffer[offset++] = (byte) (v >>> 8);
        buffer[offset++] = (byte) (v);
    }

    @Override
    public void writeFloat(float v)
    {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v)
    {
        writeLong(Double.doubleToLongBits(v));
    }

    private void writeAscii0(String s, int len)
            throws JcodecEOFException
    {
        assert !DEBUG || len > 0;
        int ramming = buffer.length - offset;
        int off = 0;
        while (len > ramming) {
            s.getBytes(off, off + ramming, buffer, offset);
            for (int i = 0; i < ramming; i++) {
                buffer[offset++] &= 0x7F;
            }
            assert !DEBUG || offset == buffer.length;
            this.flush();
            off += ramming;
            len -= ramming;
            ramming = buffer.length;
        }
        s.getBytes(off, off + len, buffer, offset);
        for (int i = 0; i < len - 1; i++) {
            buffer[offset++] &= 0x7F;
        }
        buffer[offset++] |= 0x80;
    }

    @Override
    public void writeString(String s)
    {
        if (s == null) {
            require(1);
            buffer[offset++] = (byte) 0x80;
            return;
        }
        int len = s.length();
        if (len == 0) {
            require(1);
            buffer[offset++] = (byte) 0x81;
            return;
        }

        if (len > 1 && len < 64 && Strings.isAscii(s, len)) {
            writeAscii0(s, len);
            return;
        }
        // write utf-8 length
        // If ascii string length is greater than 8192(1 << (6 + 7)), the effect will be worse than DataOutputStream.writeUTF()
        writeUtf16CharCount(len + 1);
        writeUtf8(s, len);
    }

    /**
     * @see java.io.DataOutputStream#writeUTF(String)
     */
    private void writeUtf8(String str, int len)
    {
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch < 0x80 && ch != 0) {
                require(1);
                buffer[offset++] = (byte) ch;
            }
            else if (ch >= 0x800) {
                require(3);
                buffer[offset++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                buffer[offset++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                buffer[offset++] = (byte) (0x80 | ((ch) & 0x3F));
            }
            else {
                require(2);
                buffer[offset++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                buffer[offset++] = (byte) (0x80 | ((ch) & 0x3F));
            }
        }
    }

    public final void writeUtf16CharCount(int charCount)
    {
        //assert charCount > 0;
        if (charCount >>> 6 == 0) {
            require(1);
            buffer[offset++] = (byte) (charCount | 0x80);
        }
        else if (charCount >>> 13 == 0) {
            require(2);
            buffer[offset++] = (byte) (charCount & 0x3F | 0xC0);
            buffer[offset++] = (byte) (charCount >>> 6 & 0x7F);
        }
        else if (charCount >>> 20 == 0) {
            require(3);
            buffer[offset++] = (byte) (charCount & 0x3F | 0xC0);
            buffer[offset++] = (byte) (charCount >>> 6 & 0x7F | 0x80);
            buffer[offset++] = (byte) (charCount >>> 13 & 0x7F);
        }
        else if (charCount >>> 27 == 0) {
            require(4);
            buffer[offset++] = (byte) (charCount & 0x3F | 0xC0);
            buffer[offset++] = (byte) (charCount >>> 6 & 0x7F | 0x80);
            buffer[offset++] = (byte) (charCount >>> 13 & 0x7F | 0x80);
            buffer[offset++] = (byte) (charCount >>> 20 & 0x7F);
        }
        else {
            //5bit, 32 -27
            require(5);
            buffer[offset++] = (byte) (charCount & 0x3F | 0xC0);
            buffer[offset++] = (byte) (charCount >>> 6 & 0x7F | 0x80);
            buffer[offset++] = (byte) (charCount >>> 13 & 0x7F | 0x80);
            buffer[offset++] = (byte) (charCount >>> 20 & 0x7F | 0x80);
            buffer[offset++] = (byte) (charCount >>> 27);
        }
    }

    @Override
    public final void writeVarInt(int value, boolean optimizePositive)
    {
        int v = value;
        if (!optimizePositive) {
            // zigzag coder: v = v >=0 ? value << 1 : (~value) + 1;
            v = (v << 1) ^ (v >> 31);
        }
        if (v >>> 7 == 0) {
            require(1);
            buffer[offset++] = (byte) (v & 0x7F);
        }
        else if (v >>> 14 == 0) {
            require(2);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F);
        }
        else if (v >>> 21 == 0) {
            require(3);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F);
        }
        else if (v >>> 28 == 0) {
            require(4);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F);
        }
        else {
            require(5);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 28);
        }
    }

    @Override
    public final void writeVarLong(long value, boolean optimizePositive)
    {
        long v = value;
        if (!optimizePositive) {
            // zigzag coder: v = v >=0 ? value << 1 : (~value) + 1;
            v = (v << 1) ^ (v >> 63);
        }
        if (v >>> 7 == 0) {
            require(1);
            buffer[offset++] = (byte) (v & 0x7F);
        }
        else if (v >>> 14 == 0) {
            require(2);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F);
        }
        else if (v >>> 21 == 0) {
            require(3);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F);
        }
        else if (v >>> 28 == 0) {
            require(4);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F);
        }
        else if (v >>> 35 == 0) {
            require(5);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 28 & 0x7F);
        }
        else if (v >>> 42 == 0) {
            require(6);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 28 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 35 & 0x7F);
        }
        else if (v >>> 49 == 0) {
            require(7);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 28 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 35 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 42 & 0x7F);
        }
        else if (v >>> 56 == 0) {
            require(8);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 28 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 35 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 42 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 49 & 0x7F);
        }
        else {
            require(9);
            buffer[offset++] = (byte) (v & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 7 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 14 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 21 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 28 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 35 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 42 & 0x7F | 0x80);
            buffer[offset++] = (byte) (v >>> 49 & 0x7F | 0x80);
            // remain 8bit (64 - 8 * 7)
            buffer[offset++] = (byte) (v >>> 56);
        }
    }

    @Override
    public final void writeBoolArray(boolean[] value)
    {
        assert !DEBUG || value.length > 0;
        int byteSize = (value.length + 7) >> 3;
        require(byteSize);
        IOUtils.zipBoolArray(value, 0, buffer, offset, value.length);
        offset += byteSize;
    }
}
