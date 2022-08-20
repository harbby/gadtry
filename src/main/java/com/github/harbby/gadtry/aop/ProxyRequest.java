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
import com.github.harbby.gadtry.function.Function;

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
    private final Set<Class<?>> interfaces;
    private final Class<T> superclass;
    private final boolean isAccessClass;
    private Function<Class<? extends T>, T, Exception> function;

    public ProxyRequest(Class<T> superclass, Set<Class<?>> interfaces)
    {
        this.interfaces = interfaces;
        this.superclass = superclass;
        ClassLoader supperClassLoader = superclass.getClassLoader();
        if (supperClassLoader == null) {
            // java8 BootClassLoader is null
            this.isAccessClass = false;
            return;
        }
        else if (supperClassLoader == Platform.getBootstrapClassLoader()) {
            // java9+ BootClassLoader
            this.isAccessClass = false;
            return;
        }
        if (Platform.getJavaVersion() > 8 && !Platform.isOpen(superclass, Platform.class)) {
            this.isAccessClass = false;
            return;
        }
        this.isAccessClass = true;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public InvocationHandler getHandler()
    {
        return handler;
    }

    public Function<Class<? extends T>, T, Exception> getCreateFunction()
    {
        return function;
    }

    public Class<T> getSuperclass()
    {
        return superclass;
    }

    public Collection<Class<?>> getInterfaces()
    {
        return interfaces;
    }

    public boolean isAccessClass()
    {
        return isAccessClass;
    }

    public static <T> Builder<T> builder(Class<T> superclass)
    {
        return new Builder<>(superclass);
    }

    public static class Builder<T>
    {
        private final ProxyRequest<T> request;

        public Builder(Class<T> superclass)
        {
            requireNonNull(superclass, "superclass is null");
            this.request = new ProxyRequest<>(superclass, new HashSet<>());
        }

        public Builder<T> addInterface(Class<?> it)
        {
            requireNonNull(it, "superInterface is null");
            if (it != request.superclass && it != Serializable.class
                    && it != ProxyAccess.class
                    && !it.isAssignableFrom(request.superclass)) {
                checkState(it.isInterface(), it.getName() + " not is Interface");
                request.interfaces.add(it);
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

        public Builder<T> setNewInstance(Function<Class<? extends T>, T, Exception> function)
        {
            request.function = function;
            return this;
        }

        public ProxyRequest<T> build()
        {
            return request;
        }
    }
}
