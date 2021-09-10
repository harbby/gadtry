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
package com.github.harbby.gadtry.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static java.util.Objects.requireNonNull;

//java method warp
public class MethodSignature
{
    private final Method method;
    private final String name;

    public MethodSignature(Method method, String name)
    {
        this.method = requireNonNull(method, "method is null");
        this.name = requireNonNull(name, "name is null");
    }

    public static MethodSignature of(Method method)
    {
        return new MethodSignature(method, method.getName());
    }

    public Method getMethod()
    {
        return method;
    }

    public Object invoke(Object obj, Object... args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return method.invoke(obj, args);
    }

    public void setAccessible(boolean flag)
    {
        method.setAccessible(flag);
    }

    public Class<?> getDeclaringClass()
    {
        return method.getDeclaringClass();
    }

    public String getName()
    {
        return name;
    }

    public int getModifiers()
    {
        return method.getModifiers();
    }

    public TypeVariable<Method>[] getTypeParameters()
    {
        return method.getTypeParameters();
    }

    public Class<?> getReturnType()
    {
        return method.getReturnType();
    }

    public Type getGenericReturnType()
    {
        return method.getGenericReturnType();
    }

    public Class<?>[] getParameterTypes()
    {
        return method.getParameterTypes();
    }

    public int getParameterCount()
    {
        return method.getParameterCount();
    }

    public Type[] getGenericParameterTypes()
    {
        return method.getGenericParameterTypes();
    }

    public Class<?>[] getExceptionTypes()
    {
        return method.getExceptionTypes();
    }

    public Type[] getGenericExceptionTypes()
    {
        return method.getGenericExceptionTypes();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        MethodSignature other = (MethodSignature) obj;
        return this.method.equals(other.method);
    }

    @Override
    public int hashCode()
    {
        return method.hashCode();
    }

    @Override
    public String toString()
    {
        return method.toString();
    }

    public String toGenericString()
    {
        return method.toGenericString();
    }

    public boolean isBridge()
    {
        return method.isBridge();
    }

    public boolean isVarArgs()
    {
        return method.isVarArgs();
    }

    public boolean isSynthetic()
    {
        return method.isSynthetic();
    }

    public boolean isDefault()
    {
        return method.isDefault();
    }

    public Object getDefaultValue()
    {
        return method.getDefaultValue();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return method.getAnnotation(annotationClass);
    }

    public Annotation[] getDeclaredAnnotations()
    {
        return method.getDeclaredAnnotations();
    }

    public Annotation[][] getParameterAnnotations()
    {
        return method.getParameterAnnotations();
    }

    public AnnotatedType getAnnotatedReturnType()
    {
        return method.getAnnotatedReturnType();
    }

    public Parameter[] getParameters()
    {
        return method.getParameters();
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass)
    {
        return method.getAnnotationsByType(annotationClass);
    }

    public AnnotatedType getAnnotatedReceiverType()
    {
        return method.getAnnotatedReceiverType();
    }

    public AnnotatedType[] getAnnotatedParameterTypes()
    {
        return method.getAnnotatedParameterTypes();
    }

    public AnnotatedType[] getAnnotatedExceptionTypes()
    {
        return method.getAnnotatedExceptionTypes();
    }
//    since = "9"
//    public boolean trySetAccessible()
//    {
//        return method.trySetAccessible();
//    }

//    @Deprecated(since = "9")
//    public boolean isAccessible() {  return method.isAccessible();}
    //   since = "9"
//    public boolean canAccess(Object obj)
//    {
//        return method.canAccess(obj);
//    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
    {
        return method.isAnnotationPresent(annotationClass);
    }

    public Annotation[] getAnnotations()
    {
        return method.getAnnotations();
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass)
    {
        return method.getDeclaredAnnotation(annotationClass);
    }

    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass)
    {
        return method.getDeclaredAnnotationsByType(annotationClass);
    }
}
