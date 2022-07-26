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

import com.github.harbby.gadtry.Beta;
import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.base.Try;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.compiler.CompileError;
import javassist.compiler.Lex;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Beta
final class JavassistProxyV2
        extends AbstractJavassistProxy
{
    public static final String METHOD_START = "$_";
    static final JavassistProxyV2 javassistProxyV2 = new JavassistProxyV2();

    private JavassistProxyV2() {}

    /**
     * public final int getAge() {
     * return (Integer)this.handler.invoke(this, _method5, new Object[0]);
     * }
     * <p>
     * public int $_getAge() {
     * return super.getAge();
     * }
     */
    @Override
    protected void addSupperMethod(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException
    {
        boolean access = checkMethod(ctMethod);
        if (access) {
            JavassistProxy.addSupperMethodV1(proxyClass, ctMethod, methodNewName);
        }
        else {
            addSupperMethodV2(proxyClass, ctMethod, methodNewName);
        }
    }

    private static void addSupperMethodV2(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException
    {
        // 添加字段
        String fieldCode = "public static final java.lang.reflect.Method %s = " +
                "%s.class.getDeclaredMethod(\"" + METHOD_START + "%s\", %s);";

        String arg = null;
        if (ctMethod.getParameterTypes().length != 0) {
            arg = "new Class[] {"
                    + Stream.of(ctMethod.getParameterTypes()).map(x -> x.getName() + ".class").collect(Collectors.joining(","))
                    + "}";
        }
        String fieldSrc = String.format(fieldCode, methodNewName, proxyClass.getName(), ctMethod.getName(), arg);

        CtField ctField;
        try {
            ctField = CtField.make(fieldSrc, proxyClass);
        }
        catch (CannotCompileException e) {
            if (e.getCause() instanceof CompileError) {
                Lex lex = ((CompileError) e.getCause()).getLex();
                StringBuffer stringBuffer = Try.noCatch(() -> {
                    Field field = Lex.class.getDeclaredField("textBuffer");
                    field.setAccessible(true);
                    return (StringBuffer) field.get(lex);
                });
                throw new MockGoException("package name [" + proxyClass.getPackageName() + "] find exists JAVA system keywords " + stringBuffer, e);
            }
            throw e;
        }

        proxyClass.addField(ctField);

        String code = "return ($r) this.handler.invoke(this, %s, $args);";
        String methodBodySrc = String.format(code, methodNewName);
        addMethod(proxyClass, ctMethod, methodBodySrc);
        //------add _Method()---------- // ctMethod.getLongName();
        CtMethod m2 = new CtMethod(ctMethod.getReturnType(), METHOD_START + ctMethod.getName(), ctMethod.getParameterTypes(), proxyClass);
        ctMethod.getMethodInfo().getAttributes().forEach(m2.getMethodInfo()::addAttribute); //add @注解
        m2.setBody("return ($r) super." + ctMethod.getName() + "($$);");
        proxyClass.addMethod(m2);
    }

    private boolean checkMethod(CtMethod method)
    {
        return method.getDeclaringClass().isInterface()    //接口的方法或default方法无法super.()调用
                || Modifier.isAbstract(method.getModifiers());   //Abstract方法也无法被super.()调用
    }
}
