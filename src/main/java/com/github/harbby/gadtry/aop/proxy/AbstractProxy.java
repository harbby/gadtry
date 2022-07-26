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

import com.github.harbby.gadtry.GadTry;
import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.collection.MutableList;
import com.github.harbby.gadtry.io.IOUtils;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public abstract class AbstractProxy
        implements ProxyFactory
{
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

    protected Collection<Method> findProxyMethods(Class<?> supperClass, Collection<Class<?>> interfaces)
    {
        Map<String, Method> methods = new LinkedHashMap<>();
        MutableList.Builder<Method> builder = MutableList.builder();
        builder.addAll(supperClass.getMethods());
        for (Class<?> ctClass : interfaces) {
            builder.addAll(ctClass.getMethods());
        }
        List<Method> methodList = builder.build();
        for (Method method : methodList) {
            //final or private or static function not proxy
            boolean checked = Modifier.isFinal(method.getModifiers()) ||
                    Modifier.isPrivate(method.getModifiers()) ||
                    Modifier.isStatic(method.getModifiers());
            if (!checked) {
                String key = String.format("%s%s", method.getName(), Type.getMethodDescriptor(method));
                methods.put(key, method);
            }
        }
        return methods.values();
    }

    @Override
    public <T> T newProxyInstance(ProxyRequest<T> request)
    {
        @SuppressWarnings("unchecked")
        Class<? extends T> aClass = (Class<? extends T>) getProxyClass(request);
        T proxyObj;
        proxyObj = Platform.allocateInstance(aClass);
        ((ProxyAccess) proxyObj).setHandler(request.getHandler());
        return proxyObj;
    }

    @Override
    public Class<?> getProxyClass(ClassLoader loader, Class<?>... drivers)
    {
        checkState(drivers != null && drivers.length > 0, "interfaces is Empty");
        ProxyRequest.Builder<?> request = ProxyRequest.builder(drivers[0])
                .setClassLoader(loader);
        if (drivers.length > 1) {
            request.addInterface(Arrays.copyOfRange(drivers, 1, drivers.length));
        }
        return getProxyClass(request.build());
    }

    @Override
    public boolean isProxyClass(Class<?> cl)
    {
        return ProxyAccess.class.isAssignableFrom(cl) && proxyCache.containsValue(cl);
    }

    @Override
    public Class<?> getProxyClass(ProxyRequest<?> request)
    {
        List<Class<?>> validClass = new ArrayList<>();
        validClass.add(request.getSuperclass());
        validClass.addAll(request.getInterfaces());
        KeyX keyX = new KeyX(validClass);
        return proxyCache.computeIfAbsent(keyX, k -> {
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

    @Override
    public InvocationHandler getInvocationHandler(Object proxy)
            throws IllegalArgumentException
    {
        checkState(isProxyClass(proxy.getClass()), proxy + " is not a Proxy obj");
        return ((ProxyAccess) proxy).getHandler();
    }

    private String createProxyClassName(ProxyRequest<?> request)
    {
        Class<?> superclass = request.getSuperclass();

        String beginName = superclass.getName();
        if (request.isJdkClass()) {
            beginName = GadTry.class.getPackage().getName() + "." + superclass.getSimpleName();
        }
        return beginName + "$GadtryAop" + number.getAndIncrement();
    }

    protected abstract byte[] generate(ClassLoader classLoader, String className, Class<?> superclass,
            Set<Class<?>> interfaceSet, Collection<Method> proxyMethods)
            throws Exception;

    private Class<?> createProxyClass(ProxyRequest<?> request)
            throws Exception
    {
        Class<?> superclass = request.getSuperclass();
        final Set<Class<?>> interfaceSet = new HashSet<>();
        for (Class<?> it : request.getInterfaces()) {
            if (it == ProxyAccess.class || it.isAssignableFrom(superclass)) {
                continue;
            }
            checkState(it.isInterface(), "must is interface. " + it);
            interfaceSet.add(it);
        }

        Collection<Method> proxyMethods = findProxyMethods(superclass, interfaceSet);
        String className = createProxyClassName(request);
        byte[] byteCode = this.generate(request.getClassLoader(), className, superclass, interfaceSet, proxyMethods);
        //this.toWrite(new File("./out", className.replace('.', '/') + ".class"), byteCode);
        int vmVersion = Platform.getJavaVersion();
        if (vmVersion < 9) {
            // java 8 or java7
            ClassLoader classLoader = request.getClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            return Platform.defineClass(byteCode, classLoader);
        }

        Class<?> buddyClass = request.isJdkClass() ? GadTry.class : superclass;
        if (vmVersion >= 15) {
            return Platform.defineHiddenClass(buddyClass, byteCode, false);
        }
        // 9 - 14
        return Platform.defineClass(buddyClass, byteCode);
    }

    private void toWrite(File file, byte[] byteCode)
    {
        file.getParentFile().mkdirs();
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            IOUtils.copyBytes(new ByteArrayInputStream(byteCode), outputStream, 4096);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
