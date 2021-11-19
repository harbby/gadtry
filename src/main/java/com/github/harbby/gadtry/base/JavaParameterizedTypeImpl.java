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
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * The following sources and the parameterizedTypeImpl.java source code in jdk8
 * package: sun.reflect.generics.reflectiveObjects
 * <p>
 * Jdk9 or above can't find this class
 * Alternatives are available: guice: new MoreTypes.ParameterizedTypeImpl(null, rawType, typeArguments)
 * guice see @link com.google.inject.internal.MoreTypes.ParameterizedTypeImpl(Type, Type, Type)
 *
 * <p>
 * Implementing class for ParameterizedType interface.
 * <p>
 * demo : Type type = JavaType.make(Map.class, new Type[]{String.class, String.class}, null)
 */

public class JavaParameterizedTypeImpl
        extends JavaTypes.ParameterizedType0
        implements Serializable
{
    private final Type[] actualTypeArguments;
    private final Class<?> rawType;
    private final Type ownerType;

    public JavaParameterizedTypeImpl(Class<?> rawType,
            Type[] actualTypeArguments,
            Type ownerType)
    {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = (ownerType != null) ? ownerType : rawType.getDeclaringClass();
        validateConstructorArguments();
    }

    private void validateConstructorArguments()
    {
        TypeVariable<?>[] formals = rawType.getTypeParameters();
        // check correct arity of actual type args
        if (formals.length != actualTypeArguments.length) {
            throw new MalformedParameterizedTypeException();
        }
        for (int i = 0; i < actualTypeArguments.length; i++) {
            // check actuals against formals' bounds
        }
    }

    /**
     * Returns an array of <tt>Type</tt> objects representing the actual type
     * arguments to this type.
     *
     * <p>Note that in some cases, the returned array be empty. This can occur
     * if this type represents a non-parameterized type nested within
     * a parameterized type.
     *
     * @return an array of <tt>Type</tt> objects representing the actual type
     * arguments to this type
     * throws <tt>TypeNotPresentException</tt> if any of the
     * actual type arguments refers to a non-existent type declaration
     * throws <tt>MalformedParameterizedTypeException</tt> if any of the
     * actual type parameters refer to a parameterized type that cannot
     * be instantiated for any reason
     * @since 1.5
     */
    public Type[] getActualTypeArguments()
    {
        return actualTypeArguments.clone();
    }

    /**
     * Returns the <tt>Type</tt> object representing the class or interface
     * that declared this type.
     *
     * @return the <tt>Type</tt> object representing the class or interface
     * that declared this type
     */
    public Class<?> getRawType()
    {
        return rawType;
    }

    /**
     * Returns a <tt>Type</tt> object representing the type that this type
     * is a member of.  For example, if this type is <tt>O.I</tt>,
     * return a representation of <tt>O</tt>.
     *
     * <p>If this type is a top-level type, <tt>null</tt> is returned.
     *
     * @return a <tt>Type</tt> object representing the type that
     * this type is a member of. If this type is a top-level type,
     * <tt>null</tt> is returned
     * throws <tt>TypeNotPresentException</tt> if the owner type
     * refers to a non-existent type declaration
     * throws <tt>MalformedParameterizedTypeException</tt> if the owner type
     * refers to a parameterized type that cannot be instantiated
     * for any reason
     */
    public Type getOwnerType()
    {
        return ownerType;
    }
}
