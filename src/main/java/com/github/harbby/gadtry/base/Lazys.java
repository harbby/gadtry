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

import com.github.harbby.gadtry.function.Creator;
import com.github.harbby.gadtry.function.Function1;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class Lazys
{
    private Lazys() {}

    private static <T> Creator<T> memoize(Creator<T> delegate)
    {
        return delegate instanceof LazySupplier ?
                delegate :
                new LazySupplier<>(requireNonNull(delegate));
    }

    public static <T> Creator<T> of(Creator<T> delegate)
    {
        return memoize(delegate);
    }

    public static <T> Creator<T> goLazy(Creator<T> delegate)
    {
        return memoize(delegate);
    }

    public static <F1, R> Function1<F1, R> goLazy(Function1<F1, R> lambda)
    {
        return lambda instanceof LazyFunction1 ?
                lambda :
                new LazyFunction1<>(requireNonNull(lambda));
    }

    public static class LazyFunction1<F1, R>
            implements Serializable, Function1<F1, R>
    {
        private final Function1<F1, R> delegate;
        private transient volatile boolean initialized;
        private transient R value;
        private static final long serialVersionUID = 0L;

        LazyFunction1(Function1<F1, R> delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public R apply(F1 f1)
        {
            if (!this.initialized) {
                synchronized (this) {
                    if (!this.initialized) {
                        R t = this.delegate.apply(f1);
                        this.value = t;
                        this.initialized = true;  //Atomic operation(原子操作)
                        return t;
                    }
                }
            }

            return this.value;
        }

        @Override
        public String toString()
        {
            return "Lazys.goLazy(" + this.delegate + ")";
        }
    }

    public static class LazySupplier<T>
            implements Serializable, Creator<T>
    {
        private final Creator<T> delegate;
        private transient volatile boolean initialized;
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

        @Override
        public String toString()
        {
            return "Lazys.goLazy(" + this.delegate + ")";
        }
    }
}
