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

import com.github.harbby.gadtry.collection.MutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.util.Map;

public class ObjectInputStreamProxy
        extends java.io.ObjectInputStream
{
    private static final Map<String, Class<?>> primClasses =
            MutableMap.of("boolean", boolean.class,
                    "byte", byte.class,
                    "char", char.class,
                    "short", short.class,
                    "int", int.class,
                    "long", long.class,
                    "float", float.class,
                    "double", double.class,
                    "void", void.class);

    private ClassLoader classLoader;

    public ObjectInputStreamProxy(InputStream in)
            throws IOException
    {
        super(in);
    }

    /**
     * ObjectInputStreamProxy used by user classLoader
     * <p>
     *
     * @param in          InputStream
     * @param classLoader used by loadObject
     * @throws IOException IOException
     */
    public ObjectInputStreamProxy(InputStream in, ClassLoader classLoader)
            throws IOException
    {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException
    {
        if (classLoader == null) {
            return super.resolveClass(desc);
        }

        //return super.resolveClass(desc);
        String name = desc.getName();
        try {
            return Class.forName(name, false, classLoader);
        }
        catch (ClassNotFoundException ex) {
            Class<?> cl = primClasses.get(name);
            if (cl != null) {
                return cl;
            }
            else {
                throw ex;
            }
        }
    }
}
