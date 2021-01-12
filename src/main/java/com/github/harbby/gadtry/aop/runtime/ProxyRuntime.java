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
package com.github.harbby.gadtry.aop.runtime;

import com.github.harbby.gadtry.base.Throwables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public final class ProxyRuntime
{
    private ProxyRuntime()
    {
    }

    public static final String METHOD_START = "$_";
    private static final Field methodNameField;

    static {
        Field field;
        try {
            field = Method.class.getDeclaredField("name");
        }
        catch (NoSuchFieldException e) {
            field = getJava13MethodNameField();
        }
        field.setAccessible(true);
        methodNameField = field;
    }

    private static Field getJava13MethodNameField()
    {
        try {
            Method method = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            method.setAccessible(true);
            Field[] fields = (Field[]) method.invoke(Method.class, false);
            for (Field f : fields) {
                if ("name".equals(f.getName())) {
                    return f;
                }
            }
            throw new IllegalStateException();
        }
        catch (Exception e) {
            throw Throwables.throwsThrowable(e);
        }
    }

    public static Method findProxyClassMethod(Class<?> proxyClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException, IllegalAccessException
    {
        Method method = proxyClass.getDeclaredMethod(methodName, parameterTypes);
        requireNonNull(methodNameField, "methodNameField is null");
        methodNameField.set(method, method.getName().substring(METHOD_START.length()));
        return method;
    }
}
