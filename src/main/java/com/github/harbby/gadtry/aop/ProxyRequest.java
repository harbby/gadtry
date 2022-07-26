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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.aop.proxy.ProxyAccess;
import com.github.harbby.gadtry.base.Platform;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class ProxyRequest<T>
{
    private ClassLoader classLoader;
    private InvocationHandler handler;
    private Set<Class<?>> interfaces;
    private final Class<T> superclass;
    private final boolean isJdkClass;

    public ProxyRequest(Class<T> superclass)
    {
        this.superclass = superclass;
        ClassLoader supperClassLoader = superclass.getClassLoader();
        if (supperClassLoader == null) {
            // java8 BootClassLoader is null
            this.isJdkClass = true;
            return;
        }
        else if (supperClassLoader == Platform.getBootstrapClassLoader()) {
            // java9+ BootClassLoader
            this.isJdkClass = true;
            return;
        }
        if (Platform.getJavaVersion() > 8 && !Platform.isOpen(superclass, Platform.class)) {
            this.isJdkClass = true;
            return;
        }
        this.isJdkClass = false;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public InvocationHandler getHandler()
    {
        return handler;
    }

    public Class<T> getSuperclass()
    {
        return superclass;
    }

    public Collection<Class<?>> getInterfaces()
    {
        return interfaces;
    }

    public boolean isJdkClass()
    {
        return isJdkClass;
    }

    public static <T> Builder<T> builder(Class<T> superclass)
    {
        return new Builder<>(superclass);
    }

    public static class Builder<T>
    {
        private final ProxyRequest<T> request;
        private final Set<Class<?>> superInterfaces = new HashSet<>();

        public Builder(Class<T> superclass)
        {
            this.request = new ProxyRequest<>(requireNonNull(superclass, "superclass is null"));
        }

        public Builder<T> addInterface(Class<?> it)
        {
            requireNonNull(it, "superInterface is null");
            if (it != request.getSuperclass() && it != Serializable.class
                    && it != ProxyAccess.class
                    && !it.isAssignableFrom(request.getSuperclass())) {
                checkState(it.isInterface(), it.getName() + " not is Interface");
                superInterfaces.add(it);
            }
            return this;
        }

        public Builder<T> addInterface(Class<?>[] superInterfaces)
        {
            requireNonNull(superInterfaces, "superInterfaces is null");
            for (Class<?> c : superInterfaces) {
                addInterface(c);
            }
            return this;
        }

        public Builder<T> setInvocationHandler(InvocationHandler handler)
        {
            request.handler = requireNonNull(handler, "handler is null");
            return this;
        }

        public Builder<T> setClassLoader(ClassLoader classLoader)
        {
            request.classLoader = classLoader;
            return this;
        }

        public ProxyRequest<T> build()
        {
            request.interfaces = superInterfaces;
            return request;
        }
    }
}
