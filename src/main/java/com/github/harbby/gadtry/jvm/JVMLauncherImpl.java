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

import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.base.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class JVMLauncherImpl<R>
        implements JVMLauncher<R>
{
    public static final byte[] VM_HEADER = "hello gadtry!".getBytes(UTF_8);

    private final VmCallable<R> task;
    private final Collection<URL> userJars;
    private final Consumer<String> consoleHandler;
    private final boolean depThisJvm;
    private final List<String> otherVmOps;
    private final Map<String, String> environment;
    private final ClassLoader classLoader;
    private final File workDirectory;
    private final String taskProcessName;
    private final File javaCmd;

    public JVMLauncherImpl(VmCallable<R> task,
            Consumer<String> consoleHandler,
            Collection<URL> userJars,
            boolean depThisJvm,
            List<String> otherVmOps,
            Map<String, String> environment,
            ClassLoader classLoader,
            File workDirectory,
            String taskProcessName,
            File javaCmd)
    {
        this.task = task;
        this.userJars = userJars;
        this.consoleHandler = consoleHandler;
        this.depThisJvm = depThisJvm;
        this.otherVmOps = otherVmOps;
        this.environment = environment;
        this.classLoader = classLoader;
        this.workDirectory = workDirectory;
        this.taskProcessName = taskProcessName;
        this.javaCmd = javaCmd;
    }

    @Override
    public R startAndGet()
            throws JVMException, InterruptedException
    {
        return startAndGet(this.task);
    }

    @Override
    public VmPromise<R> start(VmCallable<R> task)
            throws JVMException
    {
        requireNonNull(task, "Fork VM Task is null");
        try {
            byte[] bytes = Serializables.serialize(task);
            return this.startAndGetByte(bytes);
        }
        catch (IOException e) {
            throw new JVMException(e);
        }
    }

    @Override
    public VmPromise<R> start()
            throws JVMException
    {
        return this.start(this.task);
    }

    @Override
    public R startAndGet(VmCallable<R> task)
            throws JVMException, InterruptedException
    {
        return this.start(task).call();
    }

    private VmPromise<R> startAndGetByte(byte[] task)
            throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder(buildMainArg(otherVmOps))
                .redirectErrorStream(false);
        if (workDirectory != null && workDirectory.exists() && workDirectory.isDirectory()) {
            builder.directory(workDirectory);
        }
        builder.environment().putAll(environment);
        Process process = builder.start();

        //send task to child vm
        OutputStream os = process.getOutputStream();
        try {
            os.write(task);
            os.flush();
        }
        catch (IOException e) {
            // child vm exited
        }
        return new VmPromiseImpl<>(process, consoleHandler, classLoader);
    }

    private String getUserAddClasspath()
    {
        return userJars.stream()
                .map(URL::getPath)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private List<String> buildMainArg(List<String> otherVmOps)
    {
        List<String> ops = new ArrayList<>();
        ops.add(javaCmd.toString());
        ops.addAll(otherVmOps);

        String classpath = getUserAddClasspath();
        classpath = depThisJvm ? System.getProperty("java.class.path") + File.pathSeparator + classpath : classpath;
        //ops.add("-classpath");
        //ops.add(classpath);
        environment.put("CLASSPATH", classpath);

        String javaLibPath = System.getProperty("java.library.path");
        if (javaLibPath != null) {
            ops.add("-Djava.library.path=" + javaLibPath);
        }

        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        if (!url.getPath().endsWith(".jar")) {
            url = requireNonNull(ClassLoader.getSystemClassLoader().getResource("./agent.jar"), "not found resource ./agent.jar");
        }
        Class<?> processClass = JVMLauncher.class;
        if (Strings.isNotBlank(taskProcessName)) {
            ops.add(String.format("-javaagent:%s=%s:%s", new File(url.getPath()).getPath(), processClass.getName(), taskProcessName));
            ops.add(processClass.getPackage().getName() + "." + taskProcessName);
        }
        else {
            ops.add(processClass.getName());
        }
        return ops;
    }
}
