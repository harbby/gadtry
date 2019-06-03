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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JavaTypesTest
{
    @Test
    public void make()
            throws IOException
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, Map.class);
        Assert.assertTrue(Serializables.serialize((Serializable) type).length > 0);
        Assert.assertTrue(type.toString().length() > 0);
    }

    @Test
    public void makePrimitive()
    {
        try {
            Type type = JavaTypes.make(List.class, new Type[] {int.class}, null);
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "Java Generic Type not support PrimitiveType");
        }
    }

    @Test
    public void makeArrayType()
    {
        Type type = JavaTypes.make(List[].class, new Type[] {String.class}, null);
        Assert.assertEquals(type.getTypeName(), "java.util.List<java.lang.String>[]");
    }

    @Test
    public void typeToClass()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(JavaTypes.typeToClass(type), List.class);
        Assert.assertEquals(JavaTypes.typeToClass(String.class), String.class);
    }

    @Test
    public void arrayTypeToClass()
    {
        Type type = JavaTypes.make(List[].class, new Type[] {String.class}, null);
        Assert.assertEquals(JavaTypes.typeToClass(type), List[].class);
    }

    @Test
    public void isClassType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertTrue(JavaTypes.isClassType(type));
        Assert.assertTrue(JavaTypes.isClassType(String.class));
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

//    @Test
//    public void typeGenericSignatureTest()
//            throws NoSuchMethodException
//    {
//        //see:  java.lang.reflect.Field.class.getDeclaredMethod("getGenericSignature");
//
//        Type mapType = JavaTypes.make(HashMap.class, new Type[] {String.class, Double.class}, null);
//        String signature = TypeFactory.defaultInstance().constructType(mapType).getGenericSignature();
//        Assert.assertEquals(signature, "Ljava/util/HashMap<Ljava/lang/String;Ljava/lang/Double;>;");
//    }
}
