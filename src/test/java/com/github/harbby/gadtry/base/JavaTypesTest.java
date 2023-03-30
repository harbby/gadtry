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

import com.github.harbby.gadtry.aop.AopGo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.ArrayUtil.PRIMITIVE_TYPES;
import static com.github.harbby.gadtry.base.JavaTypes.getMethodSignature;
import static com.github.harbby.gadtry.base.Try.noCatch;

public class JavaTypesTest
{
    @Test
    public void getMethodSignatureTest()
            throws NoSuchMethodException
    {
        Assertions.assertEquals("()Ljava/lang/String;", getMethodSignature(Object.class.getMethod("toString")));
        Assertions.assertEquals("([CII)Ljava/lang/String;", getMethodSignature(String.class.getDeclaredMethod("valueOf", char[].class, int.class, int.class)));
    }

    @Test
    public void getClassSignatureTest()
    {
        String str = PRIMITIVE_TYPES.stream().map(JavaTypes::getClassSignature).collect(Collectors.joining());
        Assertions.assertEquals(str, "ISJFDZBCV");
    }

    @Test
    public void makeTest()
            throws IOException
    {
        Type listType = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type maType = JavaTypes.make(Map.class, new Type[] {String.class, listType}, null);

        Assertions.assertTrue(Serializables.serialize((Serializable) maType).length > 0);
        Assertions.assertTrue(maType.toString().length() > 0);
    }

    @Test
    public void makeByTypeReferenceTest()
    {
        Type mapType = JavaTypes.make(new TypeReference<Map<String, Object>>() {});
        Type type2 = JavaTypes.makeMapType(Map.class, String.class, Object.class);
        Assertions.assertEquals(type2, mapType);
    }

    @Test
    public void makePrimitive()
    {
        try {
            JavaTypes.make(List.class, new Type[] {int.class}, null);
            Assertions.fail();
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "Java Generic Type not support PrimitiveType");
        }

        try {
            JavaTypes.make(int.class, new Type[] {String.class}, null);
            Assertions.fail();
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "rawType int must not PrimitiveType");
        }
    }

    @Test
    public void getPrimitiveClassInitValue()
    {
        Assertions.assertNull(JavaTypes.getClassInitValue(void.class));
        Assertions.assertEquals(JavaTypes.getClassInitValue(double.class), 0.0d, 0.0d);
    }

    @Test
    public void makeArrayTypeTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        type = JavaTypes.makeArrayType(type);
        Assertions.assertEquals(type.getTypeName(), "java.util.List<java.lang.String>[]");
        Assertions.assertEquals(JavaTypes.typeToClass(type), List[].class);

        Assertions.assertEquals(JavaTypes.makeArrayType(String.class).getTypeName(), "java.lang.String[]");

        try {
            JavaTypes.makeArrayType(int.class);
            Assertions.fail();
        }
        catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void isClassType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assertions.assertTrue(JavaTypes.isClassType(type));
        Assertions.assertFalse(JavaTypes.isClassType(AopGo.proxy(Type.class).byInstance(String.class).aop(biner -> biner.doBefore(a -> {})).build()));
        Assertions.assertTrue(JavaTypes.isClassType(String.class));
    }

    @Test
    public void typeToClass()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assertions.assertEquals(JavaTypes.typeToClass(type), List.class);
        Assertions.assertEquals(JavaTypes.typeToClass(String.class), String.class);
    }

    @Test
    public void typeToClassGiveGenericArrayType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type arrayType = ArrayType.make(type);

        Assertions.assertTrue(JavaTypes.isClassType(arrayType));
        Assertions.assertEquals(JavaTypes.typeToClass(arrayType), List[].class);

        try {
            JavaTypes.typeToClass(AopGo.proxy(Type.class).byInstance(String.class).aop(biner -> biner.doBefore(a -> {})).build());
            Assertions.fail();
        }
        catch (IllegalArgumentException e) {
            Assertions.assertEquals("Cannot convert type to class", e.getMessage());
        }
    }

    @Test
    public void typeEqualsTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type type2 = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assertions.assertEquals(type, type2);
    }

    @Test
    public void hashCodeEqualsTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type type2 = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assertions.assertEquals(type.hashCode(), type2.hashCode());
    }

    @Test
    public void getPrimitiveClassTest()
    {
        List<Class<?>> wrappers = PRIMITIVE_TYPES.stream()
                .map(JavaTypes::getWrapperClass)
                .collect(Collectors.toList());
        List<Class<?>> primitiveTypes = wrappers.stream().map(JavaTypes::getPrimitiveClass)
                .collect(Collectors.toList());
        Assertions.assertEquals(primitiveTypes, PRIMITIVE_TYPES);

        try {
            JavaTypes.getPrimitiveClass(Object.class);
            Assertions.fail();
        }
        catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getWrapperClass()
    {
        List<Class<?>> pack = PRIMITIVE_TYPES.stream()
                .map(JavaTypes::getWrapperClass)
                .collect(Collectors.toList());

        List<Class<?>> typeClassList = pack.stream().map(x -> noCatch(() -> {
            Field field = x.getField("TYPE");
            field.setAccessible(true);
            return (Class<?>) field.get(null);
        })).collect(Collectors.toList());

        Assertions.assertEquals(PRIMITIVE_TYPES, typeClassList);

        try {
            JavaTypes.getWrapperClass(Object.class);
            Assertions.fail();
        }
        catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getClassGenericString()
    {
        String check = "Ljava/lang/Object;Ljava/util/function/Function<Ljava/util/Map<Ljava/lang/String;" +
                "Ljava/lang/Integer;>;Ljava/lang/String;>;Ljava/util/concurrent/Callable<Ljava/lang/Double;>;";
        Assertions.assertEquals(check,
                JavaTypes.getClassGenericString(GenericClassTest.class));
    }

    @Test
    public void getClassGenericTypes()
    {
        List<Type> types = JavaTypes.getClassGenericTypes(GenericClassTest.class);
        Assertions.assertEquals(types.get(1), JavaTypes.make(Function.class, new Type[] {JavaTypes.makeMapType(Map.class, String.class, Integer.class), String.class}, null));
    }

    @Test
    public void makeMapTypeTest()
    {
        MapType mapType = JavaTypes.makeMapType(Map.class, String.class, Integer.class);
        Assertions.assertEquals(mapType.getTypeName(), "java.util.Map<java.lang.String, java.lang.Integer>");
    }

    @Test
    public void getClassGenericTypesReturnEmpGiveJavaTypesTest()
    {
        List<Type> types = JavaTypes.getClassGenericTypes(JavaTypesTest.class);
        Assertions.assertEquals(types, Collections.singletonList(Object.class));
    }

    @Test
    public void getClassLocationTest()
    {
        String path = JavaTypes.getClassLocation(Test.class).getPath();
        String name = new File(path).getName();
        Assertions.assertTrue(Pattern.matches("junit-jupiter-api-[0-9.]+\\.jar", name));
    }

    private static class GenericClassTest
            implements Function<Map<String, Integer>, String>, Callable<Double>
    {
        @Override
        public Double call()
                throws Exception
        {
            return null;
        }

        @Override
        public String apply(Map<String, Integer> o)
        {
            return null;
        }
    }
}
