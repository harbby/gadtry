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
package com.github.harbby.gadtry.aop.aopgo;

import com.github.harbby.gadtry.aop.impl.JdkProxy;
import com.github.harbby.gadtry.aop.impl.Proxy;
import com.github.harbby.gadtry.aop.impl.ProxyHandler;
import com.github.harbby.gadtry.aop.mock.MockInvocationHandler;

public class WhenMethod<T>
{
    private final Class<T> superclass;
    private final T instance;

    private java.util.function.Consumer<MockBinder<T>>[] binders;

    public WhenMethod(Class<T> superclass, T instance)
    {
        this.superclass = superclass;
        this.instance = instance;
    }

    @SafeVarargs
    public final WhenMethod<T> aop(java.util.function.Consumer<MockBinder<T>>... binders)
    {
        this.binders = binders;
        return this;
    }

    public T build()
    {
        ClassLoader loader = superclass.getClassLoader() == null ? ProxyHandler.class.getClassLoader() :
                superclass.getClassLoader();
        MockInvocationHandler mockInvocationHandler = new MockInvocationHandler(instance);
        T proxy = Proxy.builder(superclass)
                .addInterface(ProxyHandler.class)
                .setInvocationHandler(mockInvocationHandler)
                .setClassLoader(loader)
                .build();
        // mock method getHandler()
        // 等价于: toReturn(invocationHandler).when(proxy).getHandler() 但此处并不能这么写
        if (JdkProxy.isProxyClass(proxy.getClass())) {
            mockInvocationHandler.setDoNext(p -> mockInvocationHandler);
            ((ProxyHandler) proxy).getHandler(); //must
        }
        //---------------------------
        MockBinder<T> mockBinder = new MockBinder<>(proxy, mockInvocationHandler);
        for (java.util.function.Consumer<MockBinder<T>> it : binders) {
            it.accept(mockBinder);
        }
        mockBinder.flush();
        return proxy;
    }
}
