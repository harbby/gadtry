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
import com.github.harbby.gadtry.base.Serializables;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

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
            throws JVMException
    {
        return startAndGet(this.task);
    }

    @Override
    public R startAndGet(VmCallable<R> task)
            throws JVMException
    {
        checkState(task != null, "Fork VM Task is null");
        try {
            byte[] bytes = Serializables.serialize(task);
            AtomicReference<Process> processAtomic = new AtomicReference<>();
            return this.startAndGetByte(processAtomic, bytes);
        }
        catch (JVMException e) {
            throw e;
        }
        catch (Exception e) {
            throw new JVMException(e);
        }
    }

    @Override
    public VmFuture<R> startAsync(ExecutorService executor)
            throws JVMException
    {
        return startAsync(executor, this.task);
    }

    @Override
    public VmFuture<R> startAsync(ExecutorService executor, VmCallable<R> task)
            throws JVMException
    {
        checkState(task != null, "Fork VM Task is null");
        try {
            byte[] bytes = Serializables.serialize(task);
            AtomicReference<Process> processAtomic = new AtomicReference<>();
            return new VmFuture<>(executor, processAtomic, () -> this.startAndGetByte(processAtomic, bytes));
        }
        catch (IOException | InterruptedException e) {
            throw new JVMException(e);
        }
    }

    private R startAndGetByte(AtomicReference<Process> processAtomic, byte[] task)
            throws Exception
    {
        ProcessBuilder builder = new ProcessBuilder(buildMainArg(otherVmOps))
                .redirectErrorStream(true);
        if (workDirectory != null && workDirectory.exists() && workDirectory.isDirectory()) {
            builder.directory(workDirectory);
        }
        builder.environment().putAll(environment);

        Process process = builder.start();
        processAtomic.set(process);

        //send task to child vm
        try (OutputStream os = process.getOutputStream()) {
            os.write(task);
            os.flush();
        }
        catch (IOException ignored) { //child not running
        }
        try (ChildVMChannelInputStream reader = new ChildVMChannelInputStream(process.getInputStream())) {
            reader.checkVMHeader();

            String line;
            while ((line = reader.readLine()) != null) {
                consoleHandler.accept(line);
            }
            byte[] bytes = reader.readResult();
            if (reader.isSuccess()) {
                R result = Serializables.byteToObject(bytes, classLoader);
                process.destroy();
                return result;
            }
            else {
                throw new JVMException(new String(bytes, UTF_8));
            }
        }
        catch (EOFException e) {
            if (process.isAlive()) {
                process.destroy();
                process.waitFor();
            }
            throw new JVMException("Jvm child process abnormal exit, exit code " + process.exitValue());
        }
        catch (IOException e) {
            throw new JVMException("child jvm exec failed", e);
        }
    }

    private String getUserAddClasspath()
    {
        return userJars.stream()
                .map(URL::getPath)
                .collect(Collectors.joining(File.pathSeparator));
    }

    protected List<String> buildMainArg(List<String> otherVmOps)
            throws URISyntaxException
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
            url = ClassLoader.getSystemClassLoader().getResource("./agent.jar");
        }
        if (!JVMLauncher.class.getName().equals(taskProcessName) && url != null && url.getPath().endsWith(".jar")) {
            if (Platform.getClassVersion() > 52) {
                ops.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
            }
            ops.add(String.format("-javaagent:%s=%s:%s", new File(url.toURI()).getPath(), JVMLauncher.class.getName(), taskProcessName));
            ops.add(taskProcessName);
        }
        else {
            ops.add(JVMLauncher.class.getName());
        }

        ops.add(String.valueOf(System.currentTimeMillis()));
        return ops;
    }
}
