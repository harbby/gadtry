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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.getOrDefault;
import static com.github.harbby.gadtry.easyspi.SecurityClassLoader.findPlatformClassLoader;
import static java.util.Objects.requireNonNull;

public class VolatileClassLoader
        extends URLClassLoader
{
    private static final ClassLoader PLATFORM_CLASS_LOADER = findPlatformClassLoader();

    private volatile ClassLoader spiClassLoader;

    public VolatileClassLoader(URL[] urls, ClassLoader spiClassLoader)
    {
        super(urls, PLATFORM_CLASS_LOADER);
        this.spiClassLoader = requireNonNull(spiClassLoader, "spi ClassLoader is null");
    }

    public VolatileClassLoader(ClassLoader spiClassLoader)
    {
        super(new URL[0], PLATFORM_CLASS_LOADER);
        this.spiClassLoader = requireNonNull(spiClassLoader, "spi ClassLoader is null");
    }

    public void setSpiClassLoader(ClassLoader spiClassLoader)
    {
        this.spiClassLoader = requireNonNull(spiClassLoader, "new spiClassLoader is null");
    }

    public ClassLoader getSpiClassLoader()
    {
        return spiClassLoader;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        // grab the magic lock
        synchronized (getClassLoadingLock(name)) {
            // Check if class is in the loaded classes cache
            Class<?> cachedClass = findLoadedClass(name);
            if (cachedClass != null) {
                return resolveClass(cachedClass, resolve);
            }

            // only use SPI class loader
            try {
                return resolveClass(spiClassLoader.loadClass(name), resolve);
            }
            catch (ClassNotFoundException ignored) {
            }

            // Look for class locally
            return super.loadClass(name, resolve);
        }
    }

    private Class<?> resolveClass(Class<?> clazz, boolean resolve)
    {
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public URL getResource(String name)
    {
        // first use SPI class loader
        URL url = spiClassLoader.getResource(name);
        return getOrDefault(url, super.getResource(name));
    }

    @Override
    public Enumeration<URL> getResources(String name)
            throws IOException
    {
        // first use SPI resources
        Enumeration<URL> urlEnumeration = spiClassLoader.getResources(name);
        List<URL> urlList = new ArrayList<>();
        while (urlEnumeration.hasMoreElements()) {
            urlList.add(urlEnumeration.nextElement());
        }
        urlEnumeration = super.getResources(name);
        while (urlEnumeration.hasMoreElements()) {
            urlList.add(urlEnumeration.nextElement());
        }
        return Collections.enumeration(urlList);
    }
}
