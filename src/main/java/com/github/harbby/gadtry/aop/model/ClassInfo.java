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

import java.lang.annotation.Annotation;

public interface ClassInfo
{
    String getName();

    String getCanonicalName();

    boolean isInterface();

    int getModifiers();

    Package getPackage();

    Annotation[] getAnnotations();

    public static ClassInfo of(Class<?> aClass)
    {
        return new ClassInfo()
        {
            @Override
            public String getName()
            {
                return aClass.getName();
            }

            @Override
            public String getCanonicalName()
            {
                return aClass.getCanonicalName();
            }

            @Override
            public boolean isInterface()
            {
                return aClass.isAnnotation();
            }

            @Override
            public int getModifiers()
            {
                return aClass.getModifiers();
            }

            @Override
            public Package getPackage()
            {
                return aClass.getPackage();
            }

            @Override
            public Annotation[] getAnnotations()
            {
                return aClass.getAnnotations();
            }
        };
    }
}
