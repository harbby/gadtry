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

import com.github.harbby.gadtry.collection.MutableList;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class JavaTypes
{
    private JavaTypes()
    {
    }

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
     * array typeArgs) does not correspond to the number of
     * formal type arguments.
     * If any of the actual type arguments is not an instance of the
     * bounds on the corresponding formal.
     *
     * @param rawType             the Class representing the generic type declaration being
     *                            instantiated
     * @param actualTypeArguments - a (possibly empty) array of types
     *                            representing the actual type arguments to the parameterized type
     * @param ownerType           - the enclosing type, if known.
     * @return An instance of ParameterizedType
     * @throws MalformedParameterizedTypeException - if the instantiation
     *                                             is invalid
     */
    public static Type make(Class<?> rawType,
            Type[] actualTypeArguments,
            Type ownerType)
    {
        for (Type type : actualTypeArguments) {
            checkNotPrimitive(type, "Java Generic Type not support PrimitiveType");
        }
        checkNotPrimitive(rawType, "rawType " + rawType + " must not PrimitiveType");
        return new JavaParameterizedTypeImpl(rawType, actualTypeArguments, ownerType);
    }

    public static MapType makeMapType(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        checkNotPrimitive(keyType, "MapType keyType not support PrimitiveType");
        checkNotPrimitive(valueType, "MapType valueType not support PrimitiveType");
        return new MapType(mapClass, keyType, valueType);
    }

    public static void checkNotPrimitive(Type type, String msg)
    {
        if (type instanceof Class<?>) {
            checkState(!((Class<?>) type).isPrimitive(), msg);
        }
    }

    public static ArrayType makeArrayType(Type valueType)
    {
        checkNotPrimitive(valueType, "ArrayType valueType not support PrimitiveType");
        return new ArrayType(valueType);
    }

    /**
     * Convert ParameterizedType or Class to a Class.
     *
     * @param type java type
     * @return Type class
     */
    public static Class<?> typeToClass(Type type)
    {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        else if (type instanceof ParameterizedType) {
            return ((Class<?>) ((ParameterizedType) type).getRawType());
        }
        else if (type instanceof GenericArrayType) {
            Class<?> typeToClass = typeToClass(((GenericArrayType) type).getGenericComponentType());
            return Arrays.getArrayClass(typeToClass);
        }
        throw new IllegalArgumentException("Cannot convert type to class");
    }

    /**
     * Checks if a type can be converted to a Class. This is true for ParameterizedType and Class.
     *
     * @param type java.lang.reflect.Type
     * @return is Class
     */
    public static boolean isClassType(Type type)
    {
        return type instanceof Class<?> || type instanceof ParameterizedType || type
                instanceof GenericArrayType;
    }

    public static <T> T getClassInitValue(Class<?> aClass)
    {
        if (!aClass.isPrimitive()) {
            return null;
        }
        return getPrimitiveClassInitValue(aClass);
    }

    public static <T> T getPrimitiveClassInitValue(Class<?> aClass)
    {
        checkState(aClass.isPrimitive(), "%s not is primitive", aClass);
        if (aClass == void.class) {
            return null;
        }
        Object arr = java.lang.reflect.Array.newInstance(aClass, 1);
        return (T) Array.get(arr, 0);
    }

    /**
     * @param aClass primitive class
     * @return primitive wrapper
     */
    public static Class<?> getWrapperClass(Class<?> aClass)
    {
        if (aClass == int.class) {  //Integer.TYPE
            return Integer.class;
        }
        else if (aClass == short.class) {
            return Short.class;
        }
        else if (aClass == long.class) {
            return Long.class;
        }
        else if (aClass == float.class) {
            return Float.class;
        }
        else if (aClass == double.class) {
            return Double.class;
        }
        else if (aClass == byte.class) {
            return Byte.class;
        }
        else if (aClass == boolean.class) {
            return Boolean.class;
        }
        else if (aClass == char.class) {
            return Character.class;
        }
        else if (aClass == void.class) {
            return Void.class;
        }
        else {
            throw new UnsupportedOperationException("this " + aClass + " haven't support!");
        }
    }

    public static Class<?> getPrimitiveClass(Class<?> aClass)
    {
        if (aClass == Integer.class) {  //Integer.TYPE
            return int.class;
        }
        else if (aClass == Short.class) {
            return short.class;
        }
        else if (aClass == Long.class) {
            return long.class;
        }
        else if (aClass == Float.class) {
            return float.class;
        }
        else if (aClass == Double.class) {
            return double.class;
        }
        else if (aClass == Byte.class) {
            return byte.class;
        }
        else if (aClass == Boolean.class) {
            return boolean.class;
        }
        else if (aClass == Character.class) {
            return char.class;
        }
        else if (aClass == Void.class) {
            return void.class;
        }
        else {
            throw new UnsupportedOperationException("this " + aClass + " haven't support!");
        }
    }

    /**
     * see: javap -s java.lang.String
     *
     * @param method java method
     * @return method signature
     */
    public static String getMethodSignature(Method method)
    {
        String parameterSignature = java.util.Arrays.stream(method.getParameterTypes())
                .map(JavaTypes::getClassSignature)
                .collect(Collectors.joining(""));

        return String.format("(%s)%s", parameterSignature, getClassSignature(method.getReturnType()));
    }

    public static String getClassSignature(final Class<?> type)
    {
        if (int.class == type) {
            return "I";
        }
        else if (void.class == type) {
            return "V";
        }
        else if (boolean.class == type) {
            return "Z";
        }
        else if (char.class == type) {  // Character
            return "C";
        }
        else if (byte.class == type) {
            return "B";
        }
        else if (short.class == type) {
            return "S";
        }
        else if (float.class == type) {
            return "F";
        }
        else if (long.class == type) {
            return "J";
        }
        else if (double.class == type) {
            return "D";
        }
        else if (type.isArray()) {
            return "[" + getClassSignature(type.getComponentType());
        }
        else {
            checkNotPrimitive(type, "not found primitive type " + type);
            return "L" + type.getName().replaceAll("\\.", "/") + ";";
        }
    }

    public static String getClassGenericString(Class<?> javaClass)
    {
        try {
            Method method = Class.class.getDeclaredMethod("getGenericSignature0");
            method.setAccessible(true);
            return (String) method.invoke(javaClass);
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new UnsupportedOperationException("jdk not support Class.class.getGenericSignature0()");
        }
    }

    /**
     * 获取Class的泛型信息(Get generic information about class)
     *
     * @param javaClass Class
     * @return List Type
     */
    public static List<Type> getClassGenericTypes(Class<?> javaClass)
    {
        return MutableList.asList(javaClass.getGenericSuperclass(), javaClass.getGenericInterfaces());
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> classTag(Class<?> runtimeClass)
    {
        checkNotPrimitive(runtimeClass, runtimeClass + " isPrimitive");
        return (Class<T>) runtimeClass;
    }

    public static URL getClassLocation(Class<?> aClass)
    {
        ProtectionDomain pd = aClass.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        return cs.getLocation();
    }
}
