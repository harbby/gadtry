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
package com.github.harbby.gadtry.aop.impl;

import com.github.harbby.gadtry.memory.UnsafeHelper;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import sun.reflect.CallerSensitive;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.Map;
import java.util.WeakHashMap;

import static com.github.harbby.gadtry.base.Checks.checkState;

public class JavassistProxy
        implements Serializable
{
    private static final Map<ClassLoader, Map<String, Class<?>>> proxyCache = new WeakHashMap<>();

    private JavassistProxy() {}

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader loader, Class<?> interfaces, InvocationHandler handler)
            throws IllegalArgumentException
    {
        try {
            Class<?> aClass = getProxyClass(loader, interfaces);
            //--存在可能没有无参构造器的问题
            Object obj = UnsafeHelper.getUnsafe().allocateInstance(aClass);  //aClass.newInstance();

            ((Proxy.ProxyHandler) obj).setHandler(handler);
            return (T) obj;
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isProxyClass(Class<?> cl)
    {
        return Proxy.ProxyHandler.class.isAssignableFrom(cl);
    }

    @CallerSensitive
    public static InvocationHandler getInvocationHandler(Object proxy)
            throws IllegalArgumentException
    {
        checkState(proxy instanceof Proxy.ProxyHandler, proxy + " is not a Proxy obj");
        return ((Proxy.ProxyHandler) proxy).getHandler();
    }

    private static Class<?> getCache(ClassLoader loader, Class<?> parent)
    {
        Map<String, Class<?>> classMap = proxyCache.get(loader);
        if (classMap == null) {
            synchronized (proxyCache) {
                classMap = proxyCache.computeIfAbsent(loader, (key) -> new WeakHashMap<>());
            }
        }
        return classMap.get(parent.getName());
    }

    public static void putCache(ClassLoader loader, String className, Class<?> proxyClass)
    {
        synchronized (proxyCache) {
            proxyCache.get(loader).put(className, proxyClass);
        }
    }

    public static Class<?> getProxyClass(ClassLoader loader, Class<?> parent)
            throws Exception
    {
        checkState(!java.lang.reflect.Modifier.isFinal(parent.getModifiers()), parent + " is final");
        Class<?> proxy = getCache(loader, parent);
        if (proxy != null) {
            return proxy;
        }

        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(loader));

        // New Create Proxy Class
        CtClass proxyClass = classPool.makeClass(JavassistProxy.class.getPackage().getName() + "." + parent.getSimpleName() + "$$JvstProxy");
        CtClass parentClass = classPool.get(parent.getName());

        // 添加继承父类
        proxyClass.setSuperclass(parentClass);

        // 添加 ProxyHandler 接口
        installProxyHandlerInterface(classPool, proxyClass);

        // 添加方法和字段
        installFieldAndMethod(proxyClass, parentClass);

        // 设置代理类的类修饰符
        proxyClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);

        //-- 添加构造器
        if (parentClass.getConstructors().length == 0) {
            addVoidConstructor(proxyClass);  //如果没有 任何非私有构造器,则添加一个
        }

        // 持久化class到硬盘, 可以直接反编译查看
        //proxyClass.writeFile(".");

        Class<?> aClass = proxyClass.toClass();
        putCache(loader, parent.getName(), aClass);
        return aClass;
    }

    private static void installFieldAndMethod(CtClass proxyClass, CtClass parentClass)
            throws NotFoundException, CannotCompileException
    {
        int methodIndex = 0;
        for (CtMethod ctMethod : parentClass.getMethods()) {
            //final or private or static 的方法都不会继承和代理
            if (Modifier.isFinal(ctMethod.getModifiers()) ||
                    Modifier.isPrivate(ctMethod.getModifiers()) ||
                    Modifier.isStatic(ctMethod.getModifiers())
            ) {
                continue;
            }
            final String methodFieldName = "_method" + methodIndex++;

            // 添加字段
            String fieldCode = "private static final java.lang.reflect.Method %s = " +
                    "javassist.util.proxy.RuntimeSupport.findSuperClassMethod(%s.class, \"%s\", \"%s\");";
            String fieldSrc = String.format(fieldCode, methodFieldName, proxyClass.getName(), ctMethod.getName(), ctMethod.getSignature());
            CtField ctField = CtField.make(fieldSrc, proxyClass);
            proxyClass.addField(ctField);

            String code = "return ($r) this.handler.invoke(this, %s, $args);";
            String methodBodySrc = String.format(code, methodFieldName);
            addProxyMethod(proxyClass, ctMethod, methodBodySrc);
        }
    }

    private static void installProxyHandlerInterface(ClassPool classPool, CtClass proxyClass)
            throws Exception
    {
        CtClass proxyHandler = classPool.get(Proxy.ProxyHandler.class.getName());
        proxyClass.addInterface(proxyHandler);

        CtField handlerField = CtField.make("private java.lang.reflect.InvocationHandler handler;", proxyClass);
        proxyClass.addField(handlerField);

        //Add Method setHandler
        addProxyMethod(proxyClass, proxyHandler.getDeclaredMethod("setHandler"), "this.handler = $1;");
        //Add Method getHandler
        addProxyMethod(proxyClass, proxyHandler.getDeclaredMethod("getHandler"), "return this.handler;");
    }

    /**
     * 添加方法
     */
    private static void addProxyMethod(CtClass proxy, CtMethod parentMethod, String methodBody)
            throws NotFoundException, CannotCompileException
    {
        int mod = Modifier.FINAL | parentMethod.getModifiers();
        if (Modifier.isNative(mod)) {
            mod = mod & ~Modifier.NATIVE;
        }

        CtMethod proxyMethod = new CtMethod(parentMethod.getReturnType(), parentMethod.getName(), parentMethod.getParameterTypes(), proxy);
        proxyMethod.setModifiers(mod);
        proxyMethod.setBody(methodBody);
        proxy.addMethod(proxyMethod);
    }

    /**
     * 添加无参数构造函数
     */
    private static void addVoidConstructor(CtClass proxy)
            throws CannotCompileException
    {
        CtConstructor ctConstructor = new CtConstructor(new CtClass[] {}, proxy);
        ctConstructor.setBody(";");
        proxy.addConstructor(ctConstructor);
    }
}
