/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.ioc;

import com.github.harbby.gadtry.function.Creator;
import com.github.harbby.gadtry.function.Function;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.harbby.gadtry.base.Checks.checkState;
import static com.github.harbby.gadtry.base.Throwables.noCatch;

class InternalContext
{
    private final ThreadLocal<Set<Class<?>>> deps = ThreadLocal.withInitial(HashSet::new);
    private final Function<Class<?>, ?> userCreator;
    private final BindMapping binds;

    private InternalContext(BindMapping binds, Function<Class<?>, ?> userCreator)
    {
        this.binds = binds;
        this.userCreator = userCreator;
    }

    public static InternalContext of(BindMapping binds, Function<Class<?>, ?> userCreator)
    {
        return new InternalContext(binds, userCreator);
    }

    public <T> T get(Class<T> driver)
    {
        Set<Class<?>> depCLass = deps.get();
        depCLass.clear();
        depCLass.add(driver);

        T t = getInstance(driver);
        depCLass.clear();
        return t;
    }

    public <T> T getByNew(Class<T> driver)
    {
        Set<Class<?>> depCLass = deps.get();
        depCLass.clear();
        depCLass.add(driver);

        T t = getNewInstance(driver);
        depCLass.clear();
        return t;
    }

    private <T> T getInstance(Class<T> driver)
    {
        return binds.getOrDefault(driver, () -> getNewInstance(driver)).get();
    }

    private <T> T getNewInstance(Class<T> driver)
    {
        return noCatch(() -> newInstance(driver));
    }

    private boolean check(Class<?> type)
    {
        return !deps.get().contains(type);
    }

    private <T> T newInstance(Class<T> driver)
            throws Exception
    {
        final Constructor<T> constructor = selectConstructor(driver);
        constructor.setAccessible(true);

        List<Object> builder = new ArrayList<>();
        for (Class<?> argType : constructor.getParameterTypes()) {
            checkState(argType != driver && check(argType), "Found a circular dependency involving " + driver + ", and circular dependencies are disabled.");

            Object userValue = userCreator.apply(argType);
            if (userValue == null) {
                Object value = getInstance(argType);
                checkState(value != null, String.format("Could not find a suitable constructor in [%s]. Classes must have either one (and only one) constructor annotated with @Autowired or a constructor that is not private(and only one).", argType));
                builder.add(value);
            }
            else {
                checkState(argType.isInstance(userValue));
                builder.add(userValue);
            }
        }

        T instance = constructor.newInstance(builder.toArray());
        return buildAnnotationFields(driver, instance);
    }

    private <T> T buildAnnotationFields(Class<T> driver, T instance)
            throws IllegalAccessException
    {
        for (Field field : driver.getDeclaredFields()) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (autowired != null) {
                field.setAccessible(true);
                if (field.getType() == driver) {
                    field.set(instance, instance);
                }
                else {
                    field.set(instance, getInstance(field.getType()));
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> selectConstructor(Class<T> driver)
    {
        Constructor<T>[] constructors;
        if (Creator.class.isAssignableFrom(driver)) {
            constructors = (Constructor<T>[]) driver.getDeclaredConstructors();
        }
        else {
            checkState(!driver.isInterface(), driver + " is Interface, cannot be instantiated");
            constructors = (Constructor<T>[]) driver.getConstructors(); //public
        }

        Constructor<T> noParameter = null;
        for (Constructor<T> constructor : constructors) {
            Autowired autowired = constructor.getAnnotation(Autowired.class);
            if (autowired != null) {
                return constructor;
            }
            if (constructor.getParameterCount() == 0) {
                //find 'no parameter' Constructor, using class.newInstance()";
                noParameter = constructor;
            }
        }

        if (noParameter != null) {
            return noParameter;
        }

        checkState(constructors.length == 1, String.format("%s has multiple public constructors, please ensure that there is only one", driver));
        return constructors[0];
    }
}
