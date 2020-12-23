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

import com.github.harbby.gadtry.function.exception.Runnable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

/**
 * copy code with com.google.common.base.Throwables
 */
public class Throwables
{
    private Throwables() {}

    /**
     * Returns a string containing the result of {@link Throwable#toString() toString()}, followed by
     * the full, recursive stack trace of {@code throwable}. Note that you probably should not be
     * parsing the resulting string; if you need programmatic access to the stack frames, you can call
     * {@link Throwable#getStackTrace()}.
     *
     * @param throwable throwable
     * @return throwable to string
     */
    public static String getStackTraceAsString(Throwable throwable)
    {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * Returns the innermost cause of {@code throwable}. The first throwable in a chain provides
     * context from when the error or exception was initially detected. Example usage:
     *
     * <pre>
     * assertEquals("Unable to assign a customer id", Throwables.getRootCause(e).getMessage());
     * </pre>
     *
     * @param throwable throwable
     * @return Throwable return Get the most source exception
     * @throws IllegalArgumentException if there is a loop in the causal chain
     */
    public static Throwable getRootCause(Throwable throwable)
    {
        // Keep a second pointer that slowly walks the causal chain. If the fast pointer ever catches
        // the slower pointer, then there's a loop.
        Throwable slowPointer = throwable;
        boolean advanceSlowPointer = false;

        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;

            if (throwable == slowPointer) {
                throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
            }
            if (advanceSlowPointer) {
                slowPointer = slowPointer.getCause();
            }
            advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
        }
        return throwable;
    }

    public static void noCatch(Runnable<Exception> runnable)
    {
        try {
            runnable.apply();
        }
        catch (Exception e) {
            throwsThrowable(e);
        }
    }

    public static <T> T noCatch(Callable<T> callable)
    {
        try {
            return callable.call();
        }
        catch (Exception e) {
            throw throwsThrowable(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException throwsThrowable(Throwable e)
            throws T
    {
        throw (T) e;
    }

    public static <T extends Throwable> void throwsThrowable(Class<T> clazz)
            throws T
    {}
}
