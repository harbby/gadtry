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
package com.github.harbby.gadtry.easyspi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import static com.github.harbby.gadtry.base.MoreObjects.getOrDefault;
import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;

public class DirClassLoader
        extends URLClassLoader
{
    private final long createTime = System.currentTimeMillis();

    public DirClassLoader(URL[] urls, ClassLoader parent)
    {
        super(getOrDefault(urls, new URL[0]), parent);
    }

    public DirClassLoader(ClassLoader parent)
    {
        super(new URL[0], parent);
    }

    public DirClassLoader(URL[] urls)
    {
        super(urls);
    }

    public DirClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
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
    {
        for (File jar : jarFiles) {
            this.addJarFile(jar);
        }
    }

    public void addJarFile(File jarfile)
    {
        try {
            this.addURL(jarfile.toURI().toURL());
        }
        catch (MalformedURLException e) {
            throwsThrowable(e);
        }
    }

    public void addDir(File path)
    {
        if (!path.exists()) {
            return;
        }

        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    this.addDir(file);
                }
            }
        }
        else {
            this.addJarFile(path);
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + " time:" + createTime;
    }
}
