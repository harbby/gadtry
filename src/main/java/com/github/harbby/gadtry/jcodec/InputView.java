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

import java.io.Closeable;

public interface InputView
        extends Closeable
{
    void readFully(byte[] b)
            throws JcodecException;

    void readFully(byte[] b, int off, int len)
            throws JcodecException;

    int tryReadFully(byte[] b, int off, int len)
            throws JcodecException;

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an {@code int} in the range {@code 0} to
     * {@code 255}. If no byte is available because the end of the stream
     * has been reached, the value {@code -1} is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p> A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or {@code -1} if the end of the
     * stream is reached.
     * @throws JcodecException if an I/O error occurs.
     */
    int read()
            throws JcodecException;

    int skipBytes(int n)
            throws JcodecException;

    boolean readBoolean()
            throws JcodecException;

    byte readByte()
            throws JcodecException;

    int readUnsignedByte()
            throws JcodecException;

    short readShort()
            throws JcodecException;

    int readUnsignedShort()
            throws JcodecException;

    char readChar()
            throws JcodecException;

    int readInt()
            throws JcodecException;

    long readLong()
            throws JcodecException;

    float readFloat()
            throws JcodecException;

    double readDouble()
            throws JcodecException;

    String readString()
            throws JcodecException;

    void readBoolArray(boolean[] booleans, int pos, int len)
            throws JcodecException;

    int readVarInt(boolean optimizePositive)
            throws JcodecException;

    long readVarLong(boolean optimizePositive)
            throws JcodecException;

    @Override
    void close()
            throws JcodecException;
}
