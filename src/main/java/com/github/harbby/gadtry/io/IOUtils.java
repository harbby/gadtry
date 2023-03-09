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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOUtils
{
    private IOUtils() {}

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    public static void readFully(InputStream in, byte[] b)
            throws IOException
    {
        readFully(in, b, 0, b.length);
    }

    public static void readFully(InputStream in, byte[] b, int off, int len)
            throws IOException
    {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException("should be read " + len + " bytes, but read " + n);
            }
            n += count;
        }
    }

    public static int tryReadFully(InputStream in, byte[] b, int off, int len)
            throws IOException
    {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                return n;
            }
            n += count;
        }
        return n;
    }

    public static int skipBytes(InputStream in, int n)
            throws IOException
    {
        int total = 0;
        int cur = 0;

        while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
            total += cur;
        }

        return total;
    }

    public static String toString(InputStream in, Charset charset)
            throws IOException
    {
        return new String(IOUtils.readAllBytes(in), charset);
    }

    /**
     * Copies from one stream to another.
     *
     * @param in       InputStrem to read from
     * @param out      OutputStream to write to
     * @param buffSize the size of the buffer, 4096
     * @throws IOException IOException
     */
    public static void copy(InputStream in, OutputStream out, int buffSize)
            throws IOException
    {
        PrintStream ps = out instanceof PrintStream ? (PrintStream) out : null;
        byte[] buf = new byte[buffSize];
        int bytesRead = -1;
        while ((bytesRead = in.read(buf)) >= 0) {
            out.write(buf, 0, bytesRead);
            if ((ps != null) && ps.checkError()) {
                throw new IOException("Unable to write to output stream.");
            }
        }
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException
    {
        copy(in, out, 4096);
    }

    public static List<String> readAllLines(InputStream inputStream)
            throws IOException
    {
        List<String> stringList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringList.add(line);
            }
        }
        return stringList;
    }

    /**
     * copyed jdk11
     *
     * @param inputStream input
     * @return byye array
     * @throws IOException throw IOException
     * @since 11
     */
    public static byte[] readAllBytes(InputStream inputStream)
            throws IOException
    {
        return readNBytes(inputStream, Integer.MAX_VALUE);
    }

    /**
     * copyed jdk11
     *
     * @param inputStream input
     * @param len         max len byte
     * @return byte array
     * @throws IOException throw IOException
     * @since 11
     */
    public static byte[] readNBytes(InputStream inputStream, int len)
            throws IOException
    {
        if (len < 0) {
            throw new IllegalArgumentException("initialSize < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = inputStream.read(buf, nread,
                    Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                total += nread;
                if (result == null) {
                    result = buf;
                }
                else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        }
        while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ?
                    result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }

    public static void write(byte[] bytes, String filePath)
            throws IOException
    {
        write(bytes, new File(filePath));
    }

    public static void write(byte[] bytes, File file)
            throws IOException
    {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("mkdir parent dir " + parent + " failed");
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            outputStream.write(bytes);
        }
    }

    /**
     * copied from jdk11
     * Returns a new {@code OutputStream} which discards all bytes.  The
     * returned stream is initially open.  The stream is closed by calling
     * the {@code close()} method.  Subsequent calls to {@code close()} have
     * no effect.
     *
     * <p> While the stream is open, the {@code write(int)}, {@code
     * write(byte[])}, and {@code write(byte[], int, int)} methods do nothing.
     * After the stream has been closed, these methods all throw {@code
     * IOException}.
     *
     * <p> The {@code flush()} method does nothing.
     *
     * @return an {@code OutputStream} which discards all bytes
     * @since 11
     */
    public static OutputStream nullOutputStream()
    {
        return new OutputStream()
        {
            private volatile boolean closed;

            private void ensureOpen()
                    throws IOException
            {
                if (closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public void write(int b)
                    throws IOException
            {
                ensureOpen();
            }

            @Override
            public void write(byte[] b, int off, int len)
                    throws IOException
            {
                //Objects.checkFromIndexSize(off, len, b.length);
                ensureOpen();
            }

            @Override
            public void close()
            {
                closed = true;
            }
        };
    }

    public static void zipBoolArray(boolean[] src, int srcPos, byte[] dest, int destPos, int boolArrayLength)
    {
        int byteSize = (boolArrayLength + 7) >> 3;
        Arrays.fill(dest, destPos, destPos + byteSize, (byte) 0);
        for (int i = 0; i < boolArrayLength; i++) {
            if (src[i + srcPos]) {
                dest[(i >> 3) + destPos] |= 0x80 >> (i & 7);
            }
        }
    }

    public static void unzipBoolArray(byte[] src, int srcPos, boolean[] dest, int destPos, int boolArrayLength)
    {
        for (int i = 0; i < boolArrayLength; i++) {
            byte v = src[(i >> 3) + srcPos];
            dest[i + destPos] = (v & (0x80 >> (i & 7))) != 0;
        }
    }
}
