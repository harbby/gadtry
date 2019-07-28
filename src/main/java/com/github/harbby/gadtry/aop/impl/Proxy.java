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

import com.github.harbby.gadtry.base.Arrays;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public interface Proxy
{
    <T> T getProxy(ClassLoader loader, InvocationHandler handler, Class<?>... interfaces);

    public static ProxyBuilder builder(Class<?> superclass)
    {
        return new ProxyBuilder(superclass);
    }

    public static class ProxyBuilder
    {
        private final Class<?> superclass;
        private final List<Class<?>> interfacesList = new ArrayList<>();
        private InvocationHandler handler;
        private ClassLoader classLoader;
        private Object target;

        public ProxyBuilder(Class<?> superclass)
        {
            this.superclass = requireNonNull(superclass, "superclass is null");
        }

        public ProxyBuilder addInterface(Class<?> superInterface)
        {
            requireNonNull(superInterface, "superInterface is null");
            checkState(superInterface.isInterface(), superInterface.getName() + " not is Interface");
            interfacesList.add(superInterface);
            return this;
        }

        public ProxyBuilder setInvocationHandler(InvocationHandler handler)
        {
            this.handler = handler;
            return this;
        }

        public ProxyBuilder setTarget(Object target)
        {
            this.target = target;
            return this;
        }

        public ProxyBuilder setClassLoader(ClassLoader classLoader)
        {
            this.classLoader = classLoader;
            return this;
        }

        public <T> T build()
        {
            checkState(handler != null, "InvocationHandler is null");
            Class<?>[] interfaces = Arrays.asArray(superclass, interfacesList);
            if (superclass.isInterface()) {
                return JdkProxy.newProxyInstance(classLoader, handler, interfaces);
            }
            else {
                checkState(!Modifier.isFinal(superclass.getModifiers()), superclass + " is final");
                if (target != null) {
                    return JavassistProxy.newProxyInstance(classLoader, target, handler, interfaces);
                }
                else {
                    return JavassistProxy.newProxyInstance(classLoader, handler, interfaces);
                }
            }
        }
    }
}
