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

import com.github.harbby.gadtry.base.Platform;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
        private Consumer<String> consoleHandler = System.out::println;
        private final List<URL> tmpJars = new ArrayList<>();
        private final List<String> otherVmOps = new ArrayList<>();
        private final Map<String, String> environment = new HashMap<>();
        private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        private File workDir;
        private String taskProcessName;
        private File javaCmd = getJavaCmd(System.getProperty("java.home"));
        private long timeoutNanos = -1;
        private boolean autoExit;
        private boolean redirectOutputToNull;
        private boolean ignoreClasspath;

        private static File getJavaCmd(String javaHome)
        {
            File file;
            if (Platform.isWindows()) {
                file = new File(javaHome, "bin/java.exe");
            }
            else {
                file = new File(javaHome, "bin/java");
            }
            return file;
        }

        public VmBuilder<T> javaHome(String javaHome)
        {
            this.javaCmd = getJavaCmd(javaHome);
            checkState(javaCmd.exists() && javaCmd.isFile(), "not found java cmd " + javaCmd);
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

        public VmBuilder<T> ignoreParentClasspath()
        {
            ignoreClasspath = true;
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
            return this.addVmOps("-Xms" + xms);
        }

        public VmBuilder<T> setXmx(String xmx)
        {
            return this.addVmOps("-Xmx" + xmx);
        }

        public VmBuilder<T> addVmOps(Collection<String> ops)
        {
            otherVmOps.addAll(ops);
            return this;
        }

        public VmBuilder<T> addVmOps(String... ops)
        {
            Collections.addAll(otherVmOps, ops);
            return this;
        }

        public VmBuilder<T> addEnvironment(Map<String, String> env)
        {
            this.environment.putAll(requireNonNull(env, "env is null"));
            return this;
        }

        public VmBuilder<T> addEnvironment(String key, String value)
        {
            checkState(isNotBlank(key), "key is null or Empty");
            checkState(isNotBlank(value), "value is null or Empty");
            this.environment.put(key, value);
            return this;
        }

        public VmBuilder<T> timeout(long timeout, TimeUnit timeUnit)
        {
            checkState(timeout > 0, "timeout should > 0");
            requireNonNull(timeUnit, "timeUnit is null");
            this.timeoutNanos = timeUnit.toNanos(timeout);
            return this;
        }

        /**
         * enable child vm autoClose on parent vm exited
         * default is false
         */
        public VmBuilder<T> autoExit()
        {
            this.autoExit = true;
            return this;
        }

        /**
         * enable redirect sys.out to linux /dev/null
         */
        public VmBuilder<T> redirectOutputToNull()
        {
            this.redirectOutputToNull = true;
            return this;
        }

        public JVMLauncher<T> build()
        {
            return new JVMLauncherImpl<>(task, consoleHandler, tmpJars, ignoreClasspath,
                    otherVmOps, environment, classLoader, workDir, taskProcessName, javaCmd,
                    timeoutNanos, autoExit, redirectOutputToNull);
        }
    }

    public static <T> VmBuilder<T> newJvm()
    {
        return new VmBuilder<>();
    }
}
