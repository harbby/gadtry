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
package com.github.harbby.gadtry.aop.mock;

import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.aop.impl.JavassistProxy;
import com.github.harbby.gadtry.aop.impl.JdkProxy;
import com.github.harbby.gadtry.aop.impl.Proxy;
import com.github.harbby.gadtry.aop.impl.ProxyHandler;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.exception.Function;
import com.github.harbby.gadtry.memory.UnsafeHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Throwables.throwsException;

/**
 * MockGo
 */
public class MockGo
{
    static final ThreadLocal<Tuple2<Object, Method>> LAST_MOCK_BY_WHEN_METHOD = new ThreadLocal<>();

    private MockGo() {}

    public static <T> T spy(T instance)
    {
        Class<T> tClass = (Class<T>) instance.getClass();
        return JavassistProxy.newProxyInstance(tClass.getClassLoader(), new MockInvocationHandler(instance), tClass);
    }

    public static <T> T spy(Class<T> superclass)
    {
        try {
            T instance = UnsafeHelper.allocateInstance2(superclass);
            return mock(superclass, new MockInvocationHandler(instance));
        }
        catch (Exception e) {
            throw throwsException(e);
        }
    }

    public static <T> T spy(Class<T> superclass, T instance)
    {
        return mock(superclass, new MockInvocationHandler(instance));
    }

    public static <T> T mock(Class<T> superclass)
    {
        return mock(superclass, new MockInvocationHandler());
    }

    private static <T> T mock(Class<T> superclass, MockInvocationHandler invocationHandler)
    {
        ClassLoader loader = superclass.getClassLoader() == null ? ProxyHandler.class.getClassLoader() :
                superclass.getClassLoader();

        ProxyHandler proxy = Proxy.builder(superclass)
                .addInterface(ProxyHandler.class)
                .setClassLoader(loader)
                .setInvocationHandler(invocationHandler)
                .build();
        // mock method getHandler()
        // 等价于: toReturn(invocationHandler).when(proxy).getHandler() 但此处并不能这么写
        if (JdkProxy.isProxyClass(proxy.getClass())) {
            invocationHandler.setDoNext(p -> invocationHandler);
            proxy.getHandler(); //must
        }
        //---------------------------
        return (T) proxy;
    }

    public static void initMocks(Object testObject)
    {
        MockAnnotations.initMocks(testObject);
    }

    public static DoBuilder doReturn(Object value)
    {
        return new DoBuilder(f -> value);
    }

    public static DoBuilder doNothing()
    {
        return doAround(f -> {
            if (f.getMethod().getReturnType() != void.class) {
                throw new MockGoException("Only void methods can doNothing()!\n" +
                        "Example of correct use of doNothing():\n" +
                        "    doNothing().\n" +
                        "    .when(mock).someVoidMethod();");
            }
            return null;
        });
    }

    public static DoBuilder doAnswer(Function<ProxyContext, Object, Throwable> function)
    {
        return doAround(function);
    }

    public static DoBuilder doAround(Function<ProxyContext, Object, Throwable> function)
    {
        return new DoBuilder(function);
    }

    public static class DoBuilder
    {
        private final Function<ProxyContext, Object, Throwable> function;

        public DoBuilder(Function<ProxyContext, Object, Throwable> function)
        {
            this.function = function;
        }

        public <T> T when(T instance)
        {
            MockInvocationHandler handler = getMockInvocationHandler(instance);
            handler.setDoNext(function);
            return instance;
        }
    }

    public static <T> WhenThenBuilder<T> when(T methodCallValue)
    {
        return new WhenThenBuilder<>();
    }

    public static DoBuilder doThrow(Exception e)
    {
        return new DoBuilder(f -> { throw e; });
    }

    public static class WhenThenBuilder<T>
    {
        private final Tuple2<Object, Method> lastWhenMethod;

        public WhenThenBuilder()
        {
            this.lastWhenMethod = LAST_MOCK_BY_WHEN_METHOD.get();
            LAST_MOCK_BY_WHEN_METHOD.remove();
            checkState(lastWhenMethod != null, "whenMethod is null");
        }

        public void thenReturn(T value)
        {
            bind(p -> value);
        }

        public void thenAround(Function<ProxyContext, Object, Throwable> function)
        {
            bind(function);
        }

        public void thenThrow(Exception e)
        {
            bind(f -> { throw e; });
        }

        private void bind(Function<ProxyContext, Object, Throwable> function)
        {
            MockInvocationHandler handler = getMockInvocationHandler(lastWhenMethod.f1());
            handler.register(lastWhenMethod.f2(), function);
        }
    }

    private static MockInvocationHandler getMockInvocationHandler(Object instance)
    {
        ProxyHandler proxy = (ProxyHandler) instance;
        InvocationHandler handler = proxy.getHandler();
        checkState(handler instanceof MockInvocationHandler, "instance not mock proxy");
        return (MockInvocationHandler) handler;
    }
}
