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
import java.lang.reflect.Type;
import java.util.Map;

public class MapType
        extends JavaTypes.ParameterizedType0
        implements Serializable
{
    private final Class<? extends Map> mapClass;
    private final Type keyType;
    private final Type valueType;

    public MapType(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        this.mapClass = mapClass;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public static MapType make(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        return new MapType(mapClass, keyType, valueType);
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return new Type[] {keyType, valueType};
    }

    @Override
    public Class<?> getRawType()
    {
        return mapClass;
    }

    @Override
    public Type getOwnerType()
    {
        return null;
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
}
