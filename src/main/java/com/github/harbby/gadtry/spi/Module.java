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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Module<T>
        extends Closeable
{
    File moduleFile();

    String getName();

    public List<T> getPlugins();

    public long getLoadTime();

    public URLClassLoader getModuleClassLoader();

    public boolean modified();

    public class ModuleImpl<T>
            implements Module<T>
    {
        private final List<T> plugins;
        private final File modulePath;
        private final long loadTime;
        private final URLClassLoader moduleClassLoader;

        ModuleImpl(File modulePath, long loadTime, List<T> plugins, URLClassLoader moduleClassLoader)
        {
            this.plugins = plugins;
            this.modulePath = modulePath;
            this.moduleClassLoader = requireNonNull(moduleClassLoader, "module ClassLoader is null");
            this.loadTime = loadTime;
        }

        @Override
        public File moduleFile()
        {
            return modulePath;
        }

        @Override
        public String getName()
        {
            return modulePath.getName();
        }

        @Override
        public List<T> getPlugins()
        {
            return plugins;
        }

        @Override
        public long getLoadTime()
        {
            return loadTime;
        }

        @Override
        public URLClassLoader getModuleClassLoader()
        {
            return moduleClassLoader;
        }

        @Override
        public boolean modified()
        {
            return loadTime != modulePath.lastModified();
        }

        @Override
        public void close()
                throws IOException
        {
            moduleClassLoader.close();
        }
    }
}
