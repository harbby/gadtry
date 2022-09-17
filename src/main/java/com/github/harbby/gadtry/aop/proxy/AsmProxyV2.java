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
import com.github.harbby.gadtry.collection.ImmutableSet;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import static com.github.harbby.gadtry.aop.proxy2.AsmUtil.pushClass;
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

    protected Set<Class<?>> defaultSuperInterface()
    {
        return ImmutableSet.of(ProxyAccess.class, Serializable.class);
    }

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
        //ACC_SYNTHETIC
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, methodName, descriptor, null, null);
        mv.visitCode();
        if (Modifier.isAbstract(method.getModifiers())) {
            this.addAbstractMethod(mv, method);
        }
        else {
            int varIndex = addSupperMethod(mv, superclass, method, descriptor);
            Type type = Type.getType(method.getReturnType());
            mv.visitInsn(type.getOpcode(IRETURN));
            mv.visitMaxs(varIndex, varIndex);
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
        int maxStack;
        if (returnType == void.class) {
            maxStack = 0;
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
                maxStack = type.getSize();
            }
            else {
                mv.visitInsn(ACONST_NULL);
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
                mv.visitInsn(ARETURN);
                maxStack = 1;
            }
        }
        int varIndex = 1;
        for (Class<?> parameterType : method.getParameterTypes()) {
            Type type = Type.getType(parameterType);
            varIndex += type.getSize();
        }
        mv.visitMaxs(maxStack, varIndex);
    }

    public static int addSupperMethod(MethodVisitor methodVisitor, Class<?> superclass, Method method, String descriptor)
    {
        methodVisitor.visitVarInsn(ALOAD, 0); // this
        int varIndex = visitArgsInsn(methodVisitor, method);
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?> ownerClass = declaringClass.isAssignableFrom(superclass) ? superclass : declaringClass;
        String ownerName = Type.getInternalName(ownerClass);
        boolean isInterface = ownerClass.isInterface();
        methodVisitor.visitMethodInsn(INVOKESPECIAL, ownerName, method.getName(), descriptor, isInterface);
        return varIndex;
    }

    public static int visitArgsInsn(MethodVisitor mv, Method method)
    {
        int varIndex = 1;
        for (Class<?> parameterType : method.getParameterTypes()) {
            Type type = Type.getType(parameterType);
            mv.visitVarInsn(type.getOpcode(ILOAD), varIndex); //stack+=typeSize
            varIndex += type.getSize();
        }
        return varIndex;
    }
}
