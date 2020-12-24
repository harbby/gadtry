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

import com.github.harbby.gadtry.function.PrivilegedAction;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class Java11ApiPrivilegedTest
{
    private static final Unsafe unsafe = Platform.getUnsafe();

    @Test(expected = java.lang.reflect.InaccessibleObjectException.class)
    public void should_throw_InaccessibleObjectException()
            throws Exception
    {
        Runnable thunk = () -> {};
        Method method = Class.forName("jdk.internal.ref.Cleaner").getDeclaredMethod("create", Object.class, Runnable.class);
        method.setAccessible(true);
        method.invoke(null, null, thunk);
    }

    /**
     * see: {@link Java11ApiPrivilegedTest#should_throw_InaccessibleObjectException}
     */
    @Test
    public void should_not_throw_InaccessibleObjectException_doPrivilegedTest()
            throws Exception
    {
        Runnable thunk = () -> {};
        Object out = Platform.doPrivileged(ByteBuffer.class, new PrivilegedAction<Object>()
        {
            @Override
            public Object run()
                    throws Exception
            {
                Method method = Class.forName("jdk.internal.ref.Cleaner").getDeclaredMethod("create", Object.class, Runnable.class);
                method.setAccessible(true);
                return method.invoke(null, null, thunk);
            }
        });
        Assert.assertEquals("jdk.internal.ref.Cleaner", out.getClass().getName());
    }

    /**
     * 这个例子在java9+ jdk上不会打印 Warring
     */
    @Test
    public void should_not_printWarring_doPrivilegedTest()
            throws Exception
    {
        String log = "hello;";
        OutputStream out = Platform.doPrivileged(FilterOutputStream.class, new PrivilegedAction<OutputStream>()
        {
            @Override
            public OutputStream run()
                    throws Exception
            {
                System.out.println(log);
                if (!"hello;".equals(log)) {
                    throw new IllegalStateException("check failed");
                }

                Field field = FilterOutputStream.class.getDeclaredField("out");
                field.setAccessible(true);
                return (OutputStream) field.get(System.out);
            }
        });
        Assert.assertNotNull(out);
    }

    @Test
    public void should_not_printWarring_dojInterface()
            throws Exception
    {
        Java11PrivateApi api = Platform.doPrivileged(Java11PrivateApi.class, new JvmPrivilegeFunctionDemo());
        OutputStream outputStream = api.getSystemOut();
        Assert.assertNotNull(outputStream);
    }

    /**
     * not set: --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED
     */
    @Test
    public void should_return_jdk9Cleaner_dojInterface()
            throws Exception
    {
        Java11PrivateApi api = Platform.doPrivileged(Java11PrivateApi.class, new JvmPrivilegeFunctionDemo());
        Object cleaner = api.createCleaner(null, () -> System.out.println("hello java 11"));
        Assert.assertEquals("jdk.internal.ref.Cleaner", cleaner.getClass().getName());
    }

    @Test(expected = java.lang.reflect.InaccessibleObjectException.class)
    public void should_Throw_InaccessibleObjectException_dojInterface()
            throws Exception
    {
        Java11PrivateApi api = Platform.doPrivileged(Java11PrivateApi.class, new JvmPrivilegeFunctionDemo());
        api.createCleanerThrowError(null, () -> System.out.println("hello java 11"));
    }

    public static interface Java11PrivateApi
    {
        /**
         * not printWarring
         */
        @Privilege(FilterOutputStream.class)
        public OutputStream getSystemOut()
                throws Exception;

        /**
         * not --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED
         */
        @Privilege(ByteBuffer.class)
        public Object createCleaner(Object ob, Runnable thunk)
                throws Exception;

        /**
         * should throw java.lang.reflect.InaccessibleObjectException
         * your must add --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED
         */
        public void createCleanerThrowError(Object ob, Runnable thunk)
                throws Exception;
    }

    public static class JvmPrivilegeFunctionDemo
            implements Java11PrivateApi
    {
        @Override
        public OutputStream getSystemOut()
                throws Exception
        {
            Field field = FilterOutputStream.class.getDeclaredField("out");
            field.setAccessible(true);
            return (OutputStream) field.get(System.out);
        }

        @Override
        public Object createCleaner(Object ob, Runnable thunk)
                throws Exception
        {
            Method method = Class.forName("jdk.internal.ref.Cleaner").getDeclaredMethod("create", Object.class, Runnable.class);
            method.setAccessible(true);
            return method.invoke(null, ob, thunk);
        }

        @Override
        public void createCleanerThrowError(Object ob, Runnable thunk)
                throws Exception
        {
            createCleaner(ob, thunk);
        }
    }
}
