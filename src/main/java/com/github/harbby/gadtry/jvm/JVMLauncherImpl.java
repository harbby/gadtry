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
import com.github.harbby.gadtry.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
    private final VmCallable<R> task;
    private final Collection<URL> userJars;
    private final Consumer<String> consoleHandler;
    private final boolean depThisJvm;
    private final List<String> otherVmOps;
    private final Map<String, String> environment;
    private final ClassLoader classLoader;
    private final File workDirectory;
    private final String taskProcessName;

    public JVMLauncherImpl(VmCallable<R> task,
            Consumer<String> consoleHandler,
            Collection<URL> userJars,
            boolean depThisJvm,
            List<String> otherVmOps,
            Map<String, String> environment,
            ClassLoader classLoader,
            File workDirectory,
            String taskProcessName)
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
            return this.startAndGetByte(processAtomic, bytes).get();
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

    private VmResult<R> startAndGetByte(AtomicReference<Process> processAtomic, byte[] task)
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

        try (OutputStream os = new BufferedOutputStream(process.getOutputStream())) {
            os.write(task);  //send task
        }
        //IOUtils.copyBytes();

        try (DataInputStream reader = new DataInputStream(process.getInputStream())) {
            byte type;
            while ((type = reader.readByte()) != -1) {
                if (type == 1) {
                    consoleHandler.accept(new String(readLensByte(reader), UTF_8));
                }
                else if (type == 0) {
                    VmResult<R> result = Serializables.byteToObject(readLensByte(reader), classLoader);
                    process.destroy();
                    return result;
                }
                else {
                    byte[] bytes = IOUtils.readAllBytes(reader); //java11 use reader.readAllBytes();
                    byte[] fullBytes = new byte[bytes.length + 1];
                    System.arraycopy(bytes, 0, fullBytes, 1, bytes.length);
                    fullBytes[0] = type;
                    throw new JVMException(new String(fullBytes, UTF_8));
                }
            }
        }
        catch (EOFException e) {
            // 能执行到这里 并跳出上面的where 则说明子进程已经退出
            if (process.isAlive()) {
                process.destroy();
                process.waitFor();
            }
        }
        throw new JVMException("Jvm child process abnormal exit, exit code " + process.exitValue());
    }

    private byte[] readLensByte(DataInputStream reader)
            throws IOException
    {
        int length = reader.readInt();
        byte[] bytes = new byte[length];
        int len = reader.read(bytes);
        return bytes;
    }

    private String getUserAddClasspath()
    {
        return userJars.stream()
                .map(URL::getPath)
                .collect(Collectors.joining(File.pathSeparator));
    }

    protected List<String> buildMainArg(List<String> otherVmOps)
    {
        File java = new File(new File(System.getProperty("java.home"), "bin"), "java");
        List<String> ops = new ArrayList<>();
        ops.add(java.toString());

        ops.addAll(otherVmOps);

        ops.add("-classpath");
        //ops.add(System.getProperty("java.class.path"));
        String userSdkJars = getUserAddClasspath(); //编译时还需要 用户的额外jar依赖
        if (depThisJvm) {
            ops.add(System.getProperty("java.class.path") + File.pathSeparator + userSdkJars);
        }
        else {
            ops.add(userSdkJars);
        }

        String javaLibPath = System.getProperty("java.library.path");
        if (javaLibPath != null) {
            ops.add("-Djava.library.path=" + javaLibPath);
        }

        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        if (!url.getPath().endsWith(".jar")) {
            url = ClassLoader.getSystemClassLoader().getResource("./agent.jar");
        }
        if (!JVMLauncher.class.getName().equals(taskProcessName) && url != null && url.getPath().endsWith(".jar")) {
            ops.add(String.format("-javaagent:%s=%s:%s", url.getPath(), JVMLauncher.class.getName(), taskProcessName));
            ops.add(taskProcessName);
        }
        else {
            ops.add(JVMLauncher.class.getName()); //子进程会启动这个类 进行编译
        }

        ops.add(String.valueOf(System.currentTimeMillis()));
        return ops;
    }
}
