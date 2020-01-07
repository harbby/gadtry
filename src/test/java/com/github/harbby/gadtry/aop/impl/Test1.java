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
package com.github.harbby.gadtry.aop.impl;

public class Test1
{
    private final String name;

    public Test1(String name)
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
        return name() + age();
    }

    private String city()
    {
        return "chengdu";
    }

    protected String mode()
    {
        return "protected";
    }
}
