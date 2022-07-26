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

public interface ProxyFactory
{
    <T> T newProxyInstance(ClassLoader loader, InvocationHandler handler, Class<?>... driver);

    <T> T newProxyInstance(ProxyRequest<T> request);

    Class<?> getProxyClass(ClassLoader loader, Class<?>... driver);

    boolean isProxyClass(Class<?> cl);

    Class<?> getProxyClass(ProxyRequest<?> request);

    InvocationHandler getInvocationHandler(Object proxy);

    static ProxyFactory getJdkProxy()
    {
        return JdkProxy.jdkProxy;
    }

    static ProxyFactory getJavassistProxy()
    {
        return JavassistProxy.javassistProxy;
    }

    static AsmProxy getAsmProxy()
    {
        return AsmProxy.asmProxy;
    }

    static ProxyFactory getJavassistProxyV2()
    {
        return JavassistProxyV2.javassistProxyV2;
    }
}
