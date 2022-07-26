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

import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.AutoClose;
import com.github.harbby.gadtry.function.Runnable;
import com.github.harbby.gadtry.function.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Try
{
    private Try() {}

    public static OfTryCatcher of(Runnable<Throwable> runnable)
    {
        return new OfTryCatcher(runnable);
    }

    public static <T> ValueOfTryCatcher<T> valueOf(Supplier<T, Throwable> runnable)
    {
        return new ValueOfTryCatcher<>(runnable);
    }

    public static void noCatch(Runnable<Exception> runnable)
    {
        try {
            runnable.apply();
        }
        catch (Exception e) {
            Throwables.throwThrowable(e);
        }
    }

    public static <T> T noCatch(Callable<T> callable)
    {
        try {
            return callable.call();
        }
        catch (Exception e) {
            throw Throwables.throwThrowable(e);
        }
    }

    public static AutoClose openThreadContextClassLoader(ClassLoader newThreadContextClassLoader)
    {
        final ClassLoader originalThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(newThreadContextClassLoader);
        return () -> Thread.currentThread().setContextClassLoader(originalThreadContextClassLoader);
    }

    public static class OfTryCatcher
    {
        private final List<Tuple2<Class<? extends Throwable>, Consumer<? extends Throwable>>> matchErrors = new ArrayList<>();
        private final Runnable<Throwable> runnable;
        private java.lang.Runnable onSuccess;
        private Consumer<Throwable> onError = Throwables::throwThrowable;
        private java.lang.Runnable onFinally;

        public OfTryCatcher(Runnable<Throwable> runnable)
        {
            this.runnable = requireNonNull(runnable, "don't Try.of(null)");
        }

        public OfTryCatcher onSuccess(java.lang.Runnable onSuccess)
        {
            this.onSuccess = requireNonNull(onSuccess, "onSuccess is null");
            return this;
        }

        public OfTryCatcher onFinally(java.lang.Runnable onFinally)
        {
            this.onFinally = requireNonNull(onFinally, "onFinally is null");
            return this;
        }

        public OfTryCatcher onFailure(Consumer<Throwable> onError)
        {
            this.onError = requireNonNull(onError, "onError is null");
            return this;
        }

        public <E extends Throwable> OfTryCatcher matchException(Class<E> exceptionClass, Consumer<E> caseHandler)
        {
            requireNonNull(onError, "exceptionClass is null");
            requireNonNull(onError, "caseHandler is null");
            matchErrors.add(Tuple2.of(exceptionClass, caseHandler));
            return this;
        }

        public void doTry()
        {
            try {
                runnable.apply();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
            catch (Throwable e) {
                for (Tuple2<Class<? extends Throwable>, Consumer<? extends Throwable>> tuple2 : matchErrors) {
                    if (tuple2.key().isInstance(e)) {
                        @SuppressWarnings("unchecked")
                        Consumer<Throwable> handler = (Consumer<Throwable>) tuple2.value();
                        handler.accept(e);
                        return;
                    }
                }
                onError.accept(e);
            }
            finally {
                if (onFinally != null) {
                    onFinally.run();
                }
            }
        }
    }

    public static class ValueOfTryCatcher<T>
    {
        private final List<Tuple2<Class<? extends Throwable>, Function<? extends Throwable, T>>> matchErrors = new ArrayList<>();
        private final Supplier<T, java.lang.Throwable> runnable;
        private Consumer<T> onSuccess;
        private java.lang.Runnable onFinally;
        private Function<Throwable, T> onError = e -> {
            throw Throwables.throwThrowable(e);
        };

        public ValueOfTryCatcher(Supplier<T, Throwable> runnable)
        {
            this.runnable = requireNonNull(runnable, "don't Try.of(null)");
        }

        public ValueOfTryCatcher<T> onSuccess(Consumer<T> onSuccess)
        {
            this.onSuccess = requireNonNull(onSuccess, "onSuccess is null");
            return this;
        }

        public ValueOfTryCatcher<T> onFinally(java.lang.Runnable onFinally)
        {
            this.onFinally = requireNonNull(onFinally, "onFinally is null");
            return this;
        }

        public ValueOfTryCatcher<T> onFailure(Function<Throwable, T> onError)
        {
            this.onError = requireNonNull(onError, "onError is null");
            return this;
        }

        public <E extends Throwable> ValueOfTryCatcher<T> matchException(Class<E> exceptionClass, Function<E, T> caseHandler)
        {
            requireNonNull(onError, "exceptionClass is null");
            requireNonNull(onError, "caseHandler is null");
            matchErrors.add(Tuple2.of(exceptionClass, caseHandler));
            return this;
        }

        public T doTry()
        {
            try {
                T value = runnable.apply();
                if (onSuccess != null) {
                    onSuccess.accept(value);
                }
                return value;
            }
            catch (Throwable e) {
                for (Tuple2<Class<? extends Throwable>, Function<? extends Throwable, T>> tuple2 : matchErrors) {
                    if (tuple2.key().isInstance(e)) {
                        @SuppressWarnings("unchecked")
                        Function<Throwable, T> handler = (Function<Throwable, T>) tuple2.value();
                        return handler.apply(e);
                    }
                }
                return onError.apply(e);
            }
            finally {
                if (onFinally != null) {
                    onFinally.run();
                }
            }
        }
    }
}
