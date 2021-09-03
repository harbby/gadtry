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

import sun.nio.ch.DirectBuffer;

import java.lang.invoke.MethodHandles;

class PlatformBaseImpl
        implements Platform.PlatformBase
{
    /**
     * Converts the class byte code to a java.lang.Class object
     * This method is available in Java 9 or later
     *
     * @param buddyClass neighbor Class
     * @param classBytes byte code
     * @throws IllegalAccessException buddyClass has no permission to define the class
     */
    @Override
    public Class<?> defineClass(Class<?> buddyClass, byte[] classBytes)
            throws IllegalAccessException
    {
        Platform.class.getModule().addReads(buddyClass.getModule());
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandles.Lookup prvlookup = MethodHandles.privateLookupIn(buddyClass, lookup);
        return prvlookup.defineClass(classBytes);
    }

    @Override
    public void freeDirectBuffer(DirectBuffer buffer)
    {
        jdk.internal.ref.Cleaner cleaner = buffer.cleaner();
        cleaner.clean();
    }

    @Override
    public Object createCleaner(Object ob, Runnable thunk)
    {
        return jdk.internal.ref.Cleaner.create(ob, thunk);
    }

    @Override
    public ClassLoader latestUserDefinedLoader()
    {
        return jdk.internal.misc.VM.latestUserDefinedLoader();
    }

    @Override
    public ClassLoader getBootstrapClassLoader()
    {
        return java.lang.ClassLoader.getPlatformClassLoader();
    }

    @Override
    public long getProcessPid(Process process)
    {
        return process.pid();
    }

    @Override
    public long getCurrentProcessId()
    {
        java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
        return runtime.getPid();
    }
}
