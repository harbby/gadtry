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
package com.github.harbby.gadtry.base;

import com.github.harbby.gadtry.aop.AopFactory;
import com.github.harbby.gadtry.function.Creator;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class Lazys
{
    private Lazys() {}

    public static <T> Creator<T> memoize(Creator<T> delegate)
    {
        return delegate instanceof LazySupplier ?
                delegate :
                new LazySupplier<>(requireNonNull(delegate));
    }

    public static <T> Creator<T> goLazy(Creator<T> delegate)
    {
        return memoize(delegate);
    }

    @SuppressWarnings("unchecked")
    public static <T> T goLazy(T lambda)
    {
        checkState(lambda.getClass().getSuperclass() == Object.class);
        Class<?>[] interfaces = lambda.getClass().getInterfaces();
        List<Class<?>> interfaceList = Arrays.stream(interfaces)
                .filter(aClass -> Arrays.stream(aClass.getMethods()).anyMatch(method -> !method.isDefault()))
                .collect(Collectors.toList());
        checkState(interfaceList.size() > 0);

        Method[] methods = Arrays.stream(lambda.getClass().getDeclaredMethods()).filter(method -> {
            return Modifier.isPublic(method.getModifiers()) && !method.isDefault();
        }).toArray(Method[]::new);
        checkState(methods.length == 1, "must is lambda");

        AtomicReference<Object> atomicReference = new AtomicReference<>();
        return (T) AopFactory.proxy((Class<Object>) interfaceList.get(0))
                .byInstance(lambda)
                .whereMethod(methodInfo -> Modifier.isPublic(methodInfo.getModifiers()) && !methodInfo.isDefault())
                .around(proxyContext -> {
                    if (atomicReference.get() == null) {
                        Object object = proxyContext.proceed();
                        atomicReference.compareAndSet(null, object);
                        return object;
                    }
                    else {
                        return atomicReference.get();
                    }
                });
    }

    public static class LazySupplier<T>
            implements Serializable, Creator<T>
    {
        private final Creator<T> delegate;
        private transient volatile boolean initialized = false;
        private transient T value;
        private static final long serialVersionUID = 0L;

        LazySupplier(Creator<T> delegate)
        {
            this.delegate = delegate;
        }

        public T get()
        {
            if (!this.initialized) {
                synchronized (this) {
                    if (!this.initialized) {
                        T t = this.delegate.get();
                        this.value = t;
                        this.initialized = true;  //Atomic operation(原子操作)
                        return t;
                    }
                }
            }

            return this.value;
        }

        public String toString()
        {
            return "Lazys.memoize(" + this.delegate + ")";
        }
    }
}
