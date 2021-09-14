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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * copy jdk GenericArrayTypeImpl
 */
public class ArrayType
        implements GenericArrayType, Serializable
{
    private final Type genericComponentType;

    // private constructor enforces use of static factory
    public ArrayType(Type ct)
    {
        genericComponentType = ct;
    }

    public Type getValueType()
    {
        return genericComponentType;
    }

    /**
     * Factory method.
     *
     * @param ct - the desired component type of the generic array type
     *           being created
     * @return a generic array type with the desired component type
     */
    public static ArrayType make(Type ct)
    {
        return new ArrayType(ct);
    }

    /**
     * Returns a {@code Type} object representing the component type
     * of this array.
     *
     * @return a {@code Type} object representing the component type
     * of this array
     * @since 1.5
     */
    public Type getGenericComponentType()
    {
        return getValueType(); // return cached component type
    }

    public String toString()
    {
        return getGenericComponentType().getTypeName() + "[]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof GenericArrayType) {
            GenericArrayType that = (GenericArrayType) o;
            return Objects.equals(genericComponentType, that.getGenericComponentType());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(genericComponentType);
    }
}
