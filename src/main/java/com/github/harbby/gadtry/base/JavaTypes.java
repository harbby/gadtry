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

import com.github.harbby.gadtry.collection.mutable.MutableList;
import sun.reflect.generics.repository.AbstractRepository;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Throwables.noCatch;
import static com.github.harbby.gadtry.base.Throwables.throwsException;

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
    public static Type make(Class<?> rawType,
            Type[] actualTypeArguments,
            Type ownerType)
    {
        for (Type type : actualTypeArguments) {
            if (type instanceof Class<?>) {
                checkState(!((Class) type).isPrimitive(), "Java Generic Type not support PrimitiveType");
            }
        }
        checkState(!rawType.isPrimitive(), "rawType %s must not PrimitiveType", rawType);

        return new JavaParameterizedTypeImpl(rawType, actualTypeArguments,
                ownerType);
    }

    public static MapType makeMapType(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        if (keyType instanceof Class<?>) {
            checkState(!((Class<?>) keyType).isPrimitive(), "MapType keyType not support PrimitiveType");
        }
        if (valueType instanceof Class) {
            checkState(!((Class<?>) valueType).isPrimitive(), "MapType valueType not support PrimitiveType");
        }
        return new MapType(mapClass, keyType, valueType);
    }

    public static ArrayType makeArrayType(Type valueType)
    {
        if (valueType instanceof Class<?>) {
            checkState(!((Class<?>) valueType).isPrimitive(), "ArrayType valueType not support PrimitiveType");
        }
        return new ArrayType(valueType);
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
        else if (type instanceof GenericArrayType) {
            Class typeToClass = typeToClass(((GenericArrayType) type).getGenericComponentType());
            return Arrays.getArrayClass(typeToClass);
        }
        throw new IllegalArgumentException("Cannot convert type to class");
    }

    /**
     * Checks if a type can be converted to a Class. This is true for ParameterizedType and Class.
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
     * return primitive wrapper
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
            //checkState(aClass.isPrimitive(), "%s not is Primitive", aClass);
            throw new UnsupportedOperationException("this " + aClass + " have't support!");
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
            throw new UnsupportedOperationException("this " + aClass + " have't support!");
        }
    }

    private static <T, R> R getReflectMethod(Method method, T t)
            throws InvocationTargetException, IllegalAccessException
    {
        method.setAccessible(true);
        return (R) method.invoke(t);
    }

    public static String getClassGenericString(Class<?> javaClass)
    {
        return noCatch(() -> getReflectMethod(Class.class.getDeclaredMethod("getGenericSignature0"), javaClass));
    }

    /**
     * 获取Class的泛型信息(Get generic information about class)
     *
     * @param javaClass Class
     * @return TypeArgument[]
     */
    public static Map<String, TypeArgument[]> getClassGenericInfo(Class<?> javaClass)
    {
        try {
            Map<String, TypeArgument[]> typeSignatureMap = new LinkedHashMap<>();
            ClassRepository classRepository = getReflectMethod(Class.class.getDeclaredMethod("getGenericInfo"), javaClass);

            if (classRepository == null) {
                return Collections.emptyMap();
            }
            //-----2
            ClassSignature tree = getReflectMethod(AbstractRepository.class.getDeclaredMethod("getTree"), classRepository);
            //FormalTypeParameter[] formalTypeParameters = tree.getFormalTypeParameters();  //type 个数  === type[]
            SimpleClassTypeSignature typeSignature = tree.getSuperclass().getPath().get(0);
            typeSignatureMap.put(typeSignature.getName(), typeSignature.getTypeArguments());

            for (ClassTypeSignature it : tree.getSuperInterfaces()) {
                typeSignature = it.getPath().get(0);
                typeSignatureMap.put(typeSignature.getName(), typeSignature.getTypeArguments());
            }
            return typeSignatureMap;
        }
        catch (Exception e) {
            throw throwsException(e);
        }
    }

    public static List<Type> getClassGenericTypes(Class<?> javaClass)
    {
        ClassRepository classRepository = noCatch(() -> getReflectMethod(Class.class.getDeclaredMethod("getGenericInfo"),
                javaClass));

        if (classRepository == null) {
            return Collections.emptyList();
        }

        classRepository.getSuperclass();
        return MutableList.asList(classRepository.getSuperclass(), classRepository.getSuperInterfaces());
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> classTag(Class<?> runtimeClass)
    {
        checkState(!runtimeClass.isPrimitive(), "%s is isPrimitive", runtimeClass);
//        if (runtimeClass.isPrimitive()) {
//            return (Class<T>) JavaTypes.getWrapperClass(runtimeClass);
//        }
        return (Class<T>) runtimeClass;
    }
}
