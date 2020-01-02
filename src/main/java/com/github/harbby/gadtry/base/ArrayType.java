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

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

public class ArrayType
        implements GenericArrayType
{
    private final GenericArrayType genericArrayType;

    public ArrayType(Type valueType)
    {
        this.genericArrayType = GenericArrayTypeImpl.make(valueType);
    }

    @Override
    public Type getGenericComponentType()
    {
        return genericArrayType.getGenericComponentType();
    }

    public Type getValueType()
    {
        return getGenericComponentType();
    }

    @Override
    public String getTypeName()
    {
        return genericArrayType.getTypeName();
    }

    @Override
    public int hashCode()
    {
        return genericArrayType.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        Object eq = obj;
        if (obj instanceof ArrayType) {
            eq = ((ArrayType) obj).genericArrayType;
        }
        return genericArrayType.equals(eq);
    }

    @Override
    public String toString()
    {
        return genericArrayType.toString();
    }
}
