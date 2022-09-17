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
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Platform;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.github.harbby.gadtry.aop.proxy2.AsmUtil.pushClass;
import static com.github.harbby.gadtry.aop.proxy2.AsmUtil.pushIntNumber;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public abstract class AbstractAsmProxy
        extends AbstractProxy
{
    protected void addProxyMethod(ClassWriter classWriter, String className, Method method,
            String bindName, Class<?> superclass, int methodId)
    {
        String descriptor = Type.getMethodDescriptor(method);
        int access = method.getModifiers() & 0x07; //Modifier.isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
        MethodVisitor methodVisitor = classWriter.visitMethod(access, method.getName(), descriptor, null, null);
        AnnotationVisitor annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
        annotationVisitor0.visitEnd();
        addMethodAnnotation(methodVisitor, method);

        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, className, "handler", "Ljava/lang/reflect/InvocationHandler;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETSTATIC, className, bindName, "Ljava/lang/reflect/Method;");
        int parameterCount = method.getParameterCount();
        int varIndex = 1;
        int maxStack;  //this maxStack is 3;
        if (parameterCount == 0) {
            //methodVisitor.visitInsn(ICONST_0);
            //methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
            //methodVisitor.visitFieldInsn(GETSTATIC, "com/github/harbby/gadtry/aop/proxy/Proxy", "EMPTY", "[Ljava/lang/Object;");
            maxStack = 4;
        }
        else {
            pushIntNumber(methodVisitor, parameterCount);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            maxStack = 4;
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterCount; i++) {
                methodVisitor.visitInsn(DUP); //maxStack++
                int stack = 5; // 4
                Class<?> parameterType = parameterTypes[i];
                Type type = Type.getType(parameterType);
                int loadCmd = type.getOpcode(ILOAD);
                if (parameterType.isPrimitive()) {
                    String wrapper = Type.getInternalName(JavaTypes.getWrapperClass(parameterType));
                    pushIntNumber(methodVisitor, i);  //stack+=1
                    methodVisitor.visitVarInsn(loadCmd, varIndex); //stack+=typeSize
                    stack = stack + 1 + type.getSize();
                    methodVisitor.visitMethodInsn(INVOKESTATIC, wrapper, "valueOf",
                            String.format("(%s)L%s;", Type.getDescriptor(parameterType), wrapper), false);
                    methodVisitor.visitInsn(AASTORE);
                }
                else {
                    pushIntNumber(methodVisitor, i);
                    methodVisitor.visitVarInsn(ALOAD, varIndex);
                    stack += 2;
                    methodVisitor.visitInsn(AASTORE);
                }
                varIndex += type.getSize();
                maxStack = Math.max(maxStack, stack);
            }
        }
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke",
                "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);
        addReturn(methodVisitor, method);
        int maxLocals = varIndex;
        methodVisitor.visitMaxs(maxStack, maxLocals);
        methodVisitor.visitEnd();
    }

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

    protected abstract Set<Class<?>> defaultSuperInterface();

    protected byte[] generate(ClassLoader classLoader, String classFullName, Class<?> superclass,
            Set<Class<?>> interfaceSet, Collection<Method> proxyMethods)
    {
        String className = classFullName.replace('.', '/');
        ClassWriter classWriter = new ClassWriter(0); // ClassWriter.COMPUTE_MAXS
        String superName;
        Set<String> interfaceList = new HashSet<>(interfaceSet.size() + 3);
        if (superclass.isInterface()) {
            interfaceList.add(Type.getInternalName(superclass));
            superName = Type.getInternalName(Object.class);
        }
        else {
            superName = Type.getInternalName(superclass);
        }
        for (Class<?> interfaceClass : defaultSuperInterface()) {
            interfaceList.add(Type.getInternalName(interfaceClass));
        }
        for (Class<?> it : interfaceSet) {
            interfaceList.add(Type.getInternalName(it));
        }
        int version = Platform.getClassVersion();
        classWriter.visit(version, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, className, null,
                superName, interfaceList.toArray(new String[0]));
        //classWriter.visitSource(simpleName + ".java", null);

        // add handler field and method
        addHandler(classWriter, className, this.getHandlerClass());
        addCallRealMethod(classWriter);
        //add proxy method
        createMethods(classWriter, className, proxyMethods, superclass);
        //add init Constructor
        for (Constructor<?> constructor : superclass.getConstructors()) {
            addInitConstructor(classWriter, constructor);
        }
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    protected void addMethodAnnotation(MethodVisitor methodVisitor, Executable executable)
    {
        try {
            for (Annotation annotation : executable.getAnnotations()) {
                String annDesc = Type.getDescriptor(annotation.annotationType());
                AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation(annDesc, true);
                addAnnotation(annotationVisitor, annotation);
                annotationVisitor.visitEnd();
            }
            int i = 0;
            for (Annotation[] annotationArr : executable.getParameterAnnotations()) {
                for (Annotation annotation : annotationArr) {
                    String argAnnDesc = Type.getDescriptor(annotation.annotationType());
                    AnnotationVisitor annotationVisitor = methodVisitor.visitParameterAnnotation(i, argAnnDesc, true);
                    addAnnotation(annotationVisitor, annotation);
                    annotationVisitor.visitEnd();
                }
                i++;
            }
        }
        catch (Exception e) {
            throw new MockGoException("add method annotation failed", e);
        }
    }

    protected abstract Class<?> getHandlerClass();

    private void addAnnotation(AnnotationVisitor annotationVisitor, Annotation annotation)
            throws InvocationTargetException, IllegalAccessException
    {
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Object defaultValue = method.getDefaultValue();
            Object value = method.invoke(annotation); //not null
            if (!value.equals(defaultValue)) {
                annotationVisitor.visit(method.getName(), value);
            }
        }
    }

    private void addInitConstructor(ClassWriter classWriter, Constructor<?> supperConstructor)
    {
        String descriptor = Type.getConstructorDescriptor(supperConstructor);
        Class<?> aClass = supperConstructor.getDeclaringClass();
        String internalName = Type.getInternalName(aClass);

        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        addMethodAnnotation(methodVisitor, supperConstructor);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0); // this
        Class<?>[] parameterTypes = supperConstructor.getParameterTypes();
        int varIndex = 1;
        for (Class<?> parameterType : parameterTypes) {
            Type type = Type.getType(parameterType);
            int loadCmd = type.getOpcode(ILOAD);
            methodVisitor.visitVarInsn(loadCmd, varIndex);
            varIndex += type.getSize();
        }

        methodVisitor.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", descriptor, false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(varIndex, varIndex);
        methodVisitor.visitEnd();
    }

    private static void addField(ClassWriter classWriter, int access, String name, String descriptor)
    {
        FieldVisitor fieldVisitor = classWriter.visitField(access, name, descriptor, null, null);
        fieldVisitor.visitEnd();
    }

    private static void addHandler(ClassWriter classWriter, String className, Class<?> handlerClass)
    {
        addField(classWriter, ACC_PRIVATE, "handler", Type.getDescriptor(handlerClass));
        String descriptor = Type.getDescriptor(handlerClass);
        AnnotationVisitor annotationVisitor0;
        MethodVisitor methodVisitor;
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_FINAL, "setHandler", "(" + descriptor + ")V", null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, className, "handler", descriptor);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_FINAL, "getHandler", "()" + descriptor, null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, className, "handler", descriptor);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
    }

    private void addCallRealMethod(ClassWriter classWriter)
    {
        AnnotationVisitor annotationVisitor0;
        MethodVisitor methodVisitor;
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_FINAL, "callRealMethod", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(3, 4);
            methodVisitor.visitEnd();
        }
    }

    protected void addReturn(MethodVisitor methodVisitor, Method method)
    {
        Class<?> typeClass = method.getReturnType();
        if (typeClass == void.class) {
            methodVisitor.visitInsn(POP);
            methodVisitor.visitInsn(RETURN);
        }
        else if (typeClass.isPrimitive()) {
            Type type = Type.getType(typeClass);
            Class<?> wrapper = JavaTypes.getWrapperClass(typeClass);
            String name = typeClass.getName() + "Value";
            String internalName = Type.getInternalName(wrapper);
            String descriptor = "()" + Type.getDescriptor(typeClass);
            methodVisitor.visitTypeInsn(CHECKCAST, internalName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, internalName, name, descriptor, false);
            methodVisitor.visitInsn(type.getOpcode(IRETURN));
        }
        else {
            methodVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(typeClass));
            methodVisitor.visitInsn(ARETURN);
        }
    }

    protected int initStaticField(MethodVisitor methodVisitor,
            Type type,
            String methodName,
            Class<?>[] parameterTypes,
            String className,
            String bindName,
            boolean isPublic)
    {
        int stack;
        methodVisitor.visitLdcInsn(type);  // stack = 1
        methodVisitor.visitLdcInsn(methodName);  // stack = 2
        pushIntNumber(methodVisitor, parameterTypes.length);  // stack = 3
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        stack = 3;
        for (int i = 0; i < parameterTypes.length; i++) {
            methodVisitor.visitInsn(DUP);   // stack = 4
            Class<?> arg = parameterTypes[i];
            pushIntNumber(methodVisitor, i); // stack = 5
            pushClass(methodVisitor, arg); // stack = 6
            stack = 6;
            methodVisitor.visitInsn(AASTORE);
        }
        String invokeName = isPublic ? "getMethod" : "getDeclaredMethod";
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", invokeName, "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
        //methodVisitor.visitLdcInsn(Type.getMethodDescriptor(method));
        //methodVisitor.visitMethodInsn(INVOKESTATIC, "javassist/util/proxy/RuntimeSupport", "findMethod", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/reflect/Method;", false);
        methodVisitor.visitFieldInsn(PUTSTATIC, className, bindName, "Ljava/lang/reflect/Method;");
        return stack;
    }

    private void createMethods(ClassWriter classWriter, String className, Collection<Method> proxyMethods, Class<?> superclass)
    {
        MethodVisitor staticInitMv = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        // begin try catch
        staticInitMv.visitCode();
        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        staticInitMv.visitTryCatchBlock(label0, label1, label2, "java/lang/NoSuchMethodException");
        staticInitMv.visitLabel(label0);
        // try code...
        int i = 0;
        int staticInitMaxStack = 0;
        for (Method method : proxyMethods) {
            final String methodFieldName = "m" + i;
            //1.
            addField(classWriter, ACC_PUBLIC | ACC_FINAL | ACC_STATIC, methodFieldName, "Ljava/lang/reflect/Method;");
            //2. add method
            addProxyMethod(classWriter, className, method, methodFieldName, superclass, i);
            //3. add static put
            int stack = putStaticField(staticInitMv, method, className, methodFieldName);
            staticInitMaxStack = Math.max(staticInitMaxStack, stack);
            i++;
        }
        // catch list ...
        staticInitMv.visitLabel(label1);
        Label label3 = new Label();
        staticInitMv.visitJumpInsn(GOTO, label3);
        staticInitMv.visitLabel(label2);
        staticInitMv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/NoSuchMethodException"});
        staticInitMv.visitVarInsn(ASTORE, 0);
        staticInitMv.visitTypeInsn(NEW, "java/lang/NoSuchMethodError");
        staticInitMv.visitInsn(DUP);
        staticInitMv.visitVarInsn(ALOAD, 0);
        staticInitMv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NoSuchMethodException", "getMessage", "()Ljava/lang/String;", false);
        staticInitMv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoSuchMethodError", "<init>", "(Ljava/lang/String;)V", false);
        staticInitMv.visitInsn(ATHROW);
        staticInitMv.visitLabel(label3);
        staticInitMv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        // end
        staticInitMv.visitInsn(RETURN);
        staticInitMv.visitMaxs(staticInitMaxStack, 1);
        staticInitMv.visitEnd();
    }
}
