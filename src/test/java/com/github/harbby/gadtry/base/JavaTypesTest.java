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
import com.github.harbby.gadtry.function.Function1;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Arrays.PRIMITIVE_TYPES;
import static com.github.harbby.gadtry.base.JavaTypes.getMethodSignature;
import static com.github.harbby.gadtry.base.Try.noCatch;

public class JavaTypesTest
{
    @Test
    public void getMethodSignatureTest()
            throws NoSuchMethodException
    {
        Assert.assertEquals("()Ljava/lang/String;", getMethodSignature(Object.class.getMethod("toString")));
        Assert.assertEquals("([CII)Ljava/lang/String;", getMethodSignature(String.class.getDeclaredMethod("valueOf", char[].class, int.class, int.class)));
    }

    @Test
    public void getClassSignatureTest()
    {
        String str = PRIMITIVE_TYPES.stream().map(JavaTypes::getClassSignature).collect(Collectors.joining());
        Assert.assertEquals(str, "ISJFDZBCV");
    }

    @Test
    public void makeTest()
            throws IOException
    {
        Type listType = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type maType = JavaTypes.make(Map.class, new Type[] {String.class, listType}, null);

        Assert.assertTrue(Serializables.serialize((Serializable) maType).length > 0);
        Assert.assertTrue(maType.toString().length() > 0);
    }

    @Test
    public void makeByTypeReferenceTest()
    {
        Type mapType = JavaTypes.make(new TypeReference<Map<String, Object>>() {});
        Type type2 = JavaTypes.makeMapType(Map.class, String.class, Object.class);
        Assert.assertEquals(type2, mapType);
    }

    @Test
    public void makePrimitive()
    {
        try {
            JavaTypes.make(List.class, new Type[] {int.class}, null);
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "Java Generic Type not support PrimitiveType");
        }

        try {
            JavaTypes.make(int.class, new Type[] {String.class}, null);
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "rawType int must not PrimitiveType");
        }
    }

    @Test
    public void getPrimitiveClassInitValue()
    {
        Assert.assertNull(JavaTypes.getPrimitiveClassInitValue(void.class));
        Assert.assertEquals((double) JavaTypes.getPrimitiveClassInitValue(double.class), 0.0d, 0.0d);
    }

    @Test
    public void makeArrayTypeTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        type = JavaTypes.makeArrayType(type);
        Assert.assertEquals(type.getTypeName(), "java.util.List<java.lang.String>[]");
        Assert.assertEquals(JavaTypes.typeToClass(type), List[].class);

        Assert.assertEquals(JavaTypes.makeArrayType(String.class).getTypeName(), "java.lang.String[]");

        try {
            JavaTypes.makeArrayType(int.class);
            Assert.fail();
        }
        catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void isClassType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertTrue(JavaTypes.isClassType(type));
        Assert.assertFalse(JavaTypes.isClassType(AopGo.proxy(Type.class).byInstance(String.class).aop(biner -> biner.doBefore(a -> {})).build()));
        Assert.assertTrue(JavaTypes.isClassType(String.class));
    }

    @Test
    public void typeToClass()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(JavaTypes.typeToClass(type), List.class);
        Assert.assertEquals(JavaTypes.typeToClass(String.class), String.class);
    }

    @Test
    public void typeToClassGiveGenericArrayType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type arrayType = ArrayType.make(type);

        Assert.assertTrue(JavaTypes.isClassType(arrayType));
        Assert.assertEquals(JavaTypes.typeToClass(arrayType), List[].class);

        try {
            JavaTypes.typeToClass(AopGo.proxy(Type.class).byInstance(String.class).aop(biner -> biner.doBefore(a -> {})).build());
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertEquals("Cannot convert type to class", e.getMessage());
        }
    }

    @Test
    public void typeEqualsTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type type2 = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(type, type2);
    }

    @Test
    public void hashCodeEqualsTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type type2 = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(type.hashCode(), type2.hashCode());
    }

    @Test
    public void getPrimitiveClassTest()
    {
        List<Class<?>> wrappers = PRIMITIVE_TYPES.stream()
                .map(JavaTypes::getWrapperClass)
                .collect(Collectors.toList());
        List<Class<?>> primitiveTypes = wrappers.stream().map(JavaTypes::getPrimitiveClass)
                .collect(Collectors.toList());
        Assert.assertEquals(primitiveTypes, PRIMITIVE_TYPES);

        try {
            JavaTypes.getPrimitiveClass(Object.class);
            Assert.fail();
        }
        catch (UnsupportedOperationException ignored) {
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

        Assert.assertEquals(PRIMITIVE_TYPES, typeClassList);

        try {
            JavaTypes.getWrapperClass(Object.class);
            Assert.fail();
        }
        catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void getClassGenericString()
    {
        String check = "Ljava/lang/Object;Lcom/github/harbby/gadtry/function/Function1<Ljava/util/Map<Ljava/lang/String;" +
                "Ljava/lang/Integer;>;Ljava/lang/String;>;Ljava/util/concurrent/Callable<Ljava/lang/Double;>;";
        Assert.assertEquals(check,
                JavaTypes.getClassGenericString(GenericClassTest.class));
    }

    @Test
    public void getClassGenericTypes()
    {
        List<Type> types = JavaTypes.getClassGenericTypes(GenericClassTest.class);
        Assert.assertEquals(types.get(1), JavaTypes.make(Function1.class, new Type[] {JavaTypes.makeMapType(Map.class, String.class, Integer.class), String.class}, null));
    }

    @Test
    public void makeMapTypeTest()
    {
        MapType mapType = JavaTypes.makeMapType(Map.class, String.class, Integer.class);
        Assert.assertEquals(mapType.getTypeName(), "java.util.Map<java.lang.String, java.lang.Integer>");
    }

    @Test
    public void getClassGenericTypesReturnEmpGiveJavaTypesTest()
    {
        List<Type> types = JavaTypes.getClassGenericTypes(JavaTypesTest.class);
        Assert.assertEquals(types, Collections.singletonList(Object.class));
    }

    @Test
    public void getClassLocationTest()
    {
        String path = JavaTypes.getClassLocation(Test.class).getPath();
        Assert.assertEquals(new File(path).getName(), "junit-4.12.jar");
    }

    private static class GenericClassTest
            implements Function1<Map<String, Integer>, String>, Callable<Double>
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
