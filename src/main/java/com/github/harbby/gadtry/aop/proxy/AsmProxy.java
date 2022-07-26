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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

public final class AsmProxy
        extends AbstractProxy
{
    static final AsmProxy asmProxy = new AsmProxy();

    private AsmProxy() {}

    protected byte[] generate(ClassLoader classLoader, String className, Class<?> superclass,
            Set<Class<?>> interfaceSet, Collection<Method> proxyMethods)
    {
        AsmProxyClassBuilder builder = new AsmProxyClassBuilder(
                className,
                superclass,
                interfaceSet,
                proxyMethods);
        return builder.generate();
    }
}
