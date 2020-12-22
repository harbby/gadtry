package com.github.harbby.gadtry.base;

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

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
}