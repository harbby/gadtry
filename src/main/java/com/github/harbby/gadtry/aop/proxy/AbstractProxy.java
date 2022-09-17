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
package com.github.harbby.gadtry.aop.proxy;

import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.function.Function;
import com.github.harbby.gadtry.io.IOUtils;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public abstract class AbstractProxy
        implements ProxyFactory
{
    private static final Class<?> DEFAULT_BUDDY_CLASS = AopGo.class;
    private static final String DEFAULT_PACKAGE_NAME = DEFAULT_BUDDY_CLASS.getPackage().getName();
    private final AtomicLong number;
    private final ConcurrentMap<KeyX, Class<?>> proxyCache;

    protected AbstractProxy()
    {
        number = new AtomicLong(0);
        proxyCache = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T newProxyInstance(ClassLoader loader, InvocationHandler handler, Class<?>... interfaces)
    {
        checkState(interfaces != null && interfaces.length > 0, "interfaces is Empty");
        ProxyRequest<?> request = ProxyRequest.builder(interfaces[0])
                .setClassLoader(loader)
                .addInterface(interfaces)
                .setInvocationHandler(handler)
                .build();
        return (T) newProxyInstance(request);
    }

    private static final class Key
    {
        private final String name; // must be interned (as from Method.getName())
        private final Class<?>[] ptypes;

        Key(Method method)
        {
            name = method.getName();
            ptypes = method.getParameterTypes();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o instanceof Key) {
                Key that = (Key) o;
                return name.equals(that.name)
                        && Arrays.equals(ptypes, that.ptypes);
            }
            else {
                return false;
            }
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(name) + // guaranteed interned String
                    31 * Arrays.hashCode(ptypes);
        }
    }

    private static final class MethodList
    {
        private final Map<Key, Method> methodsMap = new HashMap<>();

        void merge(Method[] methods)
        {
            for (Method method : methods) {
                if (!method.isSynthetic()) {  // method.isBridge()
                    methodsMap.putIfAbsent(new Key(method), method);
                }
            }
        }

        public Map<Key, Method> getMerged()
        {
            return methodsMap;
        }
    }

    protected Collection<Method> findProxyMethods(Class<?> supperClass, Collection<Class<?>> interfaces, boolean isAccessClass)
    {
        MethodList methodList = new MethodList();
        methodList.merge(supperClass.getMethods());
        if (supperClass.isInterface()) {
            methodList.merge(Object.class.getMethods());
        }
        else {
            Class<?> it = supperClass;
            while (it != Object.class) {
                methodList.merge(it.getDeclaredMethods());
                it = it.getSuperclass();
            }
        }
        for (Class<?> ctClass : interfaces) {
            methodList.merge(ctClass.getMethods());
        }
        List<Method> methods = new ArrayList<>();
        for (Map.Entry<Key, Method> entry : methodList.getMerged().entrySet()) {
            //final or private or static function not proxy
            Method method = entry.getValue();
            int mod = method.getModifiers();
            boolean checked = !Modifier.isFinal(mod) &&
                    !Modifier.isPrivate(mod) &&
                    !Modifier.isStatic(mod);
            if (!isAccessClass) {
                checked = checked && (Modifier.isPublic(mod) || Modifier.isProtected(mod));
            }
            if (checked) {
                methods.add(method);
            }
        }
        return methods;
    }

    @Override
    public <T> T newProxyInstance(ProxyRequest<T> request)
    {
        Class<? extends T> aClass = getProxyClass(request);
        Function<Class<? extends T>, T, Exception> function = request.getCreateFunction();
        T proxyObj;
        try {
            if (function != null) {
                proxyObj = function.apply(aClass);
            }
            else {
                proxyObj = Platform.allocateInstance(aClass);
            }
        }
        catch (Exception e) {
            throw new MockGoException("new instance proxy class failed", e);
        }
        ((ProxyAccess) proxyObj).setHandler(request.getHandler());
        return proxyObj;
    }

    @Override
    public <T> Class<? extends T> getProxyClass(ClassLoader loader, Class<T> supperClass, Class<?>... interfaces)
    {
        requireNonNull(supperClass, "supperClass is null");
        ProxyRequest<T> request = ProxyRequest.builder(supperClass)
                .addInterface(interfaces)
                .setClassLoader(loader)
                .build();
        return getProxyClass(request);
    }

    @Override
    public boolean isProxyClass(Class<?> cl)
    {
        return ProxyAccess.class.isAssignableFrom(cl) && proxyCache.containsValue(cl);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<? extends T> getProxyClass(ProxyRequest<T> request)
    {
        List<Class<?>> validClass = new ArrayList<>();
        validClass.add(request.getSuperclass());
        validClass.addAll(request.getInterfaces());
        KeyX keyX = new KeyX(validClass);
        return (Class<? extends T>) proxyCache.computeIfAbsent(keyX, k -> {
            try {
                return createProxyClass(request);
            }
            catch (MockGoException e) {
                throw e;
            }
            catch (Exception e) {
                throw new MockGoException("create Proxy Class failed, " + e.getMessage(), e);
            }
        });
    }

    protected abstract String proxyClassNameFlag();

    @Override
    public InvocationHandler getInvocationHandler(Object proxy)
            throws IllegalArgumentException
    {
        checkState(isProxyClass(proxy.getClass()), proxy + " is not a Proxy obj");
        return ((ProxyAccess) proxy).getHandler();
    }

    private String createProxyClassName(Class<?> superclass, boolean isAccessClass)
    {
        String beginName;
        if (isAccessClass) {
            beginName = superclass.getName();
        }
        else {
            beginName = DEFAULT_PACKAGE_NAME + "." + superclass.getSimpleName();
        }
        return String.format("%s$%s%s", beginName, proxyClassNameFlag(), number.getAndIncrement());
    }

    protected abstract byte[] generate(ClassLoader classLoader, String className, Class<?> superclass,
            Set<Class<?>> interfaceSet, Collection<Method> proxyMethods)
            throws Exception;

    protected boolean enableHiddenClass()
    {
        return true;
    }

    private Class<?> createProxyClass(ProxyRequest<?> request)
            throws Exception
    {
        Class<?> superclass = request.getSuperclass();
        boolean isAccessClass = request.isAccessClass();
        final Set<Class<?>> interfaceSet = new HashSet<>();
        for (Class<?> it : request.getInterfaces()) {
            if (it == ProxyAccess.class || it.isAssignableFrom(superclass)) {
                continue;
            }
            checkState(it.isInterface(), "must is interface. " + it);
            interfaceSet.add(it);
        }

        Collection<Method> proxyMethods = findProxyMethods(superclass, interfaceSet, isAccessClass);
        String className = createProxyClassName(superclass, isAccessClass);
        byte[] byteCode = this.generate(request.getClassLoader(), className, superclass, interfaceSet, proxyMethods);
        IOUtils.write(byteCode, new File("out", className.replace('.', '/') + ".class"));
        int vmVersion = Platform.getJavaVersion();
        if (vmVersion < 9) {
            // java 8 or java7
            ClassLoader classLoader = request.getClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            return Platform.defineClass(byteCode, classLoader);
        }

        Class<?> buddyClass = isAccessClass ? superclass : DEFAULT_BUDDY_CLASS;
        if (vmVersion >= 15 && this.enableHiddenClass()) {
            return Platform.defineHiddenClass(buddyClass, byteCode, false);
        }
        // 9 - 14
        return Platform.defineClass(buddyClass, byteCode);
    }
}
