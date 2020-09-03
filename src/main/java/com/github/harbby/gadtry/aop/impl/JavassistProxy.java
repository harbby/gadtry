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

import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.mock.MockGoException;
import com.github.harbby.gadtry.base.Strings;
import com.github.harbby.gadtry.collection.mutable.MutableList;
import com.github.harbby.gadtry.collection.mutable.MutableSet;
import com.github.harbby.gadtry.memory.Platform;
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
import javassist.compiler.CompileError;
import javassist.compiler.Lex;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.aop.runtime.ProxyRuntime.METHOD_START;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Throwables.noCatch;

public class JavassistProxy
        implements Serializable
{
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final AtomicLong number = new AtomicLong(0);
    private static final Map<ClassLoader, ConcurrentMap<KeyX, Class<?>>> proxyCache = new IdentityHashMap<>();

    private JavassistProxy() {}

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ProxyRequest<T> request)
    {
        Class<?> aClass = getProxyClass(request);
        Object obj;
        try {
            //--存在可能没有无参构造器的问题
            obj = Platform.getUnsafe().allocateInstance(aClass);  //aClass.newInstance();
        }
        catch (Exception e) {
            throw new MockGoException("new Instance " + aClass + "failed", e);
        }
        ((ProxyHandler) obj).setHandler(request.getHandler());

        if (request.getTarget() != null) {
            copyObjectFields(request.getSuperclass(), obj, request.getTarget());
        }

        return (T) obj;
    }

    public static <T> T newProxyInstance(ClassLoader loader, InvocationHandler handler, Class<?>... interfaces)
    {
        @SuppressWarnings("unchecked")
        ProxyRequest<T> request = ProxyRequest.builder((Class<T>) interfaces[0])
                .setClassLoader(loader)
                .addInterface(interfaces)
                .setInvocationHandler(handler)
                .disableSuperMethod()
                .build();
        return newProxyInstance(request);
    }

    private static void copyObjectFields(Class<?> superclass, Object proxyObj, Object target)
    {
        if (superclass.isInterface()) {
            return;
        }
        Set<Field> fields = MutableSet.<Field>builder().addAll(superclass.getDeclaredFields())
                .addAll(superclass.getFields())
                .build();

        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(target);
                field.set(proxyObj, value);
            }
            catch (IllegalAccessException e) {
                throw new MockGoException("init proxy object field " + field, e);
            }
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
    {
        checkState(interfaces != null && interfaces.length > 0, "interfaces is Empty");
        ProxyRequest.Builder<?> request = ProxyRequest.builder(interfaces[0])
                .setClassLoader(classLoader);
        if (interfaces.length > 1) {
            request.addInterface(java.util.Arrays.copyOfRange(interfaces, 1, interfaces.length));
        }
        return getProxyClass(request.build());
    }

    private static String getDefaultPackage(Class<?> superclass)
    {
        Package basePackage = superclass.getPackage();
        if (basePackage == null) {
            return JavassistProxy.class.getPackage().getName();
        }
        else if (basePackage.getName().startsWith("java.")) {
            //see: java8 {@like: ProtectionDomain}
            return "gadtry." + basePackage.getName();
        }
        else {
            return basePackage.getName();
        }
    }

    public static Class<?> getProxyClass(ProxyRequest<?> request)
    {
        String basePackage = request.getBasePackage();
        ClassLoader classLoader = request.getClassLoader();

        /**
         * todo: key should basePackage
         * */
        String proxyPackage = Strings.isBlank(basePackage) ? getDefaultPackage(request.getSuperclass()) : basePackage;
        checkState(!proxyPackage.endsWith("."), "basePackage %s endsWith [.]", proxyPackage);

        final ClassLoader loader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;

        List<Class<?>> validClass = new ArrayList<>(request.getInterfaces().length + 1);
        validClass.add(request.getSuperclass());
        for (Class<?> it : request.getInterfaces()) {
            if (it != request.getSuperclass() && it != Serializable.class && it != ProxyHandler.class && !it.isAssignableFrom(request.getSuperclass())) {
                validClass.add(it);
            }
        }

        KeyX name = new KeyX(request.isDisableSuperMethod(), validClass.toArray(EMPTY_CLASS_ARRAY)); //interfaces[0].getName();
        return getClassLoaderProxyCache(loader).computeIfAbsent(name, key -> {
            try {
                return createProxyClass(proxyPackage, request);
            }
            catch (MockGoException e) {
                throw e;
            }
            catch (Exception e) {
                throw new MockGoException("create Proxy Class failed, " + e.getMessage(), e);
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

    private static Class<?> createProxyClass(String basePackage, ProxyRequest<?> request)
            throws Exception
    {
        Class<?> superclass = request.getSuperclass();
        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(request.getClassLoader()));

        // New Create Proxy Class
        CtClass proxyClass = classPool.makeClass(basePackage + ".$JvstProxy" + number.getAndIncrement() + "$" + superclass.getSimpleName());
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

        for (Class<?> it : request.getInterfaces()) {
            if (it == ProxyHandler.class || it.isAssignableFrom(superclass)) {
                continue;
            }
            CtClass ctClass = classPool.get(it.getName());
            checkState(ctClass.isInterface(), ctClass.getName() + " not is Interface");

            ctInterfaces.add(ctClass);
            proxyClass.addInterface(ctClass);
        }

        //和jdk java.lang.reflect.Proxy.getProxyClass 创建代理类保持行为一致，生成的代理类自动继承Serializable接口
        if (!proxyClass.subtypeOf(classPool.get(Serializable.class.getName()))) {
            proxyClass.addInterface(classPool.get(Serializable.class.getName()));
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
        installFieldAndMethod(proxyClass, ctInterfaces, request);

        // 设置代理类的类修饰符
        proxyClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);

        //-- 添加构造器
        if (superclass.getConstructors().length == 0) {
            addVoidConstructor(proxyClass);  //如果没有 任何非私有构造器,则添加一个
        }

        // 持久化class到硬盘, 可以直接反编译查看
        //proxyClass.writeFile("out/.");
        return proxyClass.toClass(request.getClassLoader(), superclass.getProtectionDomain());
    }

    private static void installFieldAndMethod(CtClass proxyClass, List<CtClass> ctInterfaces, ProxyRequest<?> request)
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
            if (request.getSuperclass().isInterface() ||
                    ctMethod.getDeclaringClass().isInterface() ||   //接口的方法或default方法无法super.()调用
                    Modifier.isAbstract(ctMethod.getModifiers()) ||  //Abstract方法也无法被super.()调用
                    request.isDisableSuperMethod() ||
                    (Modifier.isPackage(ctMethod.getModifiers()) &&  //包内级别的无法super.()调用
                            !ctMethod.getDeclaringClass().getPackageName().equals(proxyClass.getPackageName()))
            ) {
                addSupperMethod(proxyClass, ctMethod, methodFieldName);
            }
            else {
                addSupperMethod2(proxyClass, ctMethod, methodFieldName); //这里应该全是可以被super.()调用的方法
            }
        }
    }

    private static void addSupperMethod(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException
    {
        // 添加字段
        String fieldCode = "private static final java.lang.reflect.Method %s = " +
                "javassist.util.proxy.RuntimeSupport.findSuperClassMethod(%s.class, \"%s\", \"%s\");";
        String fieldSrc = String.format(fieldCode, methodNewName, proxyClass.getName(), ctMethod.getName(), ctMethod.getSignature());
        CtField ctField;
        try {
            ctField = CtField.make(fieldSrc, proxyClass);
        }
        catch (CannotCompileException e) {
            if (e.getCause() instanceof CompileError) {
                Lex lex = ((CompileError) e.getCause()).getLex();
                StringBuffer stringBuffer = noCatch(() -> {
                    Field field = Lex.class.getDeclaredField("textBuffer");
                    field.setAccessible(true);
                    return (StringBuffer) field.get(lex);
                });
                throw new MockGoException("package name [" + proxyClass.getPackageName() + "] find exists JAVA system keywords " + stringBuffer, e);
            }
            throw e;
        }

        proxyClass.addField(ctField);

        String code = "return ($r) this.handler.invoke(this, %s, $args);";
        String methodBodySrc = String.format(code, methodNewName);
        addProxyMethod(proxyClass, ctMethod, methodBodySrc);
    }

    /**
     *     @Override
     *     public final int getAge() {
     *         return (Integer)this.handler.invoke(this, _method5, new Object[0]);
     *     }
     *
     *     public int $_getAge() {
     *         return super.getAge();
     *     }
     * */
    private static void addSupperMethod2(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException
    {
        // 添加字段
        String fieldCode = "private static final java.lang.reflect.Method %s = " +
                "com.github.harbby.gadtry.aop.runtime.ProxyRuntime.findProxyClassMethod(%s.class, \"" + METHOD_START + "%s\", %s);";

        String arg;
        if (ctMethod.getParameterTypes().length == 0) {
            arg = null;
        }
        else {
            arg = "new Class[] {"
                    + Stream.of(ctMethod.getParameterTypes()).map(x -> x.getName() + ".class").collect(Collectors.joining(","))
                    + "}";
        }
        String fieldSrc = String.format(fieldCode, methodNewName, proxyClass.getName(), ctMethod.getName(),
                arg);

        CtField ctField;
        try {
            ctField = CtField.make(fieldSrc, proxyClass);
        }
        catch (CannotCompileException e) {
            if (e.getCause() instanceof CompileError) {
                Lex lex = ((CompileError) e.getCause()).getLex();
                StringBuffer stringBuffer = noCatch(() -> {
                    Field field = Lex.class.getDeclaredField("textBuffer");
                    field.setAccessible(true);
                    return (StringBuffer) field.get(lex);
                });
                throw new MockGoException("package name [" + proxyClass.getPackageName() + "] find exists JAVA system keywords " + stringBuffer, e);
            }
            throw e;
        }

        proxyClass.addField(ctField);

        String code = "return ($r) this.handler.invoke(this, %s, $args);";
        String methodBodySrc = String.format(code, methodNewName);
        addProxyMethod(proxyClass, ctMethod, methodBodySrc);
        //------add _Method()---------- // ctMethod.getLongName();
        CtMethod m2 = new CtMethod(ctMethod.getReturnType(), METHOD_START + ctMethod.getName(), ctMethod.getParameterTypes(), proxyClass);
        ctMethod.getMethodInfo().getAttributes().forEach(m2.getMethodInfo()::addAttribute); //add @注解
        m2.setBody("return ($r) super." + ctMethod.getName() + "($$);");
        proxyClass.addMethod(m2);
    }

    private static void installProxyHandlerInterface(ClassPool classPool, CtClass proxyClass)
            throws NotFoundException, CannotCompileException

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

        try {
            proxy.addMethod(proxyMethod);
        }
        catch (DuplicateMemberException e) {
            //todo: Use a more elegant way
        }
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
        //proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
    }
}
