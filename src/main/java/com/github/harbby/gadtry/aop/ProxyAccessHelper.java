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

import com.github.harbby.gadtry.aop.aopgo.AroundHandler;
import com.github.harbby.gadtry.aop.mockgo.AopInvocationHandler;
import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.aop.proxy.ProxyAccess;
import com.github.harbby.gadtry.aop.proxy.ProxyFactory;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.collection.tuple.Tuple2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.github.harbby.gadtry.aop.MockInterceptor.LAST_MOCK_BY_WHEN_METHOD;
import static java.util.Objects.requireNonNull;

public class ProxyAccessHelper
        extends MockHelper
{
    @Override
    <T> T generatorProxy(Class<? extends T> superClas, boolean isSpy)
    {
        requireNonNull(superClas, "superClas is null");
        ClassLoader classLoader = superClas.getClassLoader();
        ProxyFactory factory = isSpy ? ProxyFactory.getAsmProxyV2() : ProxyFactory.getAsmProxy();
        Class<? extends T> proxyClass = factory.getProxyClass(classLoader, superClas);
        T mock;
        try {
            mock = Platform.allocateInstance(proxyClass);
        }
        catch (InstantiationException e) {
            throw new MockGoException("create mock class failed", e);
        }
        MockInvocationHandler interceptor = isSpy ? new MockInvocationHandler(mock) : new MockInvocationHandler();
        ((ProxyAccess) mock).setHandler(interceptor);
        return mock;
    }

    static class MockInvocationHandler
            extends AopInvocationHandler
    {
        public MockInvocationHandler(Object target)
        {
            super(target);
        }

        public MockInvocationHandler()
        {
            super();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
        {
            Object value = super.invoke(proxy, method, args);
            LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(proxy, method));
            return value;
        }
    }

    @Override
    void when(Object instance, AroundHandler answer)
    {
        AopInvocationHandler handler = getMockHandler(instance);
        handler.setHandler((proxy, method, args) -> {
            handler.initHandler();
            handler.register(method, answer);
            return JavaTypes.getClassInitValue(method.getReturnType());
        });
    }

    @Override
    void bind(Object instance, Method method, AroundHandler answer)
    {
        AopInvocationHandler handler = getMockHandler(instance);
        handler.register(method, answer);
    }

    private static AopInvocationHandler getMockHandler(Object obj)
    {
        if (obj instanceof ProxyAccess) {
            InvocationHandler interceptor = ((ProxyAccess) obj).getHandler();
            if (interceptor instanceof AopInvocationHandler) {
                return (AopInvocationHandler) interceptor;
            }
        }
        throw new MockGoException("obj don't is MockGo obj");
    }
}
