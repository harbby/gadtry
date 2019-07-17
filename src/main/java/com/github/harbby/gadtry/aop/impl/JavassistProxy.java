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
package com.github.harbby.gadtry.aop.impl;

import com.github.harbby.gadtry.collection.mutable.MutableList;
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
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.annotation.Annotation;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Throwables.throwsException;

public class JavassistProxy
        implements Serializable
{
    private static final AtomicLong number = new AtomicLong(0);
    private static final Map<ClassLoader, ConcurrentMap<KeyX, Class<?>>> proxyCache = new IdentityHashMap<>();

    private JavassistProxy() {}

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader loader, InvocationHandler handler, Class<?>... interfaces)
    {
        try {
            Class<?> aClass = getProxyClass(loader, interfaces);
            //--存在可能没有无参构造器的问题
            Object obj = UnsafeHelper.getUnsafe().allocateInstance(aClass);  //aClass.newInstance();
            ((ProxyHandler) obj).setHandler(handler);
            return (T) obj;
        }
        catch (Exception e) {
            throw throwsException(e);
        }
    }

    public static boolean isProxyClass(Class<?> cl)
    {
        return ProxyHandler.class.isAssignableFrom(cl) &&
                proxyCache.get(cl.getClassLoader()).containsValue(cl);
    }

    public static InvocationHandler getInvocationHandler(Object proxy)
            throws IllegalArgumentException
    {
        checkState(isProxyClass(proxy.getClass()), proxy + " is not a Proxy obj");
        return ((ProxyHandler) proxy).getHandler();
    }

    public static Class<?> getProxyClass(ClassLoader classLoader, Class<?>... interfaces)
            throws Exception
    {
        final ClassLoader loader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        KeyX name = new KeyX(interfaces); //interfaces[0].getName();
        return getClassLoaderProxyCache(loader).computeIfAbsent(name, key -> {
            try {
                return createProxyClass(loader, interfaces);
            }
            catch (Exception e) {
                throw throwsException(e);
            }
        });
    }

    private static ConcurrentMap<KeyX, Class<?>> getClassLoaderProxyCache(ClassLoader loader)
    {
        ConcurrentMap<KeyX, Class<?>> classMap = proxyCache.get(loader);
        if (classMap != null) {
            return classMap;
        }

        synchronized (proxyCache) {
            if (!proxyCache.containsKey(loader)) {
                ConcurrentMap<KeyX, Class<?>> tmp = new ConcurrentHashMap<>();  //HashMap
                proxyCache.put(loader, tmp);
                return tmp;
            }
            else {
                return proxyCache.get(loader);
            }
        }
    }

    private static Class<?> createProxyClass(ClassLoader loader, Class<?>... interfaces)
            throws Exception
    {
        Class<?> superclass = interfaces[0];
        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(loader));

        // New Create Proxy Class
        CtClass proxyClass = classPool.makeClass(JavassistProxy.class.getPackage().getName() + ".$JvstProxy" + number.getAndIncrement() + "$" + superclass.getSimpleName());
        CtClass parentClass = classPool.get(superclass.getName());
        final List<CtClass> ctInterfaces = MutableList.of(parentClass);

        // 添加继承父类
        if (superclass.isInterface()) {
            proxyClass.addInterface(parentClass);
        }
        else {
            checkState(!java.lang.reflect.Modifier.isFinal(superclass.getModifiers()), superclass + " is final");
            proxyClass.setSuperclass(parentClass);
        }

        for (int i = 1; i < interfaces.length; i++) {
            if (interfaces[i] == ProxyHandler.class) {
                continue;
            }
            CtClass ctClass = classPool.get(interfaces[i].getName());
            if (!ctClass.isInterface()) {
                throw new CannotCompileException(ctClass.getName() + " not is Interface");
            }

            ctInterfaces.add(ctClass);
            proxyClass.addInterface(ctClass);
        }
        //check duplicate class
        for (Map.Entry<CtClass, List<CtClass>> entry : ctInterfaces.stream().collect(Collectors.groupingBy(k -> k)).entrySet()) {
            if (entry.getValue().size() > 1) {
                throw new DuplicateMemberException("duplicate class " + entry.getKey().getName());
            }
        }

        // 添加 ProxyHandler 接口
        installProxyHandlerInterface(classPool, proxyClass);

        // 添加方法和字段
        installFieldAndMethod(proxyClass, ctInterfaces);

        // 设置代理类的类修饰符
        proxyClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);

        //-- 添加构造器
        if (parentClass.getConstructors().length == 0) {
            addVoidConstructor(proxyClass);  //如果没有 任何非私有构造器,则添加一个
        }

        // 持久化class到硬盘, 可以直接反编译查看
        //proxyClass.writeFile(".");
        return proxyClass.toClass(loader, superclass.getProtectionDomain());
    }

    private static void installFieldAndMethod(CtClass proxyClass, List<CtClass> ctInterfaces)
            throws NotFoundException, CannotCompileException
    {
        Map<CtMethod, String> methods = new IdentityHashMap<>();
        MutableList.Builder<CtMethod> builder = MutableList.builder(); //不能使用 Set进行去重。否则泛型方法会丢失
        ctInterfaces.forEach(ctClass -> {
            builder.addAll(ctClass.getMethods());
            builder.addAll(ctClass.getDeclaredMethods());
        });
        List<CtMethod> methodList = builder.build();
        methodList.stream().filter(ctMethod -> {
            //final or private or static 的方法都不会继承和代理
            return !(Modifier.isFinal(ctMethod.getModifiers()) ||
                    Modifier.isPrivate(ctMethod.getModifiers()) ||
                    Modifier.isStatic(ctMethod.getModifiers()));
        }).forEach(ctMethod -> methods.put(ctMethod, ""));

        int methodIndex = 0;
        for (CtMethod ctMethod : methods.keySet()) {
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
        CtClass proxyHandler = classPool.get(ProxyHandler.class.getName());
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

        //add Override
        Annotation annotation = new Annotation(Override.class.getName(), proxyMethod.getMethodInfo().getConstPool());
        AnnotationsAttribute attribute = new AnnotationsAttribute(proxyMethod.getMethodInfo().getConstPool(), AnnotationsAttribute.visibleTag);
        attribute.addAnnotation(annotation);
        proxyMethod.getMethodInfo().addAttribute(attribute);

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
