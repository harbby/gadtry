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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

class ExtPlatformImpl
        implements Platform.ExtPlatform
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
    public Class<?> defineHiddenClass(Class<?> buddyClass, byte[] classBytes, boolean initialize)
            throws IllegalAccessException
    {
        //Platform.getJavaVersion() >= 15
        throw new PlatFormUnsupportedOperation("platform not support this method, must be >= java15");
    }

    @Override
    public void freeDirectBuffer(ByteBuffer buffer)
    {
        Platform.unsafe.invokeCleaner(buffer);
    }

    @Override
    public Object createCleaner(Object ob, Runnable thunk)
    {
        // --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED
        // addExportsOrOpensJavaModules(Class.forName("jdk.internal.ref.Cleaner"), Platform.class, false);
        return jdk.internal.ref.Cleaner.create(ob, thunk);
    }

    @Override
    public boolean isDirectMemoryPageAligned()
    {
        // jdk.internal.misc.VM.isDirectMemoryPageAligned();
        try {
            Class<?> aClass = Class.forName("jdk.internal.misc.VM");
            Field field = aClass.getDeclaredField("pageAlignDirectMemory");
            return Platform.unsafe.getBoolean(aClass, Platform.unsafe.staticFieldOffset(field));
        }
        catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
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

    @Override
    public boolean isOpen(Class<?> source, Class<?> target)
    {
        return source.getModule().isOpen(source.getPackageName(), target.getModule());
    }

    public static void addExportsOrOpensJavaModules(Class<?> hostClass, Class<?> targetClass, boolean tryOpen)
    {
        Module hostModule = hostClass.getModule();
        Module targetModule = targetClass.getModule();
        String hostPackageName = hostClass.getPackageName();
        // check isOpen or isExported
        if (tryOpen) {
            if (hostModule.isOpen(hostPackageName, targetModule)) {
                return;
            }
        }
        else {
            if (hostModule.isExported(hostPackageName, targetModule)) {
                return;
            }
        }
        try {
            Module.class.getModule().addOpens(Module.class.getPackageName(), Platform.class.getModule());
            Method method = Module.class.getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(hostModule, hostPackageName, targetModule, /*open*/tryOpen, /*syncVM*/true);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
    }
}
