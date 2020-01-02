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

import com.github.harbby.gadtry.function.Function1;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

public class GenericTest
{
    public static class GenericClass
            extends ArrayList<String>
    {
    }

    public static class GenericInterfaceClass
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

    //范型测试
    @Test
    public void giveGenericClassReturnStringClassBy1()
    {
        Class<?> a1 = GenericClass.class;
        ParameterizedType parameterizedType = (ParameterizedType) a1.getGenericSuperclass();
        Type[] types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(types[0], String.class);
    }

    @Test
    public void giveGenericInterfaceClassReturnMapClassBy1()
    {
        Type a1 = GenericInterfaceClass.class.getGenericInterfaces()[0];

        ParameterizedType parameterizedType = (ParameterizedType) a1;
        Type[] types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(types[0], JavaTypes.makeMapType(Map.class, String.class, Integer.class));
    }
}
