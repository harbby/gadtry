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

import com.github.harbby.gadtry.Beta;
import com.github.harbby.gadtry.ioc.Autowired;
import com.github.harbby.gadtry.ioc.Config;

public class Test1
        extends Test1Supper
{
    private final String name;

    @Beta
    @Autowired
    @Deprecated
    public Test1(long a, @Config("old_name") @Deprecated String name)
    {
        this.name = name;
    }

    public Test1(@Config("name") @Deprecated String name)
    {
        this.name = name;
    }

    public static Test1 of(String name)
    {
        return new Test1(name);
    }

    public String name()
    {
        return name;
    }

    public int age()
    {
        return 18;
    }

    public String getNameAndAge()
    {
        return this.name() + this.age();
    }

    @Deprecated
    @Beta
    public double sum(String begin, int a, double b, float c)
            throws Exception
    {
        return begin.length() + a + b + c;
    }

    protected double castToDouble(long a)
    {
        return a;
    }

    @Deprecated
    double[] getDoubleArray(long a1, String a2)
    {
        return new double[2];
    }
}
