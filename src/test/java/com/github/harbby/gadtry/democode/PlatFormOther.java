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
package com.github.harbby.gadtry.democode;

import com.github.harbby.gadtry.base.PlatFormUnsupportedOperation;
import com.github.harbby.gadtry.base.Platform;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class PlatFormOther
{
    private PlatFormOther() {}

    /**
     * support only jdk8-jdk15
     * load other jar to system classLoader
     *
     * @param urls jars
     */
    public static void loadExtJarToSystemClassLoader(List<URL> urls)
            throws PlatFormUnsupportedOperation
    {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            if (classLoader instanceof URLClassLoader) {
                Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURLMethod.setAccessible(true);
                for (URL uri : urls) {
                    addURLMethod.invoke(classLoader, uri);
                }
                return;
            }
            if (Platform.getJavaVersion() >= 16) {
                throw new UnsupportedOperationException("java16 and above are not supported");
            }
            //support java9-15
            Field field = classLoader.getClass().getDeclaredField("ucp");
            field.setAccessible(true);
            Object ucp = field.get(classLoader);
            Method addURLMethod = ucp.getClass().getMethod("addURL", URL.class);
            for (URL url : urls) {
                addURLMethod.invoke(ucp, url);
            }
        }
        catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
    }

    /**
     * get system classLoader ucp jars
     *
     * @return system classLoader ucp jars
     */
    public static List<URL> getSystemClassLoaderJars()
            throws PlatFormUnsupportedOperation
    {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader instanceof URLClassLoader) {
            //java8
            return java.util.Arrays.asList(((URLClassLoader) classLoader).getURLs());
        }
        if (Platform.getJavaVersion() >= 16) {
            throw new UnsupportedOperationException("java16 and above are not supported");
        }
        //java9-15
        try {
            Field field = classLoader.getClass().getDeclaredField("ucp");
            field.setAccessible(true);
            Object ucp = field.get(classLoader);
            Method getURLs = ucp.getClass().getMethod("getURLs");
            URL[] urls = (URL[]) getURLs.invoke(ucp);
            return java.util.Arrays.asList(urls);
        }
        catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
    }
}
