/*
 * Copyright (C) 2018 The Harbby Authors
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

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JavaTypes
{
    private JavaTypes() {}

    /**
     * Static factory. Given a (generic) class, actual type arguments
     * and an owner type, creates a parameterized type.
     * This class can be instantiated with a a raw type that does not
     * represent a generic type, provided the list of actual type
     * arguments is empty.
     * If the ownerType argument is null, the declaring class of the
     * raw type is used as the owner type.
     * <p> This method throws a MalformedParameterizedTypeException
     * under the following circumstances:
     * If the number of actual type arguments (i.e., the size of the
     * array <tt>typeArgs</tt>) does not correspond to the number of
     * formal type arguments.
     * If any of the actual type arguments is not an instance of the
     * bounds on the corresponding formal.
     *
     * @param rawType the Class representing the generic type declaration being
     * instantiated
     * @param actualTypeArguments - a (possibly empty) array of types
     * representing the actual type arguments to the parameterized type
     * @param ownerType - the enclosing type, if known.
     * @return An instance of <tt>ParameterizedType</tt>
     * @throws MalformedParameterizedTypeException - if the instantiation
     * is invalid
     */
    public static ParameterizedType make(Class<?> rawType,
            Type[] actualTypeArguments,
            Type ownerType)
    {
        return new JavaParameterizedTypeImpl(rawType, actualTypeArguments,
                ownerType);
    }

    /**
     * Convert ParameterizedType or Class to a Class.
     */
    public static Class<?> typeToClass(Type type)
    {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        else if (type instanceof ParameterizedType) {
            return ((Class<?>) ((ParameterizedType) type).getRawType());
        }
        throw new IllegalArgumentException("Cannot convert type to class");
    }

    /**
     * Checks if a type can be converted to a Class. This is true for ParameterizedType and Class.
     */
    public static boolean isClassType(Type type)
    {
        return type instanceof Class<?> || type instanceof ParameterizedType;
    }
}
