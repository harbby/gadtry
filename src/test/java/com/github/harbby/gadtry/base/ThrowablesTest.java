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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import static com.github.harbby.gadtry.base.Try.noCatch;

public class ThrowablesTest
{
    @Test
    public void testNoCatch()
    {
        noCatch(() -> {});
        Assert.assertEquals(noCatch(() -> "done"), "done");
        try {
            noCatch(() -> {
                if (true) {
                    throw new MalformedURLException();
                }
            });
            Assert.fail();
            Throwables.throwThrowable(MalformedURLException.class);
        }
        catch (MalformedURLException ignored) {
        }

        try {
            noCatch(() -> {
                throw new MalformedURLException();
            });
            Assert.fail();
            Throwables.throwThrowable(MalformedURLException.class);
        }
        catch (MalformedURLException ignored) {
        }
    }

    @Test
    public void testThrowsException1()
    {
        try {
            Throwables.throwThrowable(new IOException("IO_test"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
            Assert.assertEquals("IO_test", e.getMessage());
        }
    }

    @Test
    public void testThrowValueException1()
    {
        try {
            Throwables.throwValueThrowable(new IOException("IO_test"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
            Assert.assertEquals("IO_test", e.getMessage());
        }
    }

    @Test
    public void testThrowsException2()
    {
        try {
            Throwables.throwThrowable(new IOException("IO_test"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
            Assert.assertEquals("IO_test", e.getMessage());
        }
    }

    @Test
    public void testThrowsException()
    {
        try {
            try {
                URL url = new URL("/harbby");
                Assert.fail();
            }
            catch (IOException e) {
                Throwables.throwThrowable(e);
            }
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testThrowsExceptionClass()
            throws IOException
    {
        Throwables.throwThrowable(IOException.class);
    }

    @Test
    public void getRootCauseTest()
    {
        Throwable error = new ClassCastException("cast error");
        error = new IOException(error);
        error = new SQLException(error);
        Throwable rootCause = Throwables.getRootCause(error);
        Assert.assertTrue(rootCause instanceof ClassCastException);
    }

    @Test
    public void getRootCauseLoopErrorTest()
            throws NoSuchFieldException, IllegalAccessException
    {
        Throwable error1 = new ClassCastException("cast error");
        Throwable error2 = new IOException(error1);

        Field field = Throwable.class.getDeclaredField("cause");
        field.setAccessible(true);
        field.set(error1, error2);
        try {
            Throwables.getRootCause(error2);
        }
        catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Loop in causal chain detected.");
        }
    }

    @Test
    public void getStackTraceAsStringTest()
    {
        Throwable error = new ClassCastException("cast error");
        error = new IOException(error);
        error = new SQLException(error);
        String msg = Throwables.getStackTraceAsString(error);
        Assert.assertTrue(msg.contains("cast error"));
    }
}
