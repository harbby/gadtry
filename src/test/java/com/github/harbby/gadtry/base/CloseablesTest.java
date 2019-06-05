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

import com.github.harbby.gadtry.collection.mutable.MutableList;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class CloseablesTest
{
    @Test
    public void threadContextClassLoaderTest()
    {
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0]);
        Assert.assertTrue(Thread.currentThread().getContextClassLoader() != urlClassLoader);

        try (Closeables ignored = Closeables.openThreadContextClassLoader(urlClassLoader)) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Assert.assertTrue(classLoader == urlClassLoader);
        }
        Assert.assertTrue(Thread.currentThread().getContextClassLoader() != urlClassLoader);
    }

    @Test(expected = NoSuchElementException.class)
    public void getReturnError()
    {
        Closeables.autoClose(() -> {}).get();
    }

    @Test(expected = NoSuchElementException.class)
    public void getCloseError()
    {
        try (Closeables closeables = Closeables.autoClose(() -> {
            throw new IOException("close");
        })) {
            Assert.assertNull(closeables.get());
            Throwables.throwsException(IOException.class);
        }
        catch (IOException e) {
            Assert.assertEquals(e.getMessage(), "close");
        }
    }

    @Test
    public void autoCloseGiveNullInstance()
    {
        try (Closeables<Connection> closeables = Closeables.autoClose(null, conn -> {})) {
            Assert.assertNull(closeables.get());
        }
    }

    @Test
    public void autoCloseGiveErrorClose()
    {
        try (Closeables<String> closeables = Closeables.autoClose("init", conn -> {
            throw new IOException("close error");
        })) {
            Assert.assertEquals(closeables.get(), "init");
            throw new IOException("running error");
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.assertEquals("running error", e.getMessage());
            Assert.assertEquals("close error", e.getSuppressed()[0].getMessage());
        }
    }

    @Test
    public void autoCloseByInstanceCreateError()
    {
        try (Closeables<Connection> conn = Closeables.autoClose(DriverManager.getConnection(""), connection -> {
            connection.close();
            Assert.fail();
        })) {
            Connection client = conn.get();
            Assert.fail();
            //...
        }
        catch (SQLException ignored) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void autoCloseByInstance()
    {
        List<String> arrayList = MutableList.of("start");
        try (Closeables<List<String>> listGetter = Closeables.autoClose(arrayList, list -> {
            list.add("bye");
        })) {
            listGetter.get().add("running");
            //...
        }
        Assert.assertEquals(arrayList, Arrays.asList("start", "running", "bye"));
    }

    @Test
    public void autoClose()
    {
        List<String> list = MutableList.of("start");
        try (Closeables ignored = Closeables.autoClose(() -> list.add("bye"))) {
            list.add("running");
            //...
        }
        Assert.assertEquals(list, Arrays.asList("start", "running", "bye"));
    }
}
