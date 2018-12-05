/*
 * Copyright (C) 2018 The Harbby Authors
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

public interface Proxy
{
    <T> T getProxy(ClassLoader loader, Class<T> interfaces, InvocationHandler handler);

    public static interface ProxyHandler
    {
        /**
         * Sets a handler.  It can be used for changing handlers
         * during runtime.
         */
        void setHandler(InvocationHandler handler);

        InvocationHandler getHandler();
    }
}
