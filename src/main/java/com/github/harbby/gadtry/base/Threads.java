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
package com.github.harbby.gadtry.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public final class Threads
{
    private Threads() {}

    public static Thread[] getThreads()
    {
        try {
            Method method = Thread.class.getDeclaredMethod("getThreads");
            method.setAccessible(true);
            return (Thread[]) method.invoke(null);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw Throwables.throwsThrowable(e);
        }
    }

    public static StackTraceElement getThreadMainFunc(Thread thread)
    {
        StackTraceElement[] stackTraceElements = thread.getStackTrace();
        checkState(stackTraceElements.length > 0, thread + " not exists Stack Trace");
        return stackTraceElements[stackTraceElements.length - 1];
    }

    public static StackTraceElement getJvmMainClass()
    {
        Thread mainThread = getMainThread();
        return getThreadMainFunc(mainThread);
    }

    public static Thread getMainThread()
    {
        Thread[] threads = getThreads();
        for (Thread thread : threads) {
            if (thread.getId() == 1) {
                return thread;
            }
        }
        throw new IllegalStateException("not support this jvm");
    }
}
