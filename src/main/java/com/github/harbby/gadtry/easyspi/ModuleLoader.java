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

import com.github.harbby.gadtry.base.Closeables;
import com.github.harbby.gadtry.base.Files;
import com.github.harbby.gadtry.collection.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.MoreObjects.getOrDefault;
import static java.util.Objects.requireNonNull;

public final class ModuleLoader<T>
{
    private final Supplier<Collection<File>> scanner;
    private final Class<T> pluginClass;
    private final Function<File, List<File>> filter;
    private final Consumer<Module<T>> loadHandler;
    private final Function<URL[], URLClassLoader> classLoaderFactory;
    private final BiFunction<Class<T>, ClassLoader, Iterable<T>> loader;

    private ModuleLoader(
            Class<T> pluginClass,
            Function<File, List<File>> filter,
            Supplier<Collection<File>> scanner,
            ClassLoader parentLoader,
            List<String> spiPackages,
            Consumer<Module<T>> loadHandler,
            Function<URL[], URLClassLoader> classLoaderFactory, BiFunction<Class<T>, ClassLoader, Iterable<T>> loader)
            throws IOException
    {
        this.pluginClass = pluginClass;
        this.filter = filter;
        this.scanner = scanner;
        this.loadHandler = loadHandler;
        this.loader = loader;

        if (classLoaderFactory == null) {
            if (spiPackages == null) {
                this.classLoaderFactory = urls -> new DirClassLoader(urls, parentLoader);
            }
            else {
                this.classLoaderFactory = urls -> new SecurityClassLoader(urls, parentLoader, spiPackages);
            }
        }
        else {
            this.classLoaderFactory = classLoaderFactory;
        }

        for (File dir : scanner.get()) {
            this.loadModule(dir);
        }
    }

    public void reload(Map<String, ? extends Module<T>> modulesMap)
            throws IOException
    {
        List<File> updates = new ArrayList<>();
        Iterator<? extends Module<T>> it = modulesMap.values().iterator();
        while (it.hasNext()) {
            Module<? extends T> module = it.next();
            if (module.modified()) {
                it.remove();
                module.close();
                updates.add(module.moduleFile());
            }
        }
        for (File moduleDir : scanner.get()) {
            if (!modulesMap.containsKey(moduleDir.getName())) {
                updates.add(moduleDir);
            }
        }

        for (File moduleFile : updates) {
            if (moduleFile.exists()) {
                this.loadModule(moduleFile);
            }
        }
    }

    private URLClassLoader prepareClassLoaderFromDirectory(File dir)
            throws IOException
    {
        List<URL> urls = new ArrayList<>();
        for (File file : filter.apply(dir)) {
            urls.add(file.toURI().toURL());
        }
        return classLoaderFactory.apply(urls.toArray(new URL[0]));
    }

    private void loadModule(final File moduleDir)
            throws IOException
    {
        long loadTime = moduleDir.lastModified();
        URLClassLoader moduleClassLoader = prepareClassLoaderFromDirectory(moduleDir);
        try (Closeables<?> ignored = Closeables.openThreadContextClassLoader(moduleClassLoader)) {
            Iterable<T> serviceLoader = loader.apply(pluginClass, moduleClassLoader);
            List<T> plugins = ImmutableList.copy(serviceLoader);
            Module<T> module = new Module.ModuleImpl<>(moduleDir, loadTime, plugins, moduleClassLoader);
            loadHandler.accept(module);
        }
    }

    public static <T> Builder<T> newScanner()
    {
        return new Builder<>();
    }

    public static class Builder<T>
    {
        private List<String> spiPackages = null;
        private Supplier<Collection<File>> scanner;
        private Class<T> pluginClass;
        private java.util.function.Function<File, List<File>> filter = moduleDir -> Files.listFiles(moduleDir, true);
        private Consumer<Module<T>> loadHandler = plugins -> {};
        private ClassLoader parentLoader;
        private Function<URL[], URLClassLoader> classLoaderFactory;
        private BiFunction<Class<T>, ClassLoader, Iterable<T>> loader = ServiceLoader::load;

        public Builder<T> accessSpiPackages(List<String> spiPackages)
        {
            this.spiPackages = requireNonNull(spiPackages, "spiPackages is null");
            return this;
        }

        public Builder<T> setScanDir(File scanDir)
        {
            this.scanner = () -> {
                File[] listFiles = scanDir.listFiles(File::isDirectory);
                checkState(listFiles != null, " listFiles is null, dir is " + scanDir);
                return Arrays.asList(listFiles);
            };
            return this;
        }

        public Builder<T> setScanDir(Supplier<Collection<File>> scanner)
        {
            this.scanner = requireNonNull(scanner, "scanner is null");
            return this;
        }

        public Builder<T> setLoader(BiFunction<Class<T>, ClassLoader, Iterable<T>> loader)
        {
            this.loader = requireNonNull(loader, "loader is null");
            return this;
        }

        public Builder<T> setClassLoaderFactory(Function<URL[], URLClassLoader> classLoaderFactory)
        {
            this.classLoaderFactory = requireNonNull(classLoaderFactory, "classLoaderFactory is null");
            return this;
        }

        public Builder<T> setPlugin(Class<T> pluginClass)
        {
            this.pluginClass = requireNonNull(pluginClass, "pluginClass is null");
            return this;
        }

        public Builder<T> setParentLoader(ClassLoader parentLoader)
        {
            this.parentLoader = requireNonNull(parentLoader, "parentLoader is null");
            return this;
        }

        public Builder<T> setModuleDepFilter(java.util.function.Function<File, List<File>> filter)
        {
            this.filter = requireNonNull(filter, "Depend filter is null");
            return this;
        }

        public Builder<T> setLoadHandler(Consumer<Module<T>> loadHandler)
        {
            this.loadHandler = requireNonNull(loadHandler, "loadHandler is null");
            return this;
        }

        public ModuleLoader<T> load()
                throws IOException
        {
            checkState(scanner != null, "scanner is null,your must setScanDir()");
            checkState(pluginClass != null, "pluginClass is null,your must setPlugin()");
            parentLoader = getOrDefault(parentLoader, pluginClass.getClassLoader());
            parentLoader = getOrDefault(parentLoader, ClassLoader.getSystemClassLoader());

            return new ModuleLoader<>(pluginClass, filter, scanner, parentLoader, spiPackages, loadHandler, classLoaderFactory, loader);
        }
    }
}
