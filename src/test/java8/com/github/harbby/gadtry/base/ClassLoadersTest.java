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

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

public class ClassLoadersTest
{
    public static class DemoClass
    {
        public DemoClass()
        {
            ClassLoader classLoader = sun.misc.VM.latestUserDefinedLoader();
            Assert.assertTrue(classLoader instanceof MyClassLoader);
            System.out.println(classLoader);
        }
    }

    public static class MyClassLoader
            extends URLClassLoader
    {
        public MyClassLoader(URL[] urls, ClassLoader parent)
        {
            super(urls, parent);
        }

        public final Class<?> myDefineClass(byte[] b)
        {
            return super.defineClass(null, b, 0, b.length);
        }
    }

    @Test
    public void latestUserDefinedLoader()
            throws Exception
    {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.getCtClass(DemoClass.class.getName());
        ctClass.setName("gadtry.CheckLatestUserDefinedLoaderTest");
        Class<?> aClass = new MyClassLoader(new URL[0], ClassLoader.getSystemClassLoader())
                .myDefineClass(ctClass.toBytecode());
        Object ins = aClass.newInstance();
        System.out.println(ins);
    }

    @Test
    public void loadExtJarToSystemClassLoaderTest()
            throws ClassNotFoundException
    {
        try {
            Class.forName("org.h2.Driver");
            Assert.fail();
        }
        catch (ClassNotFoundException ignored) { }
        String resourceName = "version1/h2-1.4.191.jar";
        URL url = this.getClass().getClassLoader().getResource(resourceName);
        ClassLoaders.loadExtJarToSystemClassLoader(Collections.singletonList(url));
        Class<?> driver = Class.forName("org.h2.Driver");
        Assert.assertTrue(java.sql.Driver.class.isAssignableFrom(driver));
        Assert.assertTrue(ClassLoaders.getSystemClassLoaderJars().stream()
                .anyMatch(x -> x.getPath().endsWith(resourceName)));
    }
}
