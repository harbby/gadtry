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
import java.util.Objects;
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

    public static Type make(TypeReference<?> typeReference)
    {
        return typeReference.getType();
    }

    public static MapType makeMapType(Class<? extends Map> mapClass, Type keyType, Type valueType)
    {
        checkNotPrimitive(keyType, "MapType keyType not support PrimitiveType");
        checkNotPrimitive(valueType, "MapType valueType not support PrimitiveType");
        return MapType.make(mapClass, keyType, valueType);
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
            return ArrayUtil.getArrayClass(typeToClass);
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

    public static <T> T getClassInitValue(Class<T> aClass)
    {
        return TypeWrapper.getDefaultValue(aClass);
    }

    /**
     * @param aClass primitive class
     * @return primitive wrapper
     */
    public static Class<?> getWrapperClass(Class<?> aClass)
    {
        return TypeWrapper.asWrapperType(aClass);
    }

    public static Class<?> getPrimitiveClass(Class<?> aClass)
    {
        return TypeWrapper.asPrimitiveType(aClass);
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
        if (type.isPrimitive()) {
            return String.valueOf(TypeWrapper.forPrimitiveType(type).basicTypeChar());
        }
        else if (type.isArray()) {
            return "[" + getClassSignature(type.getComponentType());
        }
        else {
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

    abstract static class ParameterizedType0
            implements ParameterizedType
    {
        public abstract Class<?> getRawType();

        @Override
        public int hashCode()
        {
            return Objects.hashCode(getRawType()) ^
                    Objects.hashCode(getOwnerType()) ^
                    java.util.Arrays.hashCode(getActualTypeArguments());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ParameterizedType)) {
                return false;
            }

            ParameterizedType other = (ParameterizedType) obj;

            return Objects.equals(this.getRawType(), other.getRawType()) &&
                    Objects.equals(this.getOwnerType(), other.getOwnerType()) &&
                    java.util.Arrays.equals(this.getActualTypeArguments(), other.getActualTypeArguments());
        }

        @Override
        public String toString()
        {
            return toString(this);
        }

        private static String toString(JavaTypes.ParameterizedType0 parameterizedType)
        {
            StringBuilder sb = new StringBuilder();
            Type ownerType = parameterizedType.getOwnerType();
            Class<?> rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (ownerType != null) {
                if (ownerType instanceof Class) {
                    sb.append(((Class<?>) ownerType).getName());
                }
                else {
                    sb.append(ownerType);
                }

                sb.append(".");

                if (ownerType instanceof JavaParameterizedTypeImpl) {
                    // Find simple name of nested type by removing the
                    // shared prefix with owner.
                    sb.append(rawType.getName().replace(((JavaTypes.ParameterizedType0) ownerType).getRawType().getName() + "$",
                            ""));
                }
                else {
                    sb.append(rawType.getName());
                }
            }
            else {
                sb.append(rawType.getName());
            }

            if (actualTypeArguments != null &&
                    actualTypeArguments.length > 0) {
                sb.append("<");
                boolean first = true;
                for (Type t : actualTypeArguments) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(t.getTypeName());
                    first = false;
                }
                sb.append(">");
            }

            return sb.toString();
        }
    }
}
