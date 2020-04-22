package com.github.harbby.gadtry.jvm.v2;

import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.jvm.JVMException;
import com.github.harbby.gadtry.jvm.JVMLauncherImpl;
import com.github.harbby.gadtry.jvm.VmCallable;
import com.github.harbby.gadtry.jvm.VmResult;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JVMLauncherImplV2<R extends Serializable>
        extends JVMLauncherImpl<R> {
    private final VmCallable<R> task;
    private final Collection<URL> userJars;
    private final Consumer<String> consoleHandler;
    private final boolean depThisJvm;
    private final List<String> otherVmOps;
    private final Map<String, String> environment;
    private final ClassLoader classLoader;
    private final File workDirectory;

    public JVMLauncherImplV2(VmCallable<R> task,
                             Consumer<String> consoleHandler,
                             Collection<URL> userJars,
                             boolean depThisJvm,
                             List<String> otherVmOps,
                             Map<String, String> environment,
                             ClassLoader classLoader,
                             File workDirectory) {
        super(task, consoleHandler, userJars, depThisJvm, otherVmOps, environment, classLoader, workDirectory);
        this.task = task;
        this.userJars = userJars;
        this.consoleHandler = consoleHandler;
        this.depThisJvm = depThisJvm;
        this.otherVmOps = otherVmOps;
        this.environment = environment;
        this.classLoader = classLoader;
        this.workDirectory = workDirectory;
    }

    @Override
    protected VmResult<R> startAndGetByte(AtomicReference<Process> processAtomic, byte[] task)
            throws Exception {
        try (ServerSocket sock = new ServerSocket()) {
            sock.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));

            ProcessBuilder builder = new ProcessBuilder(buildMainArg(sock.getLocalPort(), otherVmOps))
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
                while ((type = reader.readByte())!= -1) {
                    int length = reader.readInt();
                    byte[] bytes = new byte[length];
                    int len = reader.read(bytes);
                    if (type == 1) {
                        consoleHandler.accept(new String(bytes, UTF_8));
                    } else if (type == 2) {
                        process.destroy();
                        return Serializables.byteToObject(bytes, classLoader);
                    } else {
                        throw new RuntimeException("not support type code " + type);
                    }
                }
            }
            // 能执行到这里 并跳出上面的where 则说明子进程已经退出
            if (process.isAlive()) {
                process.destroy();
            }
            throw new JVMException("Jvm child process abnormal exit, exit code " + process.exitValue());
        }
    }
}
