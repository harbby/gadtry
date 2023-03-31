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

public interface OutputView
        extends Closeable
{
    void writeInt(int v)
            throws JcodecException;

    void write(int b)
            throws JcodecException;

    void write(byte[] b)
            throws JcodecException;

    void write(byte[] b, int off, int len)
            throws JcodecException;

    void writeBoolean(boolean v)
            throws JcodecException;

    void writeByte(int v)
            throws JcodecException;

    void writeShort(int v)
            throws JcodecException;

    void writeChar(int v)
            throws JcodecException;

    void writeLong(long v)
            throws JcodecException;

    void writeFloat(float v)
            throws JcodecException;

    void writeDouble(double v)
            throws JcodecException;

    /**
     * if true, Positive numbers are more efficient, negative numbers are less efficient,
     * negative numbers are at least 5 bytes and positive numbers are at least 1 byte.
     */
    void writeVarInt(int v, boolean optimizePositive)
            throws JcodecException;

    /**
     * if true, Positive numbers are more efficient, negative numbers are less efficient,
     * negative numbers are at least 9 bytes and positive numbers are at least 1 byte.
     */
    void writeVarLong(long v, boolean optimizePositive)
            throws JcodecException;

    void writeBoolArray(boolean[] v)
            throws JcodecException;

    void writeString(String s)
            throws JcodecException;

    void flush()
            throws JcodecException;

    @Override
    void close()
            throws JcodecException;
}
