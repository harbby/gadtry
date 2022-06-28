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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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

    /**
     * Copies from one stream to another.
     *
     * @param in       InputStrem to read from
     * @param out      OutputStream to write to
     * @param buffSize the size of the buffer, 4096
     * @throws IOException IOException
     */
    public static void copyBytes(InputStream in, OutputStream out, int buffSize)
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
}
