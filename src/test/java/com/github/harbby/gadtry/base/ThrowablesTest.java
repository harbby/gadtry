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
import java.net.URL;
import java.sql.SQLException;

import static com.github.harbby.gadtry.base.Throwables.noCatch;
import static com.github.harbby.gadtry.base.Throwables.throwsException;
import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;

public class ThrowablesTest
{
    @Test
    public void testNoCatch()
    {
        noCatch(() -> { new URL("file:"); });
        URL url1 = noCatch(() -> new URL("file:"));

        try {
            URL url = noCatch(() -> new URL("/harbby"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testThrowsException1()
    {
        try {
            throwsException(new IOException("IO_test"));
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
            throwsThrowable(new IOException("IO_test"));
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
            URL url = new URL("file:");
        }
        catch (IOException e) {
            throwsException(e);
        }

        try {
            try {
                URL url = new URL("/harbby");
            }
            catch (IOException e) {
                throwsException(e);
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
        //强制 抛出IOException个异常
        throwsException(IOException.class);
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
    public void getStackTraceAsStringTest()
    {
        Throwable error = new ClassCastException("cast error");
        error = new IOException(error);
        error = new SQLException(error);
        String msg = Throwables.getStackTraceAsString(error);
        Assert.assertTrue(msg.contains("cast error"));
    }
}
