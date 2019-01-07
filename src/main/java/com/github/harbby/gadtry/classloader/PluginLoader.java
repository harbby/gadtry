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

import com.github.harbby.gadtry.base.Files;
import com.github.harbby.gadtry.collection.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Checks.checkState;
import static java.util.Objects.requireNonNull;

public final class PluginLoader<T>
{
    private final List<Module<T>> modules;

    private PluginLoader(List<Module<T>> modules)
    {
        this.modules = modules;
    }

    public List<Module<T>> getModules()
    {
        return modules;
    }

    public List<T> getPlugins()
    {
        return modules.stream().
                flatMap(module -> module.getPlugins().stream())
                .collect(Collectors.toList());
    }

    public static <T> Builder<T> newScanner()
    {
        return new Builder<>();
    }

    public static class Builder<T>
    {
        private List<String> spiPackages;
        private File scanDir;
        private Class<T> pluginClass;

        public Builder<T> setSpiPackages(List<String> spiPackages)
        {
            this.spiPackages = requireNonNull(spiPackages, "spiPackages is null");
            return this;
        }

        public Builder<T> setScanDir(File scanDir)
        {
            this.scanDir = scanDir;
            return this;
        }

        public Builder<T> setPlugin(Class<T> pluginClass)
        {
            this.pluginClass = pluginClass;
            return this;
        }

        public PluginLoader<T> build()
                throws IOException
        {
            File[] listFiles = scanDir.listFiles(File::isDirectory);
            checkState(listFiles != null, " listFiles is null, dir is " + scanDir);

            ImmutableList.Builder<Module<T>> builder = ImmutableList.builder();
            for (File dir : listFiles) {
                Module<T> module = this.loadModule(dir);
                builder.add(module);
            }

            return new PluginLoader<>(builder.build());
        }

        private Module<T> loadModule(final File moduleDir)
                throws IOException
        {
            URLClassLoader moduleClassLoader = buildClassLoaderFromDirectory(moduleDir);
            try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(moduleClassLoader)) {
                ServiceLoader<T> serviceLoader = ServiceLoader.load(pluginClass, moduleClassLoader);
                List<T> plugins = ImmutableList.copy(serviceLoader);
                return new Module<>(moduleDir, plugins, moduleClassLoader);
            }
        }

        private URLClassLoader buildClassLoaderFromDirectory(File dir)
                throws IOException
        {
            List<URL> urls = new ArrayList<>();
            for (File file : Files.listFiles(dir, true)) {
                urls.add(file.toURI().toURL());
            }

            ClassLoader spiLoader = getClass().getClassLoader();
            return new PluginClassLoader(urls, spiLoader, spiPackages);
        }
    }
}
