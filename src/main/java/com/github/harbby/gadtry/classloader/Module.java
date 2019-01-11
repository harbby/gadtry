/*
 * Copyright (C) 2018 The Harbby Authors
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Consumer;

public class Module<T>
        implements Closeable
{
    private final List<T> plugins;
    private final File modulePath;
    private final long loadTime;
    private final URLClassLoader moduleClassLoader;
    private final Consumer<Module<T>> closeHandler;

    Module(File modulePath, long loadTime, List<T> plugins, URLClassLoader moduleClassLoader, Consumer<Module<T>> closeHandler)
    {
        this.plugins = plugins;
        this.modulePath = modulePath;
        this.moduleClassLoader = moduleClassLoader;
        this.loadTime = loadTime;
        this.closeHandler = closeHandler;
    }

    public File getModulePath()
    {
        return modulePath;
    }

    public String getName()
    {
        return modulePath.getName();
    }

    public List<T> getPlugins()
    {
        return plugins;
    }

    public long getLoadTime()
    {
        return loadTime;
    }

    public URLClassLoader getModuleClassLoader()
    {
        return moduleClassLoader;
    }

    public boolean refresh()
    {
        //查询文件|文件夹的最新更新时间 用来判断是否需要更新
        return loadTime != modulePath.lastModified();
    }

    @Override
    public void close()
            throws IOException
    {
        try (URLClassLoader classLoader = moduleClassLoader) {
            closeHandler.accept(this);
        }
    }
}
