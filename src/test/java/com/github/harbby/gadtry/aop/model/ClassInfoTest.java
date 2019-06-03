/*
 * Copyright (C) 2018 The Harbby Authors
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClassInfoTest
{
    private final ClassInfo classInfo = ClassInfo.of(ClassInfoTest.class);

    @Test
    public void getAnnotations()
    {
        Assert.assertArrayEquals(classInfo.getAnnotations(), ClassInfoTest.class.getAnnotations());
    }

    @Test
    public void getCanonicalName()
    {
        Assert.assertEquals(classInfo.getCanonicalName(), ClassInfoTest.class.getCanonicalName());
    }

    @Test
    public void getName()
    {
        Assert.assertEquals(classInfo.getName(), ClassInfoTest.class.getName());
    }

    @Test
    public void getModifiers()
    {
        Assert.assertEquals(classInfo.getModifiers(), ClassInfoTest.class.getModifiers());
    }

    @Test
    public void getPackage()
    {
        Assert.assertEquals(classInfo.getPackage(), ClassInfoTest.class.getPackage());
    }

    @Test
    public void isInterface()
    {
        Assert.assertEquals(classInfo.isInterface(), ClassInfoTest.class.isInterface());
    }
}
