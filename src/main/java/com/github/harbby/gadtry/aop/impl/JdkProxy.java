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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkProxy
{
    private JdkProxy() {}

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader loader, InvocationHandler handler, Class<?>... driver)
            throws IllegalArgumentException
    {
        return (T) Proxy.newProxyInstance(loader, driver, handler);
    }

    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... driver)
    {
        return Proxy.getProxyClass(loader, driver);
    }

    public static boolean isProxyClass(Class<?> cl)
    {
        return Proxy.isProxyClass(cl);
    }

    public static InvocationHandler getInvocationHandler(Object proxy)
    {
        return Proxy.getInvocationHandler(proxy);
    }
}
