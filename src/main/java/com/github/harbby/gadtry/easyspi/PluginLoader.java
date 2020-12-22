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

import com.github.harbby.gadtry.base.ClassLoaders;
import com.github.harbby.gadtry.base.Closeables;
import com.github.harbby.gadtry.base.Files;
import com.github.harbby.gadtry.collection.MutableList;
import com.github.harbby.gadtry.function.exception.Function;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Throwables.throwsException;
import static java.util.Objects.requireNonNull;

public final class PluginLoader<T>
{
    private final Function<File, Module<T>, Exception> loader;
    private final Supplier<Collection<File>> scanner;
    private final ConcurrentMap<String, Module<T>> modules = new ConcurrentHashMap<>();

    private PluginLoader(List<Module<T>> modules, Function<File, Module<T>, Exception> loader, Supplier<Collection<File>> scanner)
    {
        this.loader = loader;
        this.scanner = scanner;
        for (Module<T> module : modules) {
            this.modules.put(module.getName(), module);
        }
    }

    public List<Module<T>> getModules()
    {
        return MutableList.copy(modules.values());
    }

    public Module<T> getModule(String id)
    {
        return modules.get(id);
    }

    public List<T> getPlugins()
    {
        return modules.values().stream()
                .flatMap(module -> module.getPlugins().stream())
                .collect(Collectors.toList());
    }

    public void reload()
    {
        try {
            Iterator<Module<T>> it = modules.values().iterator();
            while (it.hasNext()) {
                Module<T> module = it.next();
                if (module.refresh()) {
                    module.close();  //先卸载
                    File moduleDir = module.getModulePath();
                    if (moduleDir.exists()) {
                        Module<T> newModule = loader.apply(moduleDir);
                        modules.put(newModule.getName(), newModule);
                    }
                    else {
                        it.remove(); //移除已被删除的module
                    }
                }
            }
            //reload Module dirs;
            for (File moduleDir : scanner.get()) {
                String name = moduleDir.getName();
                if (!modules.containsKey(name)) {
                    Module<T> module = loader.apply(moduleDir);
                    modules.put(module.getName(), module);
                }
            }
        }
        catch (Exception e) {
            throwsException(e);  //module reload failed
        }
    }

    public static <T> Builder<T> newScanner()
    {
        return new Builder<>();
    }

    public static class Builder<T>
    {
        private List<String> spiPackages = Collections.emptyList();
        private Supplier<Collection<File>> scanner;
        private Class<T> pluginClass;
        private java.util.function.Function<File, List<File>> filter = moduleDir -> Files.listFiles(moduleDir, true);
        private Consumer<Module<T>> closeHandler = plugins -> {};
        private Consumer<Module<T>> loadHandler = plugins -> {};
        private ClassLoader spiLoader;

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
            this.spiLoader = requireNonNull(parentLoader, "parentLoader is null");
            return this;
        }

        public Builder<T> setModuleDepFilter(java.util.function.Function<File, List<File>> filter)
        {
            this.filter = requireNonNull(filter, "Depend filter is null");
            return this;
        }

        public Builder<T> setCloseHandler(Consumer<Module<T>> closeHandler)
        {
            this.closeHandler = requireNonNull(closeHandler, "closeHandler is null");
            return this;
        }

        public Builder<T> setLoadHandler(Consumer<Module<T>> loadHandler)
        {
            this.loadHandler = requireNonNull(loadHandler, "loadHandler is null");
            return this;
        }

        public PluginLoader<T> load()
                throws IOException
        {
            checkState(scanner != null, "scanner is null,your must setScanDir()");
            checkState(pluginClass != null, "pluginClass is null,your must setPlugin()");
            if (spiLoader == null) {
                spiLoader = pluginClass.getClassLoader();
            }
            if (spiLoader == null) {
                spiLoader = ClassLoaders.latestUserDefinedLoader();
            }

            MutableList.Builder<Module<T>> builder = MutableList.builder();
            for (File dir : scanner.get()) {
                Module<T> module = this.loadModule(dir);
                builder.add(module);
            }

            return new PluginLoader<>(builder.build(), this::loadModule, scanner);
        }

        private Module<T> loadModule(final File moduleDir)
                throws IOException
        {
            long loadTime = moduleDir.lastModified();
            URLClassLoader moduleClassLoader = buildClassLoaderFromDirectory(moduleDir);
            try (Closeables ignored = Closeables.openThreadContextClassLoader(moduleClassLoader)) {
                ServiceLoader<T> serviceLoader = ServiceLoader.load(pluginClass, moduleClassLoader);
                List<T> plugins = MutableList.copy(serviceLoader);
                Module<T> module = new Module<>(moduleDir, loadTime, plugins, moduleClassLoader, closeHandler);
                loadHandler.accept(module);
                return module;
            }
        }

        private URLClassLoader buildClassLoaderFromDirectory(File dir)
                throws IOException
        {
            List<URL> urls = new ArrayList<>();
            for (File file : filter.apply(dir)) {
                urls.add(file.toURI().toURL());
            }

            return new PluginClassLoader(urls, spiLoader, spiPackages);
        }
    }
}
