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

import javassist.bytecode.DuplicateMemberException;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ProxyTest
{
    @Test(expected = DuplicateMemberException.class)
    public void duplicateInterfaceUseJavassistproxy()
    {
        Set<String> set = new HashSet<>();
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            return method.invoke(set, args);
        };
        Proxy.builder(HashSet.class)
                .setInvocationHandler(invocationHandler)
                .addInterface(Supplier.class)
                .addInterface(Supplier.class)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateClassUseJavassistproxy()
    {
        Set<String> set = new HashSet<>();
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            return method.invoke(set, args);
        };
        Proxy.builder(HashSet.class)
                .setInvocationHandler(invocationHandler)
                .addInterface(ArrayList.class)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateInterfaceUseJDKproxy()
    {
        Set<String> set = new HashSet<>();
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            return method.invoke(set, args);
        };
        Proxy.builder(Set.class)
                .setInvocationHandler(invocationHandler)
                .addInterface(Supplier.class)
                .addInterface(Supplier.class)
                .build();
    }
}
