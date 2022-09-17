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
import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.aop.proxy2.AsmProxyV3;
import com.github.harbby.gadtry.aop.proxy2.Interceptor;
import com.github.harbby.gadtry.aop.proxy2.MockAccess;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Platform;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

class MockAccessHelper
        extends MockHelper
{
    @Override
    <T> T generatorProxy(Class<? extends T> superClas, boolean isSpy)
    {
        requireNonNull(superClas, "superClas is null");
        ClassLoader classLoader = superClas.getClassLoader();
        Class<? extends T> proxyClass = AsmProxyV3.asmProxyV3.getProxyClass(classLoader, superClas);
        T mock;
        try {
            mock = Platform.allocateInstance(proxyClass);
        }
        catch (InstantiationException e) {
            throw new MockGoException("create mock class failed", e);
        }
        MockInterceptor interceptor = new MockInterceptor(isSpy);
        ((MockAccess) mock).setHandler(interceptor);
        return mock;
    }

    @Override
    void when(Object instance, AroundHandler answer)
    {
        MockInterceptor mockInterceptor = getMockInterceptor(instance);
        mockInterceptor.setHandler((proxy, method, caller) -> {
            mockInterceptor.initHandler();
            mockInterceptor.register(method, answer);
            return JavaTypes.getClassInitValue(method.getReturnType());
        });
    }

    @Override
    void bind(Object instance, Method method, AroundHandler answer)
    {
        MockInterceptor handler = getMockInterceptor(instance);
        handler.register(method, answer);
    }

    private static MockInterceptor getMockInterceptor(Object obj)
    {
        if (obj instanceof MockAccess) {
            Interceptor interceptor = ((MockAccess) obj).getHandler();
            if (interceptor instanceof MockInterceptor) {
                return (MockInterceptor) interceptor;
            }
        }
        throw new MockGoException("obj don't is MockGo obj");
    }
}
