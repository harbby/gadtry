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
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public final class ModuleLoader<T>
{
    private final Supplier<Collection<File>> scanner;
    private final Class<T> pluginClass;
    private final Function<File, List<File>> filter;
    private final Consumer<Module<T>> loadHandler;
    private final ClassLoader parentLoader;
    private final List<String> spiPackages;

    private final ConcurrentMap<String, Module<T>> modulesMap = new ConcurrentHashMap<>();

    private ModuleLoader(
            Class<T> pluginClass,
            Function<File, List<File>> filter,
            Supplier<Collection<File>> scanner,
            ClassLoader parentLoader,
            List<String> spiPackages,
            Consumer<Module<T>> loadHandler)
            throws IOException
    {
        this.pluginClass = pluginClass;
        this.filter = filter;
        this.parentLoader = parentLoader;
        this.spiPackages = spiPackages;
        this.scanner = scanner;
        this.loadHandler = loadHandler;

        for (File dir : scanner.get()) {
            Module<T> module = this.loadModule(dir);
            modulesMap.put(module.getName(), module);
        }
    }

    public List<Module<T>> getModules()
    {
        return ImmutableList.copy(modulesMap.values());
    }

    public Optional<Module<T>> getModule(String id)
    {
        return Optional.ofNullable(modulesMap.get(id));
    }

    public void removeModule(String modName)
            throws IOException
    {
        Module<T> module = modulesMap.remove(modName);
        if (module != null) {
            module.close();
        }
    }

    public List<T> getPlugins()
    {
        return modulesMap.values().stream()
                .flatMap(module -> module.getPlugins().stream())
                .collect(Collectors.toList());
    }

    public synchronized void reload()
            throws IOException
    {
        List<File> updates = new ArrayList<>();
        Iterator<Module<T>> it = modulesMap.values().iterator();
        while (it.hasNext()) {
            Module<T> module = it.next();
            if (module.modified()) {
                it.remove();
                module.close();
                updates.add(module.getModulePath());
            }
        }
        for (File moduleDir : scanner.get()) {
            if (!modulesMap.containsKey(moduleDir.getName())) {
                updates.add(moduleDir);
            }
        }

        for (File moduleFile : updates) {
            if (moduleFile.exists()) {
                Module<T> newModule = this.loadModule(moduleFile);
                Module<T> old = modulesMap.put(newModule.getName(), newModule);
                if (old != null) {
                    old.close();
                }
            }
        }
    }

    private URLClassLoader buildClassLoaderFromDirectory(File dir)
            throws IOException
    {
        List<URL> urls = new ArrayList<>();
        for (File file : filter.apply(dir)) {
            urls.add(file.toURI().toURL());
        }
        return new SecurityClassLoader(urls.toArray(new URL[0]), parentLoader, spiPackages);
    }

    private Module<T> loadModule(final File moduleDir)
            throws IOException
    {
        long loadTime = moduleDir.lastModified();
        URLClassLoader moduleClassLoader = buildClassLoaderFromDirectory(moduleDir);
        try (Closeables<?> ignored = Closeables.openThreadContextClassLoader(moduleClassLoader)) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(pluginClass, moduleClassLoader);
            List<T> plugins = ImmutableList.copy(serviceLoader);
            Module<T> module = new Module<>(moduleDir, loadTime, plugins, moduleClassLoader);
            loadHandler.accept(module);
            return module;
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

        public Builder<T> onlyAccessSpiPackages(List<String> spiPackages)
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
            if (parentLoader == null) {
                parentLoader = pluginClass.getClassLoader();
            }
            if (parentLoader == null) {
                parentLoader = ClassLoader.getSystemClassLoader();
            }

            return new ModuleLoader<>(pluginClass, filter, scanner, parentLoader, spiPackages, loadHandler);
        }
    }
}
