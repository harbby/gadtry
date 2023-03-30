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
import com.github.harbby.gadtry.base.Threads;
import com.github.harbby.gadtry.collection.MutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JVMLaunchersTest
{
    @Test
    public void baseIOTest()
            throws IOException
    {
        PipedOutputStream outStream = new PipedOutputStream();
        PrintStream out = new PrintStream(outStream);
        InputStream in = new PipedInputStream(outStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF_8));
        new Thread(() -> {
            out.println("line: " + 1);
            out.println("line: " + 2);
            out.println("line: " + 3);
            out.close();
        }).start();

        List<String> rs = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            rs.add(line);
        }
        Assertions.assertEquals(rs, Arrays.asList("line: 1", "line: 2", "line: 3"));
    }

    @Disabled
    @Test
    public void realtimeTest()
            throws InterruptedException
    {
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    for (int i = 0; i < 15; i++) {
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println("time: " + System.currentTimeMillis());
                    }
                    return 0;
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();
        int exitCode = launcher.startAndGet();
        Assertions.assertEquals(exitCode, 0);
    }

    @Test
    public void returnValueTest()
            throws InterruptedException
    {
        JVMLauncher<byte[]> launcher = JVMLaunchers.<byte[]>newJvm()
                .task(() -> {
                    byte[] bytes = new byte[85000];
                    Arrays.fill(bytes, (byte) 1);
                    return bytes;
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        byte[] bytes = new byte[85000];
        Arrays.fill(bytes, (byte) 1);
        for (int i = 0; i < 3; i++) {
            System.out.println("************ check" + i);
            byte[] vmLoadBytes = launcher.startAndGet();
            Assertions.assertArrayEquals(vmLoadBytes, bytes);
        }
    }

    @Test
    public void setJavaHomeTest()
            throws InterruptedException
    {
        JVMLauncher<List<String>> launcher = JVMLaunchers.<List<String>>newJvm()
                .javaHome(System.getProperty("java.home"))
                .task(() -> {
                    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
                    List<String> jvmArgs = runtimeMXBean.getInputArguments();
                    return jvmArgs;
                })
                .addUserJars(new URL[0])
                .setXms("5m")
                .addVmOps("-Xmx5m")
                .setConsole(System.out::println)
                .build();

        List<String> vmResult = launcher.startAndGet();
        Assertions.assertTrue(vmResult.contains("-Xms5m"));
        Assertions.assertTrue(vmResult.contains("-Xmx5m"));
    }

    @Test
    public void hookTest()
            throws InterruptedException
    {
        List<String> logs = new ArrayList<>();
        String hookLog = "child jvm shutdownHook test";
        JVMLauncher<Long> launcher = JVMLaunchers.<Long>newJvm()
                .task(() -> {
                    //TimeUnit.SECONDS.sleep(1000000);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println(hookLog);
                    }));
                    return 1L;
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(line -> {
                    logs.add(line);
                    System.out.println(line);
                }).build();

        long out = launcher.startAndGet();
        Assertions.assertEquals(out, 1L);
        Assertions.assertEquals(logs, Collections.singletonList(hookLog));
    }

    @Test
    public void getForkJvmPidTest()
            throws InterruptedException
    {
        JVMLauncher<Long> launcher = JVMLaunchers.<Long>newJvm()
                .task(() -> {
                    //TimeUnit.SECONDS.sleep(1000000);
                    System.out.println("************ job start ***************");
                    return Platform.getCurrentProcessId();
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        VmPromise<Long> out = launcher.start();
        System.out.println("pid is " + out.pid());
        Assertions.assertEquals(out.call().longValue(), out.pid());
    }

    @Test
    public void java11WarringTest()
            throws InterruptedException
    {
        if (!(Platform.getJavaVersion() > 8 && Platform.getJavaVersion() < 16)) {
            return;
        }
        List<String> childVmLogs = new ArrayList<>();
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .task(() -> {
                    System.out.println("System.out: child jvm is running.");
                    System.err.println("System.err: child jvm is running.");
                    StackTraceElement element = Threads.getJvmMainClass();
                    Arrays.stream(Thread.currentThread().getStackTrace()).forEach(x -> System.out.println(x));
                    System.out.println(element);
                    return element.getClassName();
                })
                .setXms("16m")
                .setXmx("16m")
                .setConsole(line -> {
                    childVmLogs.add(line);
                    System.out.println(line);
                }).build();
        String rs = launcher.startAndGet();
        Assertions.assertEquals(JVMLauncher.class.getName(), rs);
        Assertions.assertTrue(childVmLogs.contains("WARNING: An illegal reflective access operation has occurred"));
        Assertions.assertTrue(childVmLogs.contains("WARNING: Please consider reporting this to the maintainers of com.github.harbby.gadtry.base.Threads"));
    }

    @Test
    public void java16PlusErrorTest()
            throws InterruptedException
    {
        if (Platform.getJavaVersion() < 16) {
            return;
        }
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .task(() -> {
                    System.out.println("System.out: child jvm is running.");
                    System.err.println("System.err: child jvm is running.");
                    StackTraceElement element = Threads.getJvmMainClass();
                    return element.getClassName();
                })
                .setXms("16m")
                .setXmx("16m")
                .build();

        try {
            launcher.startAndGet();
            Assertions.fail();
        }
        catch (JVMException e) {
            Assertions.assertTrue(e.getMessage().contains("java.lang.reflect.InaccessibleObjectException: Unable to" +
                    " make private static native java.lang.Thread[] java.lang.Thread.getThreads()" +
                    " accessible: module java.base does not \"opens java.lang\" to unnamed module"));
        }
    }

    @Test
    public void setTaskNameTest()
            throws InterruptedException
    {
        String taskName = "TestForkJvmReturn1";
        JVMLaunchers.VmBuilder<String> builder = JVMLaunchers.<String>newJvm()
                .task(() -> {
                    //TimeUnit.SECONDS.sleep(1000000);
                    System.out.println("System.out: child jvm is running.");
                    System.err.println("System.err: child jvm is running.");
                    StackTraceElement element = Threads.getJvmMainClass();
                    Arrays.stream(Thread.currentThread().getStackTrace()).forEach(x -> System.out.println(x));
                    System.out.println(element);
                    return element.getClassName();
                })
                .addUserJars(Collections.emptyList())
                .setName(taskName)
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println);
        if (Platform.getJavaVersion() >= 16) {
            builder.addVmOps("--add-opens=java.base/java.lang=ALL-UNNAMED");
        }
        JVMLauncher<String> launcher = builder.build();
        Assertions.assertEquals(launcher.startAndGet(), JVMLauncher.class.getPackage().getName() + "." + taskName);
    }

    @Test
    public void testForkJvmThrowRuntimeException123()
            throws Exception
    {
        String f = "testForkJvmThrowRuntimeException123";
        System.out.println("--- vm test ---");
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    System.out.println("************ job start ***************");
                    throw new RuntimeException(f);
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        try {
            launcher.start().call();
            Assertions.fail();
        }
        catch (JVMException e) {
            Assertions.assertTrue(e.getMessage().contains(f));
        }
    }

    @Test
    public void testStartVMError()
            throws Exception
    {
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> 0)
                .setClassLoader(this.getClass().getClassLoader())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .ignoreParentClasspath()
                .build();
        VmPromise<Integer> promise = launcher.start();
        try {
            promise.call();
            Assertions.fail();
        }
        catch (JVMException e) {
            String errorMsg = e.getMessage();
            Assertions.assertTrue(errorMsg.contains(JVMLauncher.class.getName()));
        }
    }

    @Test
    public void testForkJvmEnv()
            throws JVMException, InterruptedException
    {
        String envValue = "value_007";
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .task(() -> {
                    //TimeUnit.SECONDS.sleep(1000000);
                    System.out.println("************ job start ***************");
                    String env = System.getenv("TestEnv");
                    Assertions.assertEquals(env, envValue);
                    Assertions.assertEquals(System.getenv("k1"), "v1");
                    return env;
                })
                .addEnvironment("TestEnv", envValue)
                .addEnvironment(MutableMap.of("k1", "v1"))
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        String out = launcher.startAndGet();
        Assertions.assertEquals(out, envValue);
    }

    @Test
    public void actorModelTest1()
            throws InterruptedException
    {
        String f = "testForkJvmThrowRuntimeException123";
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    System.out.println("child vm stared");
                    throw new RuntimeException(f);
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        final Object lock = new Object();
        CompletableFuture.supplyAsync(() -> {
            try {
                return launcher.startAndGet();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((code, error) -> {
            Assertions.assertTrue(error.getMessage().contains(f));
            synchronized (lock) {
                lock.notify();
            }
        });
        synchronized (lock) {
            lock.wait();
        }
    }

    @Test
    public void testActorModelForkReturn2019()
            throws JVMException, InterruptedException
    {
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    System.out.println("child vm stared");
                    return 2019;
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();
        CompletableFuture.supplyAsync(() -> {
            try {
                return launcher.startAndGet();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((value, error) -> {
            Assertions.assertEquals(2019, value.intValue());
            System.out.println(value);
            lock.lock();
            condition.signal();  //唤醒睡眠的主线程
            lock.unlock();
        });
        // LockSupport.class
        condition.await(); //睡眠进入等待池并让出锁
        lock.unlock();
    }

    @Test
    public void workDirTest()
            throws JVMException, InterruptedException
    {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        JVMLauncher<File> launcher = JVMLaunchers.<File>newJvm()
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setWorkDirectory(dir)
                .setConsole(System.out::println)
                .build();

        File jvmWorkDir = launcher.startAndGet(() -> {
            return new File(System.getProperty("user.dir"));
        });
        if (Platform.isMac()) {
            Assertions.assertEquals(new File("/private", dir.getPath()), jvmWorkDir);
        }
        else {
            Assertions.assertEquals(dir, jvmWorkDir);
        }
    }

    @Test
    public void should_success_timeoutTest()
    {
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    TimeUnit.DAYS.sleep(1);
                    return 0;
                }).addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .timeout(200, TimeUnit.MILLISECONDS)
                .build();
        Assertions.assertThrows(JVMTimeoutException.class, ()-> {
            launcher.startAndGet();
        });
    }

    @Test
    public void should_success_redirectOutputToNull()
            throws InterruptedException
    {
        String msg = "hello gadtry.";
        List<String> logs = new ArrayList<>();
        JVMLauncher<Integer> baseLauncher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    System.out.println(msg);
                    return 0;
                }).addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(logs::add)
                .build();
        Assertions.assertEquals(0, baseLauncher.startAndGet().intValue());
        Assertions.assertEquals(Collections.singletonList(msg), logs);

        List<String> testLogs = new ArrayList<>();
        JVMLauncher<Integer> testLauncher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    System.out.println(msg);
                    return 0;
                }).addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(testLogs::add)
                .redirectOutputToNull()
                .build();
        Assertions.assertEquals(0, testLauncher.startAndGet().intValue());
        Assertions.assertTrue(testLogs.isEmpty());
    }

    @Test
    public void should_success_childBlockIO()
            throws InterruptedException
    {
        JVMLauncher<Integer> baseLauncher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    byte[] bytes = new byte[85000];
                    Arrays.fill(bytes, (byte) 1);
                    System.out.write(bytes);
                    return 0;
                }).addUserJars(Collections.emptyList())
                .setXms("64m")
                .setXmx("64m")
                .setConsole(System.out::println)
                .autoExit()
                .build();
        VmPromise<Integer> vmPromise = baseLauncher.start();
        TimeUnit.MILLISECONDS.sleep(1000);
        try {
            Assertions.assertTrue(vmPromise.isAlive());
        }
        finally {
            vmPromise.cancel();
        }
    }

    @Test
    public void should_success_childNotBlockIO_by_redirectOutputToNull()
            throws InterruptedException
    {
        JVMLauncher<Integer> baseLauncher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    byte[] bytes = new byte[85000];
                    Arrays.fill(bytes, (byte) 1);
                    System.out.write(bytes);
                    return 0;
                }).addUserJars(Collections.emptyList())
                .setXms("64m")
                .setXmx("64m")
                .setConsole(System.out::println)
                .autoExit()
                .redirectOutputToNull()
                .build();
        VmPromise<Integer> vmPromise = baseLauncher.start();
        while (vmPromise.isAlive()) {
            TimeUnit.MILLISECONDS.sleep(10);
        }
        Assertions.assertEquals(0, vmPromise.call().intValue());
    }
}
