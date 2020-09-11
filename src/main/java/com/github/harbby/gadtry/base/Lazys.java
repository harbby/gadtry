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
import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.aop.mock.MockGoArgument;
import com.github.harbby.gadtry.function.Creator;
import com.github.harbby.gadtry.function.Function1;
import com.github.harbby.gadtry.memory.Platform;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

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

    public static <F1, R> Function1<F1, R> goLazy(Function1<F1, R> lambda)
    {
        AtomicReference<Object> atomicReference = new AtomicReference<>();
        Class<Function1<F1, R>> baseClass = JavaTypes.classTag(Function1.class);
        return AopGo.proxy(baseClass)
                .byInstance(lambda)
                .aop(binder -> {
                    binder.doAround(joinPoint -> {
                        if (atomicReference.get() == null) {
                            synchronized (atomicReference) {
                                if (atomicReference.get() != null) {
                                    return atomicReference.get();
                                }
                                Platform.getUnsafe().fullFence();
                                Object object = joinPoint.proceed();
                                checkState(atomicReference.compareAndSet(null, object), "not Single");
                                Platform.getUnsafe().fullFence();
                                return object;
                            }
                        }
                        else {
                            return atomicReference.get();
                        }
                    }).when().apply(MockGoArgument.any());
                }).build();
    }

    /**
     * 支持任意参数个数的Function1
     */
    private static <T> T functionGo(Class<T> interfaceClass, T lambda)
    {
        AtomicReference<Object> atomicReference = new AtomicReference<>();
        return AopFactory.proxy(interfaceClass)
                .byInstance(lambda)
                .around(proxyContext -> {
                    if (atomicReference.get() == null) {
                        synchronized (atomicReference) {
                            if (atomicReference.get() != null) {
                                return atomicReference.get();
                            }
                            Platform.getUnsafe().fullFence();
                            Object object = proxyContext.proceed();
                            checkState(atomicReference.compareAndSet(null, object), "not Single");
                            Platform.getUnsafe().fullFence();
                            return object;
                        }
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
            return "Lazys.goLazy(" + this.delegate + ")";
        }
    }
}
