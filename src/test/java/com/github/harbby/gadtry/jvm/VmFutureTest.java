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

import com.github.harbby.gadtry.aop.mock.Mock;
import com.github.harbby.gadtry.aop.mock.MockGo;
import com.github.harbby.gadtry.base.Closeables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class VmFutureTest
{
    @Test
    public void taskErrorTest()
            throws JVMException
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole((msg) -> System.out.println(msg))
                .setCallable(() -> {
                    throw new IOException("form jvm task test");
                }).build();

        try {
            launcher.startAndGet();
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertTrue(e.getMessage().contains("java.io.IOException: form jvm task test"));
        }
    }

    @Test
    public void taskErrorExitTest()
            throws JVMException
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole((msg) -> System.out.println(msg))
                .setCallable(() -> {
                    System.exit(-1);
                    return "done";
                })
                .build();

        try {
            launcher.startAndGet();
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertEquals(e.getMessage(),
                    "Jvm child process abnormal exit, exit code 255");
        }
    }

    @Test
    public void getTimeOutGive100ms()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole((msg) -> System.out.println(msg))
                .setCallable(() -> {
                    LockSupport.park();
                    return "done";
                })
                .build();
        try (Closeables<VmFuture<String>> vmFuture = Closeables.autoClose(launcher.startAsync(), VmFuture::cancel)) {
            vmFuture.get().get(100, TimeUnit.MILLISECONDS);
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof TimeoutException);
        }
    }

    @Test
    public void getTimeOut()
            throws JVMException, InterruptedException, ExecutionException
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .setCallable(() -> {
                    return "done";
                })
                .build();
        try (Closeables<VmFuture<String>> vmFuture = Closeables.autoClose(launcher.startAsync(), VmFuture::cancel)) {
            Assert.assertEquals(vmFuture.get().get(), "done");
        }
    }

    @Test
    public void isRunning()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .setCallable(() -> {
                    LockSupport.park();
                    return "done";
                }).build();

        try (Closeables<VmFuture<String>> vmFuture = Closeables.autoClose(launcher.startAsync(), VmFuture::cancel)) {
            Assert.assertTrue(vmFuture.get().isRunning());
        }
    }

    @Test
    public void getPid()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole((msg) -> System.out.println(msg))
                .setCallable(() -> {
                    LockSupport.park();
                    return "done";
                }).build();

        try (Closeables<VmFuture<String>> vmFuture = Closeables.autoClose(launcher.startAsync(), VmFuture::cancel)) {
            Assert.assertTrue(vmFuture.get().getPid() > 0);
        }
    }

    @Test
    public void getTestGiveDone()
    {
        AtomicReference<Process> processAtomic = new AtomicReference<>();
        try {
            new VmFuture<>(processAtomic, () -> new VmResult<>((Serializable) "done"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Async failed! future.isDone() result:done");
        }
    }

    @Test
    public void getTestGiveRuntimeException()
            throws InterruptedException
    {
        AtomicReference<Process> processAtomic = new AtomicReference<>();
        try {
            new VmFuture<>(processAtomic, () -> {
                throw new RuntimeException("Async failed! future.isDone() result:done");
            });
            Assert.fail();
        }
        catch (JVMException e) {
            Assert.assertEquals(e.getMessage(), "java.lang.RuntimeException: Async failed! future.isDone() result:done");
        }
    }

    @Test
    public void getTest()
            throws InterruptedException, IOException, TimeoutException, ExecutionException
    {
        File java = new File(new File(System.getProperty("java.home"), "bin"), "java");
        Process process = Runtime.getRuntime().exec(java.toString() + " --version");
        AtomicReference<Process> processAtomic = new AtomicReference<>(process);

        VmFuture<String> vmFuture = new VmFuture<>(processAtomic, () -> new VmResult<>((Serializable) "done"));
        String result = vmFuture.get(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(result, "done");
        Assert.assertFalse(vmFuture.isRunning());
    }

    @Mock private Process process;

    @Before
    public void setUp()
    {
        MockGo.initMocks(this);
    }

    @Test
    public void getVmProcess()
            throws InterruptedException
    {
        AtomicReference<Process> processAtomic = new AtomicReference<>(process);
        VmFuture<String> vmFuture = new VmFuture<>(processAtomic, () -> new VmResult<>((Serializable) "done"));
        try {
            vmFuture.getPid();
            Assert.fail();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("Only support for UNIX and Linux systems pid"));
        }
    }
}
