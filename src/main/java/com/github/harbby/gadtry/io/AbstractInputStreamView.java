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

public abstract class AbstractInputStreamView
        extends InputStream
        implements DataInputView
{
    protected final byte[] buffer;
    protected int position;
    protected int limit;

    private byte[] stringBuffer = new byte[32];
    private char[] charBuffer;

    protected AbstractInputStreamView(byte[] buffer)
    {
        this.buffer = buffer;
        this.limit = buffer.length;
        this.position = buffer.length;
        if (buffer.length > Integer.MAX_VALUE >> 4) {
            throw new GadtryIOException("buffer is large: capacity: " + buffer.length);
        }
    }

    protected final void require(int required)
    {
        if (required > buffer.length) {
            throw new GadtryIOException("buffer is small: capacity: " + buffer.length + ", required: " + required);
        }
        if (limit - position < required) {
            int n = 0;
            if (limit == buffer.length) {
                n = this.refill();
            }
            if (n < required) {
                throw new GadtryEOFException("required: " + required);
            }
        }
    }

    protected final int refill()
            throws GadtryIOException
    {
        int l = limit - position;
        if (l > 0) {
            System.arraycopy(buffer, position, buffer, 0, l);
        }
        int n = this.tryReadFully0(buffer, l, position);
        if (n < position) {
            limit = l + n;
        }
        this.position = 0;
        return n;
    }

    protected abstract int tryReadFully0(byte[] b, int off, int len)
            throws GadtryIOException;

    @Override
    public final int skipBytes(int n)
            throws GadtryIOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void readFully(byte[] b)
            throws GadtryIOException
    {
        this.readFully(b, 0, b.length);
    }

    @Override
    public final void readFully(byte[] b, int off, int len)
            throws GadtryIOException
    {
        int n = this.tryReadFully(b, off, len);
        if (n < len) {
            throw new GadtryEOFException();
        }
    }

    @Override
    public final int tryReadFully(byte[] b, int off, int len)
            throws GadtryIOException
    {
        if (limit != buffer.length & position == limit) {
            return -1;
        }
        int rlen = len;
        int index = off;
        do {
            int cacheSize = this.limit - this.position;
            if (cacheSize >= rlen) {
                System.arraycopy(buffer, this.position, b, index, rlen);
                this.position += rlen;
                return index - off + rlen;
            }
            else {
                System.arraycopy(buffer, this.position, b, index, cacheSize);
                this.position = this.limit;
                index += cacheSize;
                rlen -= cacheSize;
                if (limit != buffer.length) {
                    return index - off;
                }
                this.refill();
            }
        }
        while (true);
    }

    @Override
    public int read()
            throws GadtryIOException
    {
        if (this.position == this.limit) {
            if (limit != buffer.length) {
                return -1;
            }
            else {
                this.refill();
            }
        }
        return buffer[position++] & 0XFF;
    }

    @Override
    public final int read(byte[] b, int off, int len)
            throws GadtryIOException
    {
        return this.tryReadFully(b, off, len);
    }

    @Override
    public abstract void close();

    @Override
    public boolean readBoolean()
    {
        require(1);
        byte ch = buffer[position++];
        return (ch != 0);
    }

    @Override
    public byte readByte()
    {
        require(1);
        return buffer[position++];
    }

    @Override
    public int readUnsignedByte()
    {
        require(1);
        return buffer[position++] & 0XFF;
    }

    @Override
    public final void readBoolArray(boolean[] booleans, int pos, int len)
    {
        int byteSize = (len + 7) >> 3;
        require(byteSize);
        IOUtils.unzipBoolArray(buffer, position, booleans, pos, len);
    }

    @Override
    public final int readVarInt(boolean optimizeNegativeNumber)
    {
        require(1);
        byte b = buffer[position++];
        int result = b & 0x7F;
        if (b < 0) {
            require(1);
            b = buffer[position++];
            result |= (b & 0x7F) << 7;
            if (b < 0) {
                require(1);
                b = buffer[position++];
                result |= (b & 0x7F) << 14;
                if (b < 0) {
                    require(1);
                    b = buffer[position++];
                    result |= (b & 0x7F) << 21;
                    if (b < 0) {
                        require(1);
                        b = buffer[position++];
                        //assert b > 0;
                        result |= b << 28;
                    }
                }
            }
        }
        if (optimizeNegativeNumber) {
            return (result >>> 1) ^ -(result & 1);
        }
        else {
            return result;
        }
    }

    @Override
    public final long readVarLong(boolean optimizeNegativeNumber)
    {
        require(1);
        byte b = buffer[position++];
        long result = b & 0x7F;
        if (b < 0) {
            require(1);
            b = buffer[position++];
            result |= (b & 0x7F) << 7;
            if (b < 0) {
                require(1);
                b = buffer[position++];
                result |= (b & 0x7F) << 14;
                if (b < 0) {
                    require(1);
                    b = buffer[position++];
                    result |= (b & 0x7F) << 21;
                    if (b < 0) {
                        require(1);
                        b = buffer[position++];
                        result |= (long) (b & 0x7F) << 28;
                        if (b < 0) {
                            require(1);
                            b = buffer[position++];
                            result |= (long) (b & 0x7F) << 35;
                            if (b < 0) {
                                require(1);
                                b = buffer[position++];
                                result |= (long) (b & 0x7F) << 42;
                                if (b < 0) {
                                    require(1);
                                    b = buffer[position++];
                                    result |= (long) (b & 0x7F) << 49;
                                    if (b < 0) {
                                        require(1);
                                        b = buffer[position++];
                                        result |= (long) b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (optimizeNegativeNumber) {
            return (result >>> 1) ^ -(result & 1);
        }
        else {
            return result;
        }
    }

    @Override
    public final String readAsciiString()
    {
        for (int i = position; i < limit; i++) {
            if (buffer[i] < 0) {
                buffer[i] &= 0x7F;
                String str = new String(buffer, 0, position, i - position + 1);
                position = i + 1;
                return str;
            }
        }
        int charCount = limit - position;
        if (charCount > stringBuffer.length) {
            stringBuffer = new byte[charCount * 2];
        }

        System.arraycopy(buffer, position, stringBuffer, 0, charCount);
        while (true) {
            require(1);
            byte b = buffer[position++];
            if (charCount == stringBuffer.length) {
                byte[] newBuffer = new byte[charCount * 2];
                System.arraycopy(stringBuffer, 0, newBuffer, 0, charCount);
                this.stringBuffer = newBuffer;
            }
            if (b < 0) {
                stringBuffer[charCount] = (byte) (b & 0x7F);
                return new String(stringBuffer, 0, 0, charCount + 1);  // StandardCharsets.US_ASCII
            }
            stringBuffer[charCount++] = b;
        }
    }

    @Override
    public final String readString()
    {
        require(1);
        byte b = buffer[position];
        // b > 0
        if ((b & 0x80) == 0) {
            // ascii
            return readAsciiString();
        }
        else if (b == (byte) 0x80) {
            position++;
            return null;
        }
        else if (b == (byte) 0x81) {
            position++;
            return "";
        }
        int charCount = readUtf16CharCount() - 1;
        if (charBuffer == null || charBuffer.length < charCount) {
            charBuffer = new char[charCount];
        }
        this.readUtf8String(charCount);
        return new String(charBuffer, 0, charCount);
    }

    private void readUtf8String(int charCount)
    {
        int char1;
        int char2;
        int char3;
        for (int count = 0; count < charCount; count++) {
            require(1);
            char1 = buffer[position++] & 0xFF;
            switch (char1 >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    charBuffer[count] = (char) char1;
                    break;
                case 12:
                case 13:
                    require(1);
                    char2 = buffer[position++];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new GadtryEOFException("malformed input around byte " + position);
                    }
                    charBuffer[count] = (char) (((char1 & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    require(2);
                    char2 = buffer[position++];
                    char3 = buffer[position++];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new GadtryEOFException("malformed input around byte " + (position - 1));
                    }
                    charBuffer[count] = (char) (((char1 & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new GadtryEOFException("malformed input around byte " + count);
            }
        }
    }

    public int readUtf16CharCount()
    {
        require(1);
        byte b = buffer[position++];
        assert b < 0;
        if ((b & 0x40) == 0) {
            return b & 0x3F;
        }
        int result = b & 0x3F;
        require(1);
        b = buffer[position++];
        result |= (b & 0x7F) << 6;
        if (b < 0) {
            require(1);
            b = buffer[position++];
            result |= (b & 0x7F) << 13;
            if (b < 0) {
                require(1);
                b = buffer[position++];
                result |= (b & 0x7F) << 20;
                if (b < 0) {
                    require(1);
                    b = buffer[position++];
                    //assert b > 0;
                    result |= b << 27;
                }
            }
        }
        return result;
    }
}
