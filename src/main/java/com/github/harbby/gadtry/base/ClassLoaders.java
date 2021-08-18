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
import java.net.URL;
import java.net.URLClassLoader;

public final class ClassLoaders
{
    private ClassLoaders() {}

    /**
     * jdk8:  sun.misc.VM.latestUserDefinedLoader()
     * jdk9+: jdk.internal.misc.VM.latestUserDefinedLoader()
     *
     * @return latestUserDefinedLoader
     */
    public static ClassLoader latestUserDefinedLoader()
    {
        try {
            if (Platform.getClassVersion() > 52) {
                return (ClassLoader) Class.forName("jdk.internal.misc.VM")
                        .getMethod("latestUserDefinedLoader")
                        .invoke(null);
            }
            else {
                return (ClassLoader) Class.forName("sun.misc.VM")
                        .getMethod("latestUserDefinedLoader")
                        .invoke(null);
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw Throwables.throwsThrowable(e);
        }
    }

    /**
     * 用户系统类加载器
     *
     * @return appClassLoader
     */
    public static ClassLoader getAppClassLoader()
    {
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * 获取jdk类加载器
     *
     * @return bootstrap ClassLoader
     */
    public static ClassLoader getBootstrapClassLoader()
    {
        try {
            Object upath = Class.forName("sun.misc.Launcher").getMethod("getBootstrapClassPath").invoke(null);
            URL[] urls = (URL[]) Class.forName("sun.misc.URLClassPath").getMethod("getURLs").invoke(upath);
            return new URLClassLoader(urls, null);
        }
        catch (Exception ignored) {
        }
        //jdk9+
        try {
            return (ClassLoader) Class.forName("java.lang.ClassLoader").getMethod("getPlatformClassLoader").invoke(null);
        }
        catch (Exception e) {
            throw Throwables.throwsThrowable(e);
        }
    }
}
