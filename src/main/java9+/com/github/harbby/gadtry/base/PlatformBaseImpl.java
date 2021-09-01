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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

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

    /**
     * 绕开jdk9模块化引入的访问限制:
     * 1. 非法反射访问警告消除
     * 2. --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED 型访问限制消除， 现在通过这个方法我们可以访问任何jdk限制的内部代码
     * 注: java16开始，你需要提供额外的java运行时参数: --add-opens=java.base/java.lang=ALL-UNNAMED 来使用该方法提供的功能
     * <p>
     * This method is available in Java 9 or later
     *
     * @param hostClass          action将获取hostClass的内部实现反射访问权限
     * @param unnamedModuleClass 期望获取host访问权限的类
     * @throws java.lang.NoClassDefFoundError user dep class
     * @since jdk9+
     */
    @Override
    public void openJavaModuleToUnnamedModule(Class<?> hostClass, Class<?> unnamedModuleClass)
            throws PlatFormUnsupportedOperation
    {
        boolean isUnnamedModule = !unnamedModuleClass.getModule().isNamed();
        checkState(isUnnamedModule, "targetClass " + unnamedModuleClass + " not is UNNAMED module");
        //----------------------
        Module hostModule = hostClass.getModule();
        String hostPackageName = hostClass.getPackageName();
        Module unnamedModule = unnamedModuleClass.getModule();

        if (hostModule.isOpen(hostPackageName, unnamedModule)) {
            hostModule.addOpens(hostPackageName, unnamedModule);
        }
        else {
            try {
                Module.class.getModule().addOpens(Module.class.getPackageName(), Platform.class.getModule());
                Method method = Module.class.getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
                method.setAccessible(true);
                //--add-opens=java.base/$hostPackageName=ALL-UNNAMED
                method.invoke(hostModule, hostPackageName, unnamedModule, /*open*/true, /*syncVM*/true);
            }
            catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new PlatFormUnsupportedOperation(e);
            }
        }
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
}
