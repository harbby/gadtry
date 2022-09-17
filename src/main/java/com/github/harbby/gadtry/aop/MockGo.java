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
import com.github.harbby.gadtry.aop.mockgo.MockGoAnnotations;
import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.base.MoreObjects;
import com.github.harbby.gadtry.collection.tuple.Tuple2;

import java.lang.reflect.Method;

import static com.github.harbby.gadtry.aop.MockInterceptor.LAST_MOCK_BY_WHEN_METHOD;
import static java.util.Objects.requireNonNull;

/**
 * MockGo
 */
public class MockGo
{
    private static final MockHelper mockHelper = new MockAccessHelper();

    private MockGo() {}

    public static <T> T spy(T obj)
    {
        requireNonNull(obj, "obj is null");
        @SuppressWarnings("unchecked")
        Class<T> superclass = (Class<T>) obj.getClass();
        T proxyObj = spy(superclass);
        MoreObjects.copyWriteObjectState(superclass, obj, proxyObj);
        return proxyObj;
    }

    public static <T> T spy(Class<? extends T> spyClass)
    {
        requireNonNull(spyClass, "spyClass is null");
        return mockHelper.generatorProxy(spyClass, true);
    }

    public static <T> T mock(Class<T> superclass)
    {
        T mock = mockHelper.generatorProxy(superclass, false);
        when(mock.toString()).thenReturn(superclass.getSimpleName());
        return mock;
    }

    public static void initMocks(Object testObject)
    {
        MockGoAnnotations.initMocks(testObject);
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

    public static DoBuilder doAnswer(AroundHandler function)
    {
        return doAround(function);
    }

    public static DoBuilder doAround(AroundHandler function)
    {
        return new DoBuilder(function);
    }

    public static class DoBuilder
    {
        private final AroundHandler answer;

        public DoBuilder(AroundHandler function)
        {
            this.answer = function;
        }

        public <T> T when(T instance)
        {
            mockHelper.when(instance, answer);
            return instance;
        }
    }

    public static <T> WhenThenBuilder<T> when(T methodCallValue)
    {
        return new WhenThenBuilder<>();
    }

    public static DoBuilder doThrow(Throwable e)
    {
        return new DoBuilder(f -> {
            throw e;
        });
    }

    public static class WhenThenBuilder<T>
    {
        private final Tuple2<Object, Method> lastWhenMethod;

        public WhenThenBuilder()
        {
            this.lastWhenMethod = LAST_MOCK_BY_WHEN_METHOD.get();
            LAST_MOCK_BY_WHEN_METHOD.set(null);
            if (lastWhenMethod == null) {
                throw new MockGoException("when(...) does not select any Method\n" +
                        "Example of: \n" +
                        " when(someMethod(any...)).then...()");
            }
        }

        public void thenReturn(T value)
        {
            bind(p -> value);
        }

        public void thenAround(AroundHandler answer)
        {
            bind(answer);
        }

        public void thenThrow(Throwable e)
        {
            bind(f -> {
                throw e;
            });
        }

        private void bind(AroundHandler answer)
        {
            mockHelper.bind(lastWhenMethod.f1(), lastWhenMethod.f2(), answer);
        }
    }
}
