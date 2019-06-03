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

import com.github.harbby.gadtry.collection.mutable.MutableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public interface MethodInfo
{
    String getName();

    Class<?> getReturnType();

    int getModifiers();

    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    public Annotation[] getAnnotations();

    Class<?>[] getParameterTypes();

    int getParameterCount();

    Class<?>[] getExceptionTypes();

    Annotation[][] getParameterAnnotations();

    boolean isDefault();

    boolean isVarArgs();

    public static MethodInfo of(Method method)
    {
        return new MethodInfo()
        {
            @Override
            public String getName()
            {
                return method.getName();
            }

            @Override
            public Class<?> getReturnType()
            {
                return method.getReturnType();
            }

            @Override
            public int getModifiers()
            {
                return method.getModifiers();
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return method.getAnnotation(annotationClass);
            }

            @Override
            public Annotation[] getAnnotations()
            {
                return method.getAnnotations();
            }

            @Override
            public Class<?>[] getParameterTypes()
            {
                return method.getParameterTypes();
            }

            @Override
            public int getParameterCount()
            {
                return method.getParameterCount();
            }

            @Override
            public Class<?>[] getExceptionTypes()
            {
                return method.getExceptionTypes();
            }

            @Override
            public boolean isDefault()
            {
                return method.isDefault();
            }

            @Override
            public Annotation[][] getParameterAnnotations()
            {
                return method.getParameterAnnotations();
            }

            @Override
            public boolean isVarArgs()
            {
                return method.isVarArgs();
            }

            @Override
            public String toString()
            {
                Map<String, Object> helper = new HashMap<>();
                helper.put("name", getName());
                helper.put("returnType", getReturnType());
                helper.put("modifiers", getModifiers());
                helper.put("Annotations", MutableList.<Annotation>of(method.getAnnotations()));
                helper.put("method", method);
                return super.toString() + helper.toString();
            }
        };
    }
}
