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
package com.github.harbby.gadtry.jvm;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Strings.isNotBlank;
import static java.util.Objects.requireNonNull;

public class JVMLaunchers
{
    private JVMLaunchers() {}

    public static class VmBuilder<T>
    {
        private VmCallable<T> task;
        private boolean depThisJvm = true;
        private Consumer<String> consoleHandler = System.out::print;
        private final List<URL> tmpJars = new ArrayList<>();
        private final List<String> otherVmOps = new ArrayList<>();
        private final Map<String, String> environment = new HashMap<>();
        private ClassLoader classLoader;
        private File workDir;
        private String taskProcessName;
        private File javaCmd = new File(System.getProperty("java.home"), "bin/java");

        public VmBuilder<T> javaHome(String javaHome)
        {
            File file = new File(javaHome, "bin/java");
            checkState(file.exists() && file.isFile(), "not found java cmd");
            this.javaCmd = file;
            return this;
        }

        public VmBuilder<T> task(VmCallable<T> task)
        {
            this.task = requireNonNull(task, "task is null");
            return this;
        }

        public VmBuilder<T> setClassLoader(ClassLoader classLoader)
        {
            this.classLoader = requireNonNull(classLoader, "classLoader is null");
            return this;
        }

        public VmBuilder<T> setWorkDirectory(File workDirectory)
        {
            checkState(workDirectory.exists(), workDirectory + " not exists");
            checkState(workDirectory.isDirectory(), workDirectory + " not is Directory");
            this.workDir = workDirectory;
            return this;
        }

        public VmBuilder<T> setConsole(Consumer<String> consoleHandler)
        {
            this.consoleHandler = requireNonNull(consoleHandler, "consoleHandler is null");
            return this;
        }

        public VmBuilder<T> notDependParentJvmClassPath()
        {
            depThisJvm = false;
            return this;
        }

        @Deprecated
        public VmBuilder<T> addUserURLClassLoader(URLClassLoader vmClassLoader)
        {
            ClassLoader classLoader = requireNonNull(vmClassLoader, "classLoader is null");
            while (classLoader instanceof URLClassLoader) {
                Collections.addAll(tmpJars, ((URLClassLoader) classLoader).getURLs());
                classLoader = classLoader.getParent();
            }
            return this;
        }

        public VmBuilder<T> addUserJars(Collection<URL> jars)
        {
            tmpJars.addAll(requireNonNull(jars, "jars is null"));
            return this;
        }

        public VmBuilder<T> addUserJars(URL[] jars)
        {
            this.addUserJars(Arrays.asList(requireNonNull(jars, "jars is null")));
            return this;
        }

        public VmBuilder<T> setName(String name)
        {
            this.taskProcessName = requireNonNull(name, "task process name is null");
            return this;
        }

        public VmBuilder<T> setXms(String xms)
        {
            otherVmOps.add("-Xms" + xms);
            return this;
        }

        public VmBuilder<T> setXmx(String xmx)
        {
            otherVmOps.add("-Xmx" + xmx);
            return this;
        }

        public VmBuilder<T> addVmOps(String ops)
        {
            otherVmOps.add(ops);
            return this;
        }

        public VmBuilder<T> setEnvironment(Map<String, String> env)
        {
            this.environment.putAll(requireNonNull(env, "env is null"));
            return this;
        }

        public VmBuilder<T> setEnvironment(String key, String value)
        {
            checkState(isNotBlank(key), "key is null or Empty");
            checkState(isNotBlank(value), "value is null or Empty");
            this.environment.put(key, value);
            return this;
        }

        public JVMLauncher<T> build()
        {
            return new JVMLauncherImpl<>(task, consoleHandler, tmpJars, depThisJvm,
                    otherVmOps, environment, classLoader, workDir, taskProcessName, javaCmd);
        }
    }

    public static <T> VmBuilder<T> newJvm()
    {
        return new VmBuilder<>();
    }
}
