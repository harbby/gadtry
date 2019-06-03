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
package com.github.harbby.gadtry.aop.v1;

import com.github.harbby.gadtry.aop.model.Pointcut;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class FilterBuilderTest
{
    @Test
    public void filterTest1()
    {
        Pointcut pointcut = new Pointcut("test");
        FilterBuilder builder = new FilterBuilder(pointcut);
        builder.classes(Set.class, Map.class)
                .returnType(boolean.class)
                .whereMethod(methodInfo -> methodInfo.getName().startsWith("add"))
                .build();

        Set<Class<?>> classes = pointcut.getSearchClass();
        Assert.assertEquals(1, classes.size());
    }
}
