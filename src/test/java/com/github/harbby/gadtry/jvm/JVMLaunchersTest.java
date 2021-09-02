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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class JVMLaunchersTest
{
    @Test
    public void returnValueTest()
            throws Exception
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
                .setConsole(msg -> System.out.print(msg))
                .build();

        byte[] bytes = new byte[85000];
        Arrays.fill(bytes, (byte) 1);
        IntStream.range(0, 3).forEach(i -> {
            System.out.println("************ check" + i);
            byte[] vmLoadBytes = launcher.startAndGet();
            Assert.assertArrayEquals(vmLoadBytes, bytes);
        });
    }

    @Test
    public void setJavaHomeTest()
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
                .setConsole(msg -> System.out.print(msg))
                .build();

        List<String> vmResult = launcher.startAndGet();
        Assert.assertTrue(vmResult.contains("-Xms5m"));
        Assert.assertTrue(vmResult.contains("-Xmx5m"));
    }

    @Test
    public void getForkJvmPidTest()
            throws InterruptedException
    {
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    //TimeUnit.SECONDS.sleep(1000000);
                    System.out.println("************ job start ***************");
                    return 1;
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(msg -> System.out.println(msg))
                .build();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        VmFuture<Integer> out = launcher.startAsync(executor);
        System.out.println("pid is " + out.getPid());
        Assert.assertEquals(out.get().intValue(), 1);
        executor.shutdown();
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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            launcher.startAsync(executor).get();
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertTrue(e.getMessage().contains(f));
        }
        finally {
            executor.shutdown();
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
                .notDependParentJvmClassPath()
                .build();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            launcher.startAsync(executor).get();
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertTrue(e.getMessage().contains(JVMLauncher.class.getName()));
            e.printStackTrace();
        }
        finally {
            executor.shutdown();
        }
    }

    @Test
    public void testForkJvmEnv()
            throws JVMException
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
                .setConsole(msg -> System.out.println(msg))
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
                    System.out.println("************ job start ***************");
                    throw new RuntimeException(f);
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        // 使用如下方式 对actor模型 进行测试
        final Object lock = new Object();
        synchronized (lock) {
            CompletableFuture.runAsync(launcher::startAndGet).whenComplete((value, error) -> {
                Assert.assertTrue(error.getMessage().contains(f));
                error.printStackTrace();
                synchronized (lock) {
                    lock.notify();   //唤醒主线程
                }
            });
            lock.wait(500_000); //开始睡眠并让出锁
        }
    }

    @Test
    public void testActorModelForkReturn2019()
            throws JVMException, InterruptedException
    {
        JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
                .task(() -> {
                    System.out.println("************ job start ***************");
                    return 2019;
                })
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setConsole(System.out::println)
                .build();

        // 使用如下方式 对actor模型 进行测试
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();

        CompletableFuture.supplyAsync(launcher::startAndGet)
                .whenComplete((value, error) -> {
                    Assert.assertEquals(2019, value.intValue());
                    System.out.println(value);
                    lock.lock();
                    condition.signal();  //唤醒睡眠的主线程
                    lock.unlock();
                });

        // LockSupport.class
        condition.await(600, TimeUnit.SECONDS); //睡眠进入等待池并让出锁
        lock.unlock();
    }

    @Test
    public void workDirTest()
            throws JVMException
    {
        File dir = new File("/tmp");
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .addUserJars(Collections.emptyList())
                .setXms("16m")
                .setXmx("16m")
                .setWorkDirectory(dir)
                .setConsole(System.out::println)
                .build();

        String jvmWorkDir = launcher.startAndGet(() -> {
            return System.getProperty("user.dir");
        });

        Assert.assertArrayEquals(dir.list(), new File(jvmWorkDir).list());
    }
}
