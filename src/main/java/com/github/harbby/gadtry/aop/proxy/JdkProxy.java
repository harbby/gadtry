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

import com.github.harbby.gadtry.aop.ProxyRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;

public final class JdkProxy
        implements ProxyFactory
{
    static final JdkProxy jdkProxy = new JdkProxy();

    private JdkProxy() {}

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newProxyInstance(ClassLoader loader, InvocationHandler handler, Class<?>... driver)
            throws IllegalArgumentException
    {
        return (T) Proxy.newProxyInstance(loader, driver, handler);
    }

    @Override
    public Class<?> getProxyClass(ClassLoader loader, Class<?>... driver)
    {
        return Proxy.getProxyClass(loader, driver);
    }

    @Override
    public boolean isProxyClass(Class<?> cl)
    {
        return Proxy.isProxyClass(cl);
    }

    @Override
    public InvocationHandler getInvocationHandler(Object proxy)
    {
        return Proxy.getInvocationHandler(proxy);
    }

    @Override
    public <T> T newProxyInstance(ProxyRequest<T> request)
    {
        Collection<Class<?>> collection = request.getInterfaces();
        Class<?>[] array = new Class[collection.size() + 1];
        array[0] = request.getSuperclass();
        int i = 1;
        for (Class<?> aClass : collection) {
            array[i++] = aClass;
        }
        return newProxyInstance(request.getClassLoader(), request.getHandler(), array);
    }

    @Override
    public Class<?> getProxyClass(ProxyRequest<?> request)
    {
        Collection<Class<?>> collection = request.getInterfaces();
        Class<?>[] array = new Class[collection.size() + 1];
        array[0] = request.getSuperclass();
        int i = 1;
        for (Class<?> aClass : collection) {
            array[i++] = aClass;
        }
        return getProxyClass(request.getClassLoader(), array);
    }
}
