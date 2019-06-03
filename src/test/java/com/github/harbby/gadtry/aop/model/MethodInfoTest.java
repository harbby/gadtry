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
package com.github.harbby.gadtry.aop.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;

@RunWith(JUnit4.class)
public class MethodInfoTest
{
    @Deprecated
    private Map<String, Connection> getConnections(@Deprecated String jdbc, double a2)
    {
        return Collections.emptyMap();
    }

    private Method method;
    private MethodInfo methodInfo;

    @Before
    public void init()
            throws NoSuchMethodException
    {
        this.method = MethodInfoTest.class.getDeclaredMethod("getConnections", String.class, double.class);
        this.methodInfo = MethodInfo.of(method);
    }

    @Test
    public void methodInfoGetNameTest()
    {
        Assert.assertEquals(methodInfo.getName(), method.getName());
    }

    @Test
    public void getModifiers()
    {
        Assert.assertEquals(methodInfo.getModifiers(), method.getModifiers());
    }

    @Test
    public void getParameterCount()
    {
        Assert.assertEquals(methodInfo.getParameterCount(), method.getParameterCount());
    }

    @Test
    public void getReturnType()
    {
        Assert.assertEquals(methodInfo.getReturnType(), method.getReturnType());
    }

    @Test
    public void getExceptionTypes()
    {
        Assert.assertEquals(methodInfo.getExceptionTypes(), method.getExceptionTypes());
    }

    @Test
    public void getParameterAnnotations()
    {
        Assert.assertEquals(methodInfo.getParameterAnnotations(), method.getParameterAnnotations());
    }

    @Test
    public void getAnnotations()
    {
        Assert.assertArrayEquals(methodInfo.getAnnotations(), method.getAnnotations());
        Assert.assertEquals(methodInfo.getAnnotations().length, 1);
    }

    @Test
    public void getAnnotation()
    {
        Assert.assertEquals(methodInfo.getAnnotation(Test.class), method.getAnnotation(Test.class));
    }

    @Test
    public void getParameterTypes()
    {
        Assert.assertEquals(methodInfo.getParameterTypes(), method.getParameterTypes());
    }

    @Test
    public void isVarArgs()
    {
        Assert.assertEquals(methodInfo.isVarArgs(), method.isVarArgs());
    }

    @Test
    public void isDefault()
    {
        Assert.assertEquals(methodInfo.isDefault(), method.isDefault());
    }

    @Test
    public void toStringTest()
    {
        Assert.assertNotNull(methodInfo.toString());
    }
}
