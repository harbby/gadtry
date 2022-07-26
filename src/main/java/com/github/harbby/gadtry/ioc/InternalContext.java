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
package com.github.harbby.gadtry.ioc;

import com.github.harbby.gadtry.base.Throwables;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

class InternalContext
{
    private final ThreadLocal<Set<Class<?>>> deps = ThreadLocal.withInitial(HashSet::new);
    private final BindMapping binds;

    InternalContext(BindMapping binds)
    {
        this.binds = binds;
    }

    public static InternalContext of(BindMapping binds)
    {
        return new InternalContext(binds);
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

    private <T> T getNewInstance(Class<T> driver)
    {
        try {
            final Constructor<T> constructor = selectConstructor(driver);
            constructor.setAccessible(true);

            List<Object> builder = new ArrayList<>();
            for (Class<?> argType : constructor.getParameterTypes()) {
                checkState(argType != driver && check(argType), "Found a circular dependency involving " + driver + ", and circular dependencies are disabled.");

                Object value = getInstance(argType);
                checkState(value != null, String.format("Could not find a suitable constructor in [%s]. Classes must have either one (and only one) constructor annotated with @Autowired or a constructor that is not private(and only one).", argType));
                builder.add(value);
            }

            T instance = constructor.newInstance(builder.toArray());
            return (T) buildAnnotationFields(driver, instance);
        }
        catch (InvocationTargetException e) {
            throw Throwables.throwThrowable(e.getTargetException());
        }
        catch (Exception e) {
            throw Throwables.throwThrowable(e);
        }
    }

    private static class StackSnapshot
    {
        private final Class<?> aClass;
        private Object value;
        private Constructor<?> constructor;
        private StackSnapshot[] childArr;

        private StackSnapshot(Class<?> aClass)
        {
            this.aClass = aClass;
        }
    }

    private <T> T getInstance(Class<T> driver)
    {
        StackSnapshot root = new StackSnapshot(driver);
        Deque<StackSnapshot> deque = new LinkedList<>();
        deque.add(root);

        StackSnapshot snapshot;
        while ((snapshot = deque.pollFirst()) != null) {
            Class<?> aClass = snapshot.aClass;
            Supplier<?> creator0 = binds.get(aClass);
            if (creator0 != null) {
                snapshot.value = creator0.get();
                continue;
            }
            if (snapshot.childArr == null) {
                Constructor<?> constructor = selectConstructor(aClass);
                constructor.setAccessible(true);
                int parameterCount = constructor.getParameterCount();
                if (parameterCount == 0) {
                    try {
                        snapshot.value = constructor.newInstance();
                    }
                    catch (IllegalAccessException | InstantiationException e) {
                        throw new InjectorException("newInstance class " + constructor.getName() + "failed", e);
                    }
                    catch (InvocationTargetException e) {
                        throw new InjectorException("newInstance class " + constructor.getName() + "failed", e.getTargetException());
                    }
                    continue;
                }

                deque.addFirst(snapshot);
                StackSnapshot[] childs = new StackSnapshot[parameterCount];
                int i = 0;
                for (Class<?> argType : constructor.getParameterTypes()) {
                    checkState(argType != aClass && check(argType), "Found a circular dependency involving %s, and circular dependencies are disabled.", aClass);
                    StackSnapshot child = new StackSnapshot(argType);
                    deque.addFirst(child);
                    childs[i++] = child;
                }
                snapshot.constructor = constructor;
                snapshot.childArr = childs;
            }
            else {
                Constructor<?> constructor = snapshot.constructor;
                Object[] values = new Object[constructor.getParameterCount()];
                for (int i = 0; i < values.length; i++) {
                    Object value = snapshot.childArr[i].value;
                    checkState(value != null, String.format("Could not find a suitable constructor in [%s]. " +
                            "Classes must have either one (and only one) constructor annotated " +
                            "with @Autowired or a constructor that is not private(and only one).", aClass));
                    values[i] = value;
                }
                Object instance;
                try {
                    instance = constructor.newInstance(values);
                }
                catch (IllegalAccessException | InstantiationException e) {
                    throw new InjectorException("newInstance class " + constructor.getName() + "failed", e);
                }
                catch (InvocationTargetException e) {
                    throw new InjectorException("newInstance class " + constructor.getName() + "failed", e.getTargetException());
                }
                snapshot.value = buildAnnotationFields(snapshot.aClass, instance);
            }
        }
        @SuppressWarnings("unchecked")
        T typeValue = (T) root.value;
        return typeValue;
    }

    private boolean check(Class<?> type)
    {
        return !deps.get().contains(type);
    }

    private Object buildAnnotationFields(Class<?> driver, Object instance)
    {
        for (Field field : driver.getDeclaredFields()) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (autowired != null) {
                field.setAccessible(true);
                Object value = field.getType() == driver ? instance : getInstance(field.getType());
                try {
                    field.set(instance, value);
                }
                catch (IllegalAccessException e) {
                    throw new InjectorException("injector @Autowired field " + field.getName() + "failed", e);
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> selectConstructor(Class<T> driver)
    {
        Constructor<T>[] constructors;
        if (Supplier.class.isAssignableFrom(driver)) {
            constructors = (Constructor<T>[]) driver.getDeclaredConstructors();
        }
        else {
            if (driver.isInterface() || Modifier.isAbstract(driver.getModifiers())) {
                throw new IllegalStateException(driver + " cannot be instantiated, No binding entity class");
            }
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
