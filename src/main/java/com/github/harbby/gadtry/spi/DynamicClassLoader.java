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
package com.github.harbby.gadtry.spi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import static com.github.harbby.gadtry.spi.SecurityClassLoader.PLATFORM_CLASS_LOADER;

public class DynamicClassLoader
        extends URLClassLoader
{
    public DynamicClassLoader(URL[] urls, ClassLoader parent)
    {
        this(urls, parent, null);
    }

    public DynamicClassLoader(ClassLoader parent)
    {
        this(new URL[0], parent);
    }

    public DynamicClassLoader(URL[] urls)
    {
        this(urls, PLATFORM_CLASS_LOADER);
    }

    public DynamicClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
    {
        super(urls, parent, factory);
    }

    /**
     * Adds a jar file from the filesystems into the jar loader list.
     *
     * @param jarfile The full path to the jar file.
     */
    public void addJarFile(URL jarfile)
    {
        this.addURL(jarfile);
    }

    public void addJarFiles(Iterable<File> jarFiles)
            throws MalformedURLException
    {
        for (File jar : jarFiles) {
            this.addJarFile(jar);
        }
    }

    public void addJarFile(File jarfile)
            throws MalformedURLException
    {
        this.addJarFile(jarfile.toURI().toURL());
    }
}
