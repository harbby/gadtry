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
package com.github.harbby.gadtry.aop.mock;

import com.github.harbby.gadtry.ioc.Bean;
import com.github.harbby.gadtry.ioc.IocFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.aop.mock.MockGo.mock;
import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;

public class MockAnnotations
{
    private MockAnnotations() {}

    public static void initMocks(Object testObject)
    {
        try {
            injectField(testObject);
        }
        catch (Exception e) {
            throwsThrowable(e);
        }
    }

    private static void injectField(Object instance)
            throws Exception
    {
        Field[] fields = instance.getClass().getDeclaredFields();
        List<Field> injectMocks = new ArrayList<>();
        List<Bean> beans = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);

            Mock mock = field.getAnnotation(Mock.class);
            if (mock != null) {
                Object m = mock(field.getType());
                field.set(instance, m);
                Bean bean = binder -> binder.bind((Class<? super Object>) field.getType(), m);
                beans.add(bean);
            }

            InjectMock injectMock = field.getAnnotation(InjectMock.class);
            if (injectMock != null) {
                injectMocks.add(field);
            }
        }

        IocFactory iocFactory = IocFactory.create(beans.toArray(new Bean[beans.size()]));
        for (Field field : injectMocks) {
            field.setAccessible(true);
            field.set(instance, iocFactory.getInstance(field.getType()));
        }
    }
}
