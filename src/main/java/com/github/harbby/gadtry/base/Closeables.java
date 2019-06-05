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

import com.github.harbby.gadtry.function.exception.Consumer;

import java.io.Serializable;
import java.util.NoSuchElementException;

import static com.github.harbby.gadtry.base.Throwables.throwsException;

public interface Closeables<T>
        extends AutoCloseable, Serializable
{
    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code Exception}.
     */
    public void close();

    default T get()
    {
        throw new NoSuchElementException();
    }

    public static Closeables openThreadContextClassLoader(ClassLoader newThreadContextClassLoader)
    {
        final ClassLoader originalThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(newThreadContextClassLoader);
        return () -> Thread.currentThread().setContextClassLoader(originalThreadContextClassLoader);
    }

    public static <T> Closeables<T> autoClose(T instance, Consumer<T> runnable)
    {
        return new Closeables<T>()
        {
            @Override
            public void close()
            {
                if (instance != null) {
                    try {
                        runnable.apply(instance);
                    }
                    catch (Exception e) {
                        throwsException(e);
                    }
                }
            }

            @Override
            public T get()
            {
                return instance;
            }
        };
    }

    public static Closeables autoClose(AutoCloseable runnable)
    {
        return () -> {
            try {
                runnable.close();
            }
            catch (Exception e) {
                throwsException(e);
            }
        };
    }
}
