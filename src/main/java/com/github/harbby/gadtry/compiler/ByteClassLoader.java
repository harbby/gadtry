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
package com.github.harbby.gadtry.compiler;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ByteClassLoader
        extends ClassLoader
        implements Closeable
{
    private final ConcurrentMap<String, Class<?>> cached = new ConcurrentHashMap<>();

    public ByteClassLoader(ClassLoader parent)
    {
        super(parent);
    }

    public Class<?> loadClass(String name, byte[] bytes)
            throws ClassFormatError
    {
        return cached.computeIfAbsent(name, key -> super.defineClass(name, bytes, 0, bytes.length));
    }

    @Override
    public void close()
    {
        cached.clear();
    }
}
