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

import com.github.harbby.gadtry.aop.MockGo;
import com.github.harbby.gadtry.function.AutoClose;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class VmFutureTest
{
    @Test
    public void agentTest()
            throws Exception
    {
        Instrumentation instrumentation = MockGo.mock(Instrumentation.class);
        JvmAgent.premain(JVMLauncher.class.getName() + ":newClassName", instrumentation);
        Assertions.assertNotNull(Class.forName(JVMLauncher.class.getPackage().getName() + ".newClassName"));
    }

    @Test
    public void taskErrorTest()
            throws JVMException, InterruptedException
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .task(() -> {
                    throw new IOException("form jvm task test");
                }).build();

        try {
            launcher.startAndGet();
            Assertions.fail();
        }
        catch (JVMException e) {
            Assertions.assertTrue(e.getMessage().contains("java.io.IOException: form jvm task test"));
        }
    }

    @Test
    public void taskErrorExitTest()
            throws JVMException, InterruptedException
    {
        Random random = new Random();
        int exitCode = random.nextInt(255);
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .task(() -> {
                    System.exit(exitCode);
                    return "done";
                }).build();
        try {
            launcher.startAndGet();
            Assertions.fail();
        }
        catch (JVMException e) {
            Assertions.assertEquals(e.getMessage(), "child process abnormal exit, exit code " + exitCode);
        }
    }

    @Test
    public void getTimeOutGive100ms()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .task(() -> {
                    LockSupport.park();
                    return "done";
                })
                .build();
        VmPromise<String> vmPromise = launcher.start();
        try (AutoClose ignored = vmPromise::cancel) {
            vmPromise.call(100, TimeUnit.MILLISECONDS);
            Assertions.fail();
        }
        catch (Exception e) {
            Assertions.assertTrue(e instanceof JVMTimeoutException);
        }
    }

    @Test
    public void getTimeOut()
            throws JVMException, InterruptedException
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .task(() -> {
                    return "done";
                })
                .build();

        VmPromise<String> promise = launcher.start();
        try {
            Assertions.assertEquals(promise.call(), "done");
        }
        finally {
            promise.cancel();
        }
    }

    @Test
    public void isRunning()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .task(() -> {
                    LockSupport.park();
                    return "done";
                }).build();

        VmPromise<String> promise = launcher.start();
        try {
            Assertions.assertTrue(promise.isAlive());
        }
        finally {
            promise.cancel();
        }
    }

    @Test
    public void getPid()
    {
        JVMLauncher<String> launcher = JVMLaunchers.<String>newJvm()
                .setXmx("32m")
                .setConsole(System.out::println)
                .task(() -> {
                    LockSupport.park();
                    return "done";
                }).build();
        VmPromise<String> promise = launcher.start();
        try {
            Assertions.assertTrue(promise.pid() > 0);
        }
        finally {
            promise.cancel();
        }
    }
}
