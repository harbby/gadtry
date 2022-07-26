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

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Lazys
{
    private Lazys() {}

    public static <T> Supplier<T> of(Supplier<T> delegate)
    {
        return delegate instanceof LazySupplier ?
                delegate :
                new LazySupplier<>(requireNonNull(delegate));
    }

    public static <F1, R> Function<F1, R> of(Function<F1, R> lambda)
    {
        return lambda instanceof LazyFunction ?
                lambda :
                new LazyFunction<>(requireNonNull(lambda));
    }

    public static <R> Function<Supplier<R>, R> of()
    {
        return new LazySupplier2<>();
    }

    public static class LazySupplier2<T>
            implements Function<Supplier<T>, T>, Serializable
    {
        private static final long serialVersionUID = 7286040962786903681L;
        private transient volatile boolean initialized;
        private transient T value;

        private LazySupplier2() {}

        @Override
        public T apply(Supplier<T> tCreator)
        {
            if (!this.initialized) {
                synchronized (this) {
                    if (!this.initialized) {
                        T t = tCreator.get();
                        this.value = t;
                        // StoreStore
                        this.initialized = true;
                        return t;
                    }
                }
            }
            // LoadLoad
            return this.value;
        }

        @Override
        public String toString()
        {
            return "Lazys.of()";
        }
    }

    public static class LazyFunction<F1, R>
            implements Serializable, Function<F1, R>
    {
        private final Function<F1, R> delegate;
        private transient volatile boolean initialized;
        private transient R value;
        private static final long serialVersionUID = 3059649382149812183L;

        LazyFunction(Function<F1, R> delegate)
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
                        // StoreStore
                        this.initialized = true;
                        return t;
                    }
                }
            }
            // LoadLoad
            return this.value;
        }

        @Override
        public String toString()
        {
            return "Lazys.of(" + this.delegate + ")";
        }
    }

    public static class LazySupplier<T>
            implements Serializable, Supplier<T>
    {
        private final Supplier<T> delegate;
        private transient volatile boolean initialized;
        private transient T value;
        private static final long serialVersionUID = 7273112384936327520L;

        LazySupplier(Supplier<T> delegate)
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
                        // StoreStore
                        this.initialized = true;
                        return t;
                    }
                }
            }
            // LoadLoad
            return this.value;
        }

        @Override
        public String toString()
        {
            return "Lazys.of(" + this.delegate + ")";
        }
    }
}
