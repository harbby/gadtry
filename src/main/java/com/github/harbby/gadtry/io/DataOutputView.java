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

import java.io.Closeable;

public interface DataOutputView
        extends Closeable
{
    void writeInt(int v)
            throws GadtryIOException;

    void write(int b)
            throws GadtryIOException;

    void write(byte[] b)
            throws GadtryIOException;

    void write(byte[] b, int off, int len)
            throws GadtryIOException;

    void writeBoolean(boolean v)
            throws GadtryIOException;

    void writeByte(int v)
            throws GadtryIOException;

    void writeShort(int v)
            throws GadtryIOException;

    void writeChar(int v)
            throws GadtryIOException;

    void writeLong(long v)
            throws GadtryIOException;

    void writeFloat(float v)
            throws GadtryIOException;

    void writeDouble(double v)
            throws GadtryIOException;

    void writeVarInt(int v, boolean optimizeNegativeNumber)
            throws GadtryIOException;

    void writeVarLong(long v, boolean optimizeNegativeNumber)
            throws GadtryIOException;

    void writeBoolArray(boolean[] v)
            throws GadtryIOException;

    void writeAsciiString(String s)
            throws GadtryIOException;

    void writeString(String s)
            throws GadtryIOException;

    void flush()
            throws GadtryIOException;

    @Override
    void close()
            throws GadtryIOException;
}
