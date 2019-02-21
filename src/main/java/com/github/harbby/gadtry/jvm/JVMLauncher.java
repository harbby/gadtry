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
package com.github.harbby.gadtry.jvm;

import com.github.harbby.gadtry.base.ObjectInputStreamProxy;
import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.base.Throwables;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class JVMLauncher<R extends Serializable>
{
    private final VmCallable<R> callable;
    private final Collection<URL> userJars;
    private final Consumer<String> consoleHandler;
    private final boolean depThisJvm;
    private final List<String> otherVmOps;
    private final Map<String, String> environment;
    private final ClassLoader classLoader;

    JVMLauncher(
            VmCallable<R> callable,
            Consumer<String> consoleHandler,
            Collection<URL> userJars,
            boolean depThisJvm,
            List<String> otherVmOps,
            Map<String, String> environment,
            ClassLoader classLoader)
    {
        this.callable = callable;
        this.userJars = userJars;
        this.consoleHandler = consoleHandler;
        this.depThisJvm = depThisJvm;
        this.otherVmOps = otherVmOps;
        this.environment = environment;
        this.classLoader = classLoader;
    }

    public R startAndGet()
            throws JVMException
    {
        return startAndGet(this.callable);
    }

    public R startAndGet(VmCallable<R> callable)
            throws JVMException
    {
        checkState(callable != null, "Fork VM Task callable is null");
        try {
            byte[] bytes = Serializables.serialize(callable);
            AtomicReference<Process> processAtomic = new AtomicReference<>();
            return this.startAndGetByte(processAtomic, bytes).get();
        }
        catch (IOException e) {
            throw new JVMException(e);
        }
    }

    public VmFuture<R> startAsync()
            throws JVMException
    {
        return startAsync(this.callable);
    }

    public VmFuture<R> startAsync(VmCallable<R> callable)
            throws JVMException
    {
        checkState(callable != null, "Fork VM Task callable is null");
        try {
            byte[] bytes = Serializables.serialize(callable);
            AtomicReference<Process> processAtomic = new AtomicReference<>();
            return new VmFuture<>(processAtomic, () -> this.startAndGetByte(processAtomic, bytes));
        }
        catch (IOException | InterruptedException e) {
            throw new JVMException(e);
        }
    }

    private VmResult<R> startAndGetByte(AtomicReference<Process> processAtomic, byte[] bytes)
            throws JVMException
    {
        try (ServerSocket sock = new ServerSocket()) {
            sock.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));

            ProcessBuilder builder = new ProcessBuilder(buildMainArg(sock.getLocalPort(), otherVmOps))
                    .redirectErrorStream(true);
            builder.environment().putAll(environment);

            Process process = builder.start();
            processAtomic.set(process);

            try (OutputStream os = new BufferedOutputStream(process.getOutputStream())) {
                os.write(bytes);  //send task
            }
            //IOUtils.copyBytes();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consoleHandler.accept(line);
                }
            }
            //---return Socket io Stream
            // 能执行到这里 并跳出上面的where 则说明子进程已经退出
            //set accept timeOut 3s  //设置最大3秒等待,防止子进程意外退出时 无限等待
            // 正常情况下子进程在退出时,已经回写完数据， 这里需要设置异常退出时 最大等待时间
            sock.setSoTimeout(3000);
            try (Socket socketClient = sock.accept();
                    InputStream inputStream = socketClient.getInputStream()) {
                return Serializables.byteToObject(inputStream, classLoader);
            }
            catch (SocketTimeoutException e) {
                if (process.isAlive()) {
                    process.destroy();
                }
                throw new JVMException("Jvm child process abnormal exit, exit code " + process.exitValue(), e);
            }
        }
        catch (Exception e) {
            throw new JVMException(e);
        }
    }

    private String getUserAddClasspath()
    {
        return userJars.stream()
                .map(URL::getPath)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private List<String> buildMainArg(int port, List<String> otherVmOps)
    {
        File java = new File(new File(System.getProperty("java.home"), "bin"), "java");
        List<String> ops = new ArrayList<>();
        ops.add(java.toString());

        ops.addAll(otherVmOps);

        ops.add("-classpath");
        //ops.add(System.getProperty("java.class.path"));
        String userSdkJars = getUserAddClasspath(); //编译时还需要 用户的额外jar依赖
        if (depThisJvm) {
            ops.add(System.getProperty("java.class.path") + ":" + userSdkJars);
        }
        else {
            ops.add(userSdkJars);
        }

        String javaLibPath = System.getProperty("java.library.path");
        if (javaLibPath != null) {
            ops.add("-Djava.library.path=" + javaLibPath);
        }
        ops.add(JVMLauncher.class.getCanonicalName()); //子进程会启动这个类 进行编译
        ops.add(Integer.toString(port));
        return ops;
    }

    public static void main(String[] args)
            throws Exception
    {
        System.out.println("vm start ok ...");
        VmResult<? extends Serializable> future;

        try (ObjectInputStreamProxy ois = new ObjectInputStreamProxy(System.in)) {
            System.out.println("vm start init ok ...");
            VmCallable<? extends Serializable> callable = (VmCallable<? extends Serializable>) ois.readObject();
            future = new VmResult<>(callable.call());
        }
        catch (Throwable e) {
            future = new VmResult<>(Throwables.getStackTraceAsString(e));
            System.out.println("vm task run error");
        }

        try (OutputStream out = chooseOutputStream(args)) {
            out.write(Serializables.serialize(future));
            System.out.println("vm exiting ok ...");
        }
    }

    private static OutputStream chooseOutputStream(String[] args)
            throws IOException
    {
        if (args.length > 0) {
            int port = Integer.parseInt(args[0]);
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(InetAddress.getLocalHost(), port));
            return sock.getOutputStream();
        }
        else {
            return System.out;
        }
    }
}
