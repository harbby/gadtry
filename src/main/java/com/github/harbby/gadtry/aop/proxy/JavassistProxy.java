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

import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.base.Try;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.compiler.CompileError;
import javassist.compiler.Lex;

import java.lang.reflect.Field;

final class JavassistProxy
        extends AbstractJavassistProxy
        implements ProxyFactory
{
    static final JavassistProxy javassistProxy = new JavassistProxy();
    private static final Field LEX_TEXT_BUFFER_FIELD;

    private JavassistProxy() {}

    static {
        try {
            LEX_TEXT_BUFFER_FIELD = Lex.class.getDeclaredField("textBuffer");
            LEX_TEXT_BUFFER_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("unknown Javassist version");
        }
    }

    @Override
    protected String proxyClassNameFlag()
    {
        return "GadtryJssAop";
    }

    @Override
    protected void addSupperMethod(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException
    {
        addSupperMethodV1(proxyClass, ctMethod, methodNewName);
    }

    static void addSupperMethodV1(CtClass proxyClass, CtMethod ctMethod, String methodNewName)
            throws NotFoundException, CannotCompileException
    {
        String fieldCode = "public static final java.lang.reflect.Method %s = " +
                "javassist.util.proxy.RuntimeSupport.findMethod(%s.class, \"%s\", \"%s\");";
        String fieldSrc = String.format(fieldCode, methodNewName, ctMethod.getDeclaringClass().getName(), ctMethod.getName(), ctMethod.getSignature());
        CtField ctField;
        try {
            ctField = CtField.make(fieldSrc, proxyClass);
        }
        catch (CannotCompileException e) {
            if (e.getCause() instanceof CompileError) {
                Lex lex = ((CompileError) e.getCause()).getLex();
                StringBuffer stringBuffer = Try.noCatch(() -> (StringBuffer) LEX_TEXT_BUFFER_FIELD.get(lex));
                String message = String.format("package name [%s] find exists JAVA system keywords %s", proxyClass.getPackageName(), stringBuffer.toString());
                throw new MockGoException(message, e);
            }
            throw e;
        }

        proxyClass.addField(ctField);

        String code = "return ($r) this.handler.invoke(this, %s, $args);";
        String methodBodySrc = String.format(code, methodNewName);
        addMethod(proxyClass, ctMethod, methodBodySrc);
    }
}
