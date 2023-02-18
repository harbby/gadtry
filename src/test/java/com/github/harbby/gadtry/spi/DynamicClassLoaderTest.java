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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DynamicClassLoaderTest
{
    private final URL url = DynamicClassLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();

    @Test
    public void addURLJarFile()
            throws ClassNotFoundException, MalformedURLException
    {
        DynamicClassLoader classLoader = new DynamicClassLoader(new URL[0]);
        classLoader.addJarFile(new File(url.getFile()));

        Class<?> newClass = classLoader.loadClass(DynamicClassLoaderTest.class.getName());
        Assert.assertNotNull(newClass);
        Assert.assertNotSame(newClass, DynamicClassLoaderTest.class);
    }
}
