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

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class ProxyRequest<T>
{
    private ClassLoader classLoader;
    private String basePackage;
    private InvocationHandler handler;
    private Class<?>[] interfaces;
    private Object target;
    private final Class<T> superclass;
    private boolean disableSuperMethod = false;

    public ProxyRequest(Class<T> superclass)
    {
        this.superclass = superclass;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public String getBasePackage()
    {
        return basePackage;
    }

    public InvocationHandler getHandler()
    {
        return handler;
    }

    public Class<T> getSuperclass()
    {
        return superclass;
    }

    public Class<?>[] getInterfaces()
    {
        return interfaces;
    }

    public Object getTarget()
    {
        return target;
    }

    public boolean isDisableSuperMethod()
    {
        return disableSuperMethod;
    }

    public static <T> Builder<T> builder(Class<T> superclass)
    {
        return new Builder<>(superclass);
    }

    public static class Builder<T>
    {
        private final ProxyRequest<T> request;
        private final List<Class<?>> superInterfaces = new ArrayList<>();

        public Builder(Class<T> superclass)
        {
            this.request = new ProxyRequest<>(requireNonNull(superclass, "superclass is null"));
        }

        public Builder<T> addInterface(Class<?> superInterface)
        {
            requireNonNull(superInterface, "superInterface is null");
            if (superInterface != request.superclass) {
                checkState(superInterface.isInterface(), superInterface.getName() + " not is Interface");
                superInterfaces.add(superInterface);
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

        public Builder<T> disableSuperMethod()
        {
            request.disableSuperMethod = true;
            return this;
        }

        public Builder<T> setInvocationHandler(InvocationHandler handler)
        {
            request.handler = requireNonNull(handler, "handler is null");
            return this;
        }

        public Builder<T> setTarget(Object target)
        {
            request.target = target;
            return this;
        }

        public Builder<T> basePackage(String basePackage)
        {
            request.basePackage = basePackage;
            return this;
        }

        public Builder<T> setClassLoader(ClassLoader classLoader)
        {
            request.classLoader = classLoader;
            return this;
        }

        public ProxyRequest<T> build()
        {
            request.interfaces = superInterfaces.toArray(new Class[0]);
            return request;
        }
    }
}
