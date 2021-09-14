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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class MapType
        implements ParameterizedType, Serializable
{
    private final ParameterizedType parameterizedType;
    private final Class<? extends Map> mapClass;
    private final Type keyType;
    private final Type valueType;

    public MapType(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        this.mapClass = mapClass;
        this.keyType = keyType;
        this.valueType = valueType;
        this.parameterizedType = new JavaParameterizedTypeImpl(mapClass, new Type[] {keyType, valueType}, null);
    }

    public static MapType make(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        return new MapType(mapClass, keyType, valueType);
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return parameterizedType.getActualTypeArguments();
    }

    @Override
    public Type getRawType()
    {
        return parameterizedType.getRawType();
    }

    @Override
    public Type getOwnerType()
    {
        return parameterizedType.getOwnerType();
    }

    public Class<? extends Map> getBaseClass()
    {
        return mapClass;
    }

    public Type getKeyType()
    {
        return keyType;
    }

    public Type getValueType()
    {
        return valueType;
    }

    @Override
    public int hashCode()
    {
        return parameterizedType.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        Object eq = obj;
        if (obj instanceof MapType) {
            eq = ((MapType) obj).parameterizedType;
        }
        return parameterizedType.equals(eq);
    }

    @Override
    public String toString()
    {
        return parameterizedType.toString();
    }

    @Override
    public String getTypeName()
    {
        return parameterizedType.getTypeName();
    }
}
