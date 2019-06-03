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

import com.github.harbby.gadtry.base.Closeables;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
            throws JVMException, InterruptedException
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
    public void getVmProcess()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole((msg) -> System.out.println(msg))
                .setCallable(() -> {
                    LockSupport.park();
                    return "done";
                }).build();

        try (Closeables<VmFuture<String>> vmFuture = Closeables.autoClose(launcher.startAsync(), VmFuture::cancel)) {
            Assert.assertTrue(vmFuture.get().getVmProcess().isAlive());
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
}
