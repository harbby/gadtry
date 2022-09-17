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
package com.github.harbby.gadtry.aop.proxy2;

import com.github.harbby.gadtry.aop.proxy.AbstractAsmProxy;
import com.github.harbby.gadtry.aop.proxy.AsmProxyV2;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.collection.ImmutableSet;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.aop.proxy.AsmProxyV2.visitArgsInsn;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class AsmProxyV3
        extends AbstractAsmProxy
{
    public static final AsmProxyV3 asmProxyV3 = new AsmProxyV3();
    private static final Method LAMBDA_META_FACTORY;

    @Override
    protected Set<Class<?>> defaultSuperInterface()
    {
        return ImmutableSet.of(MockAccess.class, Serializable.class);
    }

    @Override
    protected boolean enableHiddenClass()
    {
        return false;
    }

    @Override
    protected int putStaticField(MethodVisitor methodVisitor, Method method, String className, String bindName)
    {
        Type type = Type.getType(method.getDeclaringClass());
        boolean isPublic = Modifier.isPublic(method.getModifiers());
        return this.initStaticField(methodVisitor, type,
                method.getName(),
                method.getParameterTypes(),
                className, bindName,
                isPublic);
    }

    @Override
    protected Class<?> getHandlerClass()
    {
        return Interceptor.class;
    }

    @Override
    protected String proxyClassNameFlag()
    {
        return "GadtryAsmV3Aop";
    }

    @Override
    protected void addProxyMethod(ClassWriter classWriter, String className, Method method, String bindName, Class<?> superclass, int methodId)
    {
        String descriptor = Type.getMethodDescriptor(method);
        int access = method.getModifiers() & 0x07; //Modifier.isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
        MethodVisitor methodVisitor = classWriter.visitMethod(access, method.getName(), descriptor, null, null);
        AnnotationVisitor annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
        annotationVisitor0.visitEnd();
        addMethodAnnotation(methodVisitor, method);

        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, className, "handler", Type.getDescriptor(Interceptor.class));
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETSTATIC, className, bindName, "Ljava/lang/reflect/Method;");
        int maxStack = 3;  //this maxStack is 3;
        int childStack = this.addLambda(classWriter, methodVisitor, method, className, superclass, methodId);
        String owner = Type.getInternalName(Interceptor.class);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, owner, "invoke",
                "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/util/concurrent/Callable;)Ljava/lang/Object;", true);
        addReturn(methodVisitor, method);
        methodVisitor.visitMaxs(maxStack + childStack, childStack);
        methodVisitor.visitEnd();
    }

    private int addLambda(ClassWriter classWriter, MethodVisitor mv, Method method, String className, Class<?> superclass, int methodId)
    {
        String lambdaName = "lambda$" + method.getName() + "$" + methodId;
        String argsDesc = Stream.of(method.getParameterTypes()).map(Type::getDescriptor).collect(Collectors.joining());
        String lambdaDesc = "(" + argsDesc + ")" + Type.getDescriptor(Object.class);
        // add lambda function
        mv.visitVarInsn(ALOAD, 0);
        int varIndex = visitArgsInsn(mv, method);
        if (Modifier.isAbstract(method.getModifiers())) {
            this.addAbstractMethodLambdaFunction(classWriter, method, lambdaName, lambdaDesc, superclass);
        }
        else {
            this.addLambdaFunction(classWriter, method, lambdaName, lambdaDesc, superclass);
        }
        // add call
        mv.visitInvokeDynamicInsn("call", String.format("(L%s;%s)Ljava/util/concurrent/Callable;", className, argsDesc),
                new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(LambdaMetafactory.class),
                        LAMBDA_META_FACTORY.getName(),
                        Type.getMethodDescriptor(LAMBDA_META_FACTORY), false),
                Type.getType("()Ljava/lang/Object;"),
                new Handle(Opcodes.H_INVOKESPECIAL, className, lambdaName, lambdaDesc, false),
                Type.getType("()Ljava/lang/Object;"));
        return varIndex;
    }

    private void addAbstractMethodLambdaFunction(ClassWriter classWriter, Method method, String lambdaName, String lambdaDesc, Class<?> superclass)
    {
        Class<?> returnType = method.getReturnType();
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_SYNTHETIC, lambdaName,
                lambdaDesc, null, new String[] {"java/lang/Exception"});
        methodVisitor.visitCode();
        int varIndex = 1;
        for (Class<?> parameterType : method.getParameterTypes()) {
            Type type = Type.getType(parameterType);
            varIndex += type.getSize();
        }

        if (returnType == void.class) {
            methodVisitor.visitInsn(ACONST_NULL);
        }
        else {
            AsmUtil.pushDefaultObjectValue(methodVisitor, returnType);
        }
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(1, varIndex);
        methodVisitor.visitEnd();
    }

    private void addLambdaFunction(ClassWriter classWriter, Method method, String lambdaName, String lambdaDesc, Class<?> superclass)
    {
        Class<?> returnType = method.getReturnType();
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_SYNTHETIC, lambdaName,
                lambdaDesc, null, new String[] {"java/lang/Exception"});
        methodVisitor.visitCode();
        int varIndex = AsmProxyV2.addSupperMethod(methodVisitor, superclass, method, Type.getMethodDescriptor(method));

        if (returnType == void.class) {
            methodVisitor.visitInsn(ACONST_NULL);
        }
        else if (returnType.isPrimitive()) {
            String wrapper = Type.getInternalName(JavaTypes.getWrapperClass(returnType));
            methodVisitor.visitMethodInsn(INVOKESTATIC, wrapper, "valueOf",
                    String.format("(%s)L%s;", Type.getDescriptor(returnType), wrapper),
                    false);
        }
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(varIndex, varIndex);
        methodVisitor.visitEnd();
    }

    static {
        try {
            LAMBDA_META_FACTORY = LambdaMetafactory.class.getMethod("metafactory", MethodHandles.Lookup.class, String.class,
                    MethodType.class, MethodType.class, MethodHandle.class, MethodType.class);
        }
        catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
}
