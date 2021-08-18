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
package com.github.harbby.gadtry.aop.codegen;

import com.github.harbby.gadtry.aop.MethodSignature;
import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.mockgo.AopInvocationHandler;
import com.github.harbby.gadtry.base.Arrays;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.base.Throwables;
import com.github.harbby.gadtry.function.Function1;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class Proxy
{
    private Proxy() {}

    public static <T> T proxy(ProxyRequest<T> request)
    {
        Class<?> superclass = request.getSuperclass();

        Class<?>[] interfaces = Arrays.asArray(superclass, request.getInterfaces(), Class.class);
        if (superclass.isInterface() && Platform.getClassVersion() < 60) {
            return JdkProxy.newProxyInstance(request.getClassLoader(), request.getHandler(), interfaces);
        }
        else {
            checkState(!Modifier.isFinal(superclass.getModifiers()), superclass + " is final");
            return JavassistProxy.newProxyInstance(request);
        }
    }

    public static AopInvocationHandler getInvocationHandler(Object instance)
    {
        if (JdkProxy.isProxyClass(instance.getClass())) {
            return (AopInvocationHandler) JdkProxy.getInvocationHandler(instance);
        }

        ProxyHandler proxy = (ProxyHandler) instance;
        InvocationHandler handler = proxy.getHandler();
        checkState(handler instanceof AopInvocationHandler, "instance not mock proxy");
        return (AopInvocationHandler) handler;
    }

    public static List<Method> filter(Class<?> proxyClass, List<Function1<MethodSignature, Boolean>> filters)
    {
        List<Method> methods = new ArrayList<>();
        for (Field field : proxyClass.getDeclaredFields()) {
            if (field.getType() == Method.class && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                MethodSignature method = null;
                try {
                    method = MethodSignature.ofByGadtryAop((Method) field.get(null));
                }
                catch (IllegalAccessException e) {
                    throw Throwables.throwsThrowable(e);
                }
                boolean access = true;
                for (Function1<MethodSignature, Boolean> filter : filters) {
                    if (!filter.apply(method)) {
                        access = false;
                        break;
                    }
                }
                if (access) {
                    methods.add(method.getMethod());
                }
            }
        }
        return methods;
    }
}
