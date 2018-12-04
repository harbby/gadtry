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
import com.github.harbby.gadtry.collection.ImmutableSet;
import com.github.harbby.gadtry.ioc.InjectorException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Checks.checkContainsTrue;

public class ClassScanner
{
    private final Set<Class<?>> classSet;

    private ClassScanner(Set<Class<?>> classSet)
    {
        this.classSet = classSet;
    }

    public Set<Class<?>> getClasses()
    {
        return classSet;
    }

    @SafeVarargs
    public final Set<Class<?>> getClassWithAnnotated(Class<? extends Annotation>... annotations)
    {
        return classSet.stream()
                .filter(aClass -> checkContainsTrue(annotations, (ann) -> aClass.getAnnotation(ann) != null))
                .collect(Collectors.toSet());
    }

    public Set<Class<?>> getClassWithSubclassOf(Class<?>... subclasses)
    {
        return classSet.stream()
                .filter(aClass -> checkContainsTrue(subclasses, aClass::isAssignableFrom))
                .collect(Collectors.toSet());
    }

    public static Builder builder(String basePackage)
    {
        return new Builder(basePackage) {};
    }

    public static class Builder
    {
        private final String basePackage;

        private ClassLoader classLoader;
        private Class<?>[] subclasses;
        private Class<? extends Annotation>[] annotations;
        private BiConsumer<String, Throwable> errorHandler = (classString, error) -> {
            throw new RuntimeException(classString, error);
        };
        private Function<Class<?>, Boolean> classFilter = aClass -> true;

        public Builder(String basePackage)
        {
            this.basePackage = basePackage;
        }

        public Builder classLoader(ClassLoader classLoader)
        {
            this.classLoader = classLoader;
            return this;
        }

        public Builder subclassOf(Class<?>... subclasses)
        {
            this.subclasses = subclasses;
            return this;
        }

        @SafeVarargs
        public final Builder annotated(Class<? extends Annotation>... annotations)
        {
            this.annotations = annotations;
            return this;
        }

        public Builder filter(Function<Class<?>, Boolean> filter)
        {
            this.classFilter = filter;
            return this;
        }

        public Builder loadError(BiConsumer<String, Throwable> handler)
        {
            this.errorHandler = handler;
            return this;
        }

        public ClassScanner scan()
        {
            Set<Class<?>> classSet;
            try {
                if (classLoader == null) {
                    classLoader = sun.misc.VM.latestUserDefinedLoader();
                }
                classSet = getClasses(basePackage, classLoader, errorHandler);
            }
            catch (IOException e) {
                throw new InjectorException(e);
            }

            classSet = classSet.stream()
                    .filter(aClass -> checkContainsTrue(annotations, (ann) -> aClass.getAnnotation(ann) != null))
                    .filter(aClass -> checkContainsTrue(subclasses, (sub) -> sub.isAssignableFrom(aClass)))
                    .filter(aClass -> classFilter.apply(aClass))
                    .collect(Collectors.toSet());
            return new ClassScanner(classSet);
        }
    }

    public static Set<Class<?>> getClasses(String basePackage)
            throws InjectorException
    {
        ClassLoader classLoader = sun.misc.VM.latestUserDefinedLoader();
        try {
            return getClasses(basePackage, classLoader, (classString, error) -> {
                throw new RuntimeException(classString, error);
            });
        }
        catch (IOException e) {
            throw new InjectorException(e);
        }
    }

    private static Set<Class<?>> getClasses(String basePackage, ClassLoader classLoader, BiConsumer<String, Throwable> handler)
            throws IOException
    {
        Set<String> classStrings = scanClasses(basePackage, classLoader);

        ImmutableSet.Builder<Class<?>> classes = ImmutableSet.builder();
        for (String it : classStrings) {
            String classString = it.substring(0, it.length() - 6).replace("/", ".");

            try {
                Class<?> driver = Class.forName(classString, false, classLoader);  //classLoader.loadClass(classString)
                classes.add(driver);  //
            }
            catch (Throwable e) {
                handler.accept(classString, e);
            }
        }
        return classes.build();
    }

    public static Set<String> scanClasses(String basePackage, ClassLoader classLoader)
            throws IOException
    {
        String packagePath = basePackage.replace('.', '/');

        ImmutableSet.Builder<String> classStrings = ImmutableSet.builder();
        Enumeration<URL> resources = classLoader.getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                classStrings.addAll(scanFileClass(packagePath, url, true));
            }
            else if ("jar".equals(protocol)) {
                classStrings.addAll(scanJarClass(packagePath, url));
            }
        }

        return classStrings.build();
    }

    private static Set<String> scanJarClass(String packagePath, URL url)
            throws IOException
    {
        JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();

        Set<String> classSet = new HashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }
            if (!name.startsWith(packagePath)) {
                continue;
            }

            if (name.endsWith(".class") && !entry.isDirectory()) {
                classSet.add(name);
            }
        }
        return classSet;
    }

    private static Set<String> scanFileClass(String packagePath, URL url, boolean recursive)
    {
        List<File> files = Files.listFiles(new File(url.getPath()), recursive);
        return files.stream().map(file -> {
            String path = file.getPath();
            int start = path.indexOf(packagePath);
            return path.substring(start);
        }).collect(Collectors.toSet());
    }
}
