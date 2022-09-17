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
package com.github.harbby.gadtry.aop.proxy2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class MethodCallerImpl
        implements MethodCaller
{
    private final Object mock;
    private final Callable<Object> caller;
    private final Method method;
    private Field[] fields;

    public MethodCallerImpl(Object mock, Method method, Callable<Object> caller)
    {
        this.mock = mock;
        this.caller = caller;
        this.method = method;
    }

    @Override
    public Object getMock()
    {
        return mock;
    }

    @Override
    public String getName()
    {
        return method.getName();
    }

    @Override
    public Object proceed()
            throws Exception
    {
        return caller.call();
    }

    @Override
    public Object proceed(Object[] args)
            throws Throwable
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] getArgs()
    {
        Field[] fields = this.getFields();
        Object[] args = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                args[i] = field.get(caller);
            }
            catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <O> O getArgument(int i)
    {
        return (O) this.getArgs()[i];
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    private Field[] getFields()
    {
        Field[] fields = this.fields;
        if (fields != null) {
            return fields;
        }
        Field[] callerFields = caller.getClass().getDeclaredFields();
        fields = new Field[callerFields.length - 1];
        System.arraycopy(callerFields, 1, fields, 0, fields.length);
        this.fields = fields;
        return fields;
    }
}
