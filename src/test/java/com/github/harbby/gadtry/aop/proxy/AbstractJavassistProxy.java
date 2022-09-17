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
package com.github.harbby.gadtry.aop.proxy;

import com.github.harbby.gadtry.collection.MutableList;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

abstract class AbstractJavassistProxy
        extends AbstractProxy
{
    @Override
    protected byte[] generate(
            ClassLoader classLoader,
            String className,
            Class<?> superclass,
            Set<Class<?>> interfaceSet,
            Collection<Method> proxyMethods)
            throws Exception
    {
        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(classLoader));

        // New Create Proxy Class
        CtClass proxyClass = classPool.makeClass(className);
        CtClass parentClass = classPool.get(superclass.getName());

        // add parent Class
        if (superclass.isInterface()) {
            proxyClass.addInterface(parentClass);
        }
        else {
            checkState(!java.lang.reflect.Modifier.isFinal(superclass.getModifiers()), superclass + " is final");
            proxyClass.setSuperclass(parentClass);
        }

        final Set<CtClass> ctInterfaces = new HashSet<>();
        ctInterfaces.add(parentClass);
        for (Class<?> it : interfaceSet) {
            CtClass ctClass = classPool.get(it.getName());
            ctInterfaces.add(ctClass);
            proxyClass.addInterface(ctClass);
        }
        // add ProxyHandler Interface
        addProxyHandlerInterface(classPool, proxyClass);
        // add field and method
        addFieldAndMethod(proxyClass, ctInterfaces);
        // set class flag
        proxyClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);

        //proxyClass.writeFile("out/.");
        return proxyClass.toBytecode();
    }

    private void addFieldAndMethod(CtClass proxyClass, Collection<CtClass> ctInterfaces)
            throws NotFoundException, CannotCompileException
    {
        Map<CtMethod, String> methods = new IdentityHashMap<>();
        MutableList.Builder<CtMethod> builder = MutableList.builder();
        //---------------------
        for (CtClass ctClass : ctInterfaces) {
            builder.addAll(ctClass.getMethods());
            builder.addAll(ctClass.getDeclaredMethods());
        }
        List<CtMethod> methodList = builder.build();
        for (CtMethod ctMethod : methodList) {
            //final or private or static function not proxy
            boolean checked = Modifier.isFinal(ctMethod.getModifiers()) ||
                    Modifier.isPrivate(ctMethod.getModifiers()) ||
                    Modifier.isStatic(ctMethod.getModifiers());
            if (!checked) {
                methods.put(ctMethod, "");
            }
        }

        int methodIndex = 0;
        for (CtMethod ctMethod : methods.keySet()) {
            final String methodFieldName = "_method" + methodIndex++;
            addSupperMethod(proxyClass, ctMethod, methodFieldName);
        }
    }

    protected abstract void addSupperMethod(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException;

    static void addProxyHandlerInterface(ClassPool classPool, CtClass proxyClass)
            throws NotFoundException, CannotCompileException
    {
        CtClass proxyHandler = classPool.get(ProxyAccess.class.getName());
        proxyClass.addInterface(proxyHandler);

        CtField handlerField = CtField.make("private java.lang.reflect.InvocationHandler handler;", proxyClass);
        proxyClass.addField(handlerField);

        //Add Method setHandler
        addMethod(proxyClass, proxyHandler.getDeclaredMethod("setHandler"), "this.handler = $1;");
        //Add Method getHandler
        addMethod(proxyClass, proxyHandler.getDeclaredMethod("getHandler"), "return this.handler;");
        //callRealMethod
        addMethod(proxyClass, proxyHandler.getDeclaredMethod("callRealMethod"), "return $1.invoke($2, $3);");
    }

    static void addMethod(CtClass proxy, CtMethod parentMethod, String methodBody)
            throws NotFoundException, CannotCompileException
    {
        int mod = Modifier.FINAL | parentMethod.getModifiers();
        if (Modifier.isNative(mod)) {
            mod = mod & ~Modifier.NATIVE;
        }

        CtMethod proxyMethod = new CtMethod(parentMethod.getReturnType(), parentMethod.getName(), parentMethod.getParameterTypes(), proxy);
        proxyMethod.setModifiers(mod);
        proxyMethod.setBody(methodBody);

        //add Override
        Annotation annotation = new Annotation(Override.class.getName(), proxyMethod.getMethodInfo().getConstPool());
        AnnotationsAttribute attribute = new AnnotationsAttribute(proxyMethod.getMethodInfo().getConstPool(), AnnotationsAttribute.visibleTag);
        attribute.addAnnotation(annotation);
        proxyMethod.getMethodInfo().addAttribute(attribute);
        proxy.addMethod(proxyMethod);
    }

    /**
     * add no Parameter Constructor
     */
    static void addVoidConstructor(CtClass proxy)
            throws CannotCompileException
    {
        CtConstructor ctConstructor = new CtConstructor(new CtClass[] {}, proxy);
        ctConstructor.setBody(";");
        proxy.addConstructor(ctConstructor); //or proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
    }
}
