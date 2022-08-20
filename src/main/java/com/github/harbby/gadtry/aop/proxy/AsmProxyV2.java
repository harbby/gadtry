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

import com.github.harbby.gadtry.base.JavaTypes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

public final class AsmProxyV2
        extends AbstractAsmProxy
{
    private static final String METHOD_STARTS_WITH = "$_";
    static final AsmProxyV2 asmProxyV2 = new AsmProxyV2();

    private AsmProxyV2() {}

    @Override
    protected Class<?> getHandlerClass()
    {
        return InvocationHandler.class;
    }

    @Override
    protected String proxyClassNameFlag()
    {
        return "GadtryAsmV2Aop";
    }

    @Override
    protected void addProxyMethod(ClassWriter classWriter, String className, Method method,
            String bindName, Class<?> superclass, int methodId)
    {
        String descriptor = Type.getMethodDescriptor(method);
        super.addProxyMethod(classWriter, className, method, bindName, superclass, methodId);
        String methodName = METHOD_STARTS_WITH + method.getName();
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, methodName, descriptor, null, null);
        mv.visitCode();
        if (Modifier.isAbstract(method.getModifiers())) {
            this.addAbstractMethod(mv, method);
        }
        else {
            this.addSupperMethod(mv, superclass, method, descriptor);
        }
        mv.visitEnd();
    }

    @Override
    protected int putStaticField(MethodVisitor methodVisitor, Method method, String className, String bindName)
    {
        Type type = Type.getType("L" + className + ";");
        boolean isPublic = Modifier.isPublic(method.getModifiers());
        return this.initStaticField(methodVisitor, type,
                METHOD_STARTS_WITH + method.getName(),
                method.getParameterTypes(),
                className, bindName, isPublic);
    }

    private void addAbstractMethod(MethodVisitor mv, Method method)
    {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            mv.visitInsn(RETURN);
        }
        else {
            if (returnType.isPrimitive()) {
                pushClass(mv, returnType);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(JavaTypes.class),
                        "getClassInitValue", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                Type type = Type.getType(returnType);
                Class<?> wrapper = JavaTypes.getWrapperClass(returnType);
                String internalName = Type.getInternalName(wrapper);
                mv.visitTypeInsn(CHECKCAST, internalName);
                mv.visitMethodInsn(INVOKEVIRTUAL, internalName,
                        returnType.getName() + "Value", "()" + Type.getDescriptor(returnType), false);
                mv.visitInsn(type.getOpcode(IRETURN));
            }
            else {
                mv.visitInsn(ACONST_NULL);
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
                mv.visitInsn(ARETURN);
            }
        }
        int varIndex = 1;
        for (Class<?> parameterType : method.getParameterTypes()) {
            Type type = Type.getType(parameterType);
            varIndex += type.getSize();
        }
        mv.visitMaxs(1, varIndex);
    }

    private void addSupperMethod(MethodVisitor methodVisitor, Class<?> superclass, Method method, String descriptor)
    {
        methodVisitor.visitVarInsn(ALOAD, 0); // this
        Class<?>[] parameterTypes = method.getParameterTypes();
        int varIndex = 1;
        for (Class<?> parameterType : parameterTypes) {
            Type type = Type.getType(parameterType);
            int loadCmd = type.getOpcode(ILOAD);
            methodVisitor.visitVarInsn(loadCmd, varIndex);
            varIndex += type.getSize();
        }
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?> ownerClass = declaringClass.isAssignableFrom(superclass) ? superclass : declaringClass;
        String ownerName = Type.getInternalName(ownerClass);
        boolean isInterface = ownerClass.isInterface();
        methodVisitor.visitMethodInsn(INVOKESPECIAL, ownerName, method.getName(), descriptor, isInterface);
        Class<?> returnType = method.getReturnType();
        Type type = Type.getType(returnType);
        int opCode = type.getOpcode(IRETURN);
        methodVisitor.visitInsn(opCode);
        methodVisitor.visitMaxs(varIndex, varIndex);
    }
}
