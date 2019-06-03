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
package com.github.harbby.gadtry.classloader;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

public class DirClassLoaderTest
{
    private final URL url = this.getClass().getClassLoader().getResource("version2/h2-1.4.199.jar");

    @Test
    public void addURLJarFile()
            throws ClassNotFoundException
    {
        DirClassLoader classLoader = new DirClassLoader(new URL[0]);
        classLoader.addJarFile(url);

        Assert.assertNotNull(classLoader.loadClass("org.h2.Driver"));
    }

    @Test
    public void addJarFile()
            throws ClassNotFoundException
    {
        DirClassLoader classLoader = new DirClassLoader(new URL[0], this.getClass().getClassLoader());
        classLoader.addJarFile(new File(url.getFile()));
        Assert.assertNotNull(classLoader.loadClass("org.h2.Driver"));
    }

    @Test
    public void addJarFiles()
            throws ClassNotFoundException
    {
        DirClassLoader classLoader = new DirClassLoader(this.getClass().getClassLoader());
        classLoader.addJarFiles(Arrays.asList(new File(url.getFile())));
        Assert.assertNotNull(classLoader.loadClass("org.h2.Driver"));
    }

    @Test
    public void addDir()
            throws ClassNotFoundException
    {
        DirClassLoader classLoader = new DirClassLoader(new URL[0]);

        classLoader.addDir(new File(url.getFile()).getParentFile());
        Assert.assertNotNull(classLoader.loadClass("org.h2.Driver"));
    }

    @Test
    public void toStringTest()
    {
        DirClassLoader classLoader = new DirClassLoader(new URL[0]);
        Assert.assertTrue(classLoader.toString().contains("time:"));
    }
}
