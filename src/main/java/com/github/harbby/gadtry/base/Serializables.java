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
package com.github.harbby.gadtry.base;

import com.github.harbby.gadtry.function.AutoClose;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

public class Serializables
{
    private Serializables() {}

    public static byte[] serialize(Object obj)
            throws IOException
    {
        requireNonNull(obj, "obj is null");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(obj);
            os.flush();
            return bos.toByteArray();
        }
    }

    public static void serialize(OutputStream outputStream, Object obj)
            throws IOException
    {
        requireNonNull(obj, "outputStream is null");
        requireNonNull(obj, "obj is null");
        ObjectOutputStream os = outputStream instanceof ObjectOutputStream ? (ObjectOutputStream) outputStream : new ObjectOutputStream(outputStream);
        os.writeObject(obj);
    }

    public static <T> T byteToObject(byte[] bytes)
            throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        return byteToObject(bi);
    }

    @SuppressWarnings("unchecked")
    public static <T> T byteToObject(InputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        ObjectInputStream oi = new ObjectInputStream(inputStream);
        return (T) oi.readObject();
    }

    public static <T> T byteToObject(byte[] bytes, ClassLoader classLoader)
            throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        return byteToObject(bi, classLoader);
    }

    @SuppressWarnings("unchecked")
    public static <T> T byteToObject(InputStream inputStream, ClassLoader classLoader)
            throws IOException, ClassNotFoundException
    {
        ObjectInputStream oi = new ClassLoaderObjectInputStream(inputStream, classLoader);
        try (AutoClose ignored = Try.openThreadContextClassLoader(classLoader)) {
            return (T) oi.readObject();
        }
    }
}
