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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JVMLaunchersTest
{
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
            Assert.assertArrayEquals(vmLoadBytes, bytes);
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
        Assert.assertTrue(vmResult.contains("-Xms5m"));
        Assert.assertTrue(vmResult.contains("-Xmx5m"));
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
        Assert.assertEquals(out, 1L);
        Assert.assertEquals(logs, Collections.singletonList(hookLog));
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
        Assert.assertEquals(out.call().longValue(), out.pid());
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
        Assert.assertEquals(JVMLauncher.class.getName(), rs);
        Assert.assertTrue(childVmLogs.contains("WARNING: An illegal reflective access operation has occurred"));
        Assert.assertTrue(childVmLogs.contains("WARNING: Please consider reporting this to the maintainers of com.github.harbby.gadtry.base.Threads"));
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
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertTrue(e.getMessage().contains("java.lang.reflect.InaccessibleObjectException: Unable to" +
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
        Assert.assertEquals(launcher.startAndGet(), JVMLauncher.class.getPackage().getName() + "." + taskName);
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
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertTrue(e.getMessage().contains(f));
        }
    }

    @Test
    public void testStartVMError()
            throws Exception
    {
        String f = "testForkJvmThrowRuntimeException123";
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    //----child vm task
                    return 0;
                })
                .setClassLoader(this.getClass().getClassLoader())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .filterThisJVMClass()
                .build();
        try {
            launcher.start().call();
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertTrue(e.getMessage().contains(JVMLauncher.class.getName()));
            e.printStackTrace();
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
                    Assert.assertEquals(env, envValue);
                    Assert.assertEquals(System.getenv("k1"), "v1");
                    return env;
                })
                .setEnvironment("TestEnv", envValue)
                .setEnvironment(MutableMap.of("k1", "v1"))
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        String out = launcher.startAndGet();
        Assert.assertEquals(out, envValue);
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
        CompletableFuture.supplyAsync(launcher::startAndGet).whenComplete((code, error) -> {
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
        CompletableFuture.supplyAsync(launcher::startAndGet).whenComplete((value, error) -> {
            Assert.assertEquals(2019, value.intValue());
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
            Assert.assertEquals(new File("/private", dir.getPath()), jvmWorkDir);
        }
        else {
            Assert.assertEquals(dir, jvmWorkDir);
        }
    }
}
