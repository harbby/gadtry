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
import com.github.harbby.gadtry.base.Platform;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;

public class AsmProxyClassBuilder
{
    private final Class<?> baseClass;
    private final Set<Class<?>> interfaceSet;
    private final String className;
    private final int version;
    private final Collection<Method> proxyMethods;

    public AsmProxyClassBuilder(String className,
            Class<?> baseClass,
            Set<Class<?>> interfaceSet,
            Collection<Method> proxyMethods)
    {
        this.baseClass = baseClass;
        this.interfaceSet = interfaceSet;
        this.className = className.replace('.', '/');
        this.version = Platform.getClassVersion();
        this.proxyMethods = proxyMethods;
    }

    private void addField(ClassWriter classWriter, int access, String name, String descriptor)
    {
        FieldVisitor fieldVisitor = classWriter.visitField(access, name, descriptor, null, null);
        fieldVisitor.visitEnd();
    }

    private void addHandler(ClassWriter classWriter, String className)
    {
        AnnotationVisitor annotationVisitor0;
        MethodVisitor methodVisitor;
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_FINAL, "setHandler", "(Ljava/lang/reflect/InvocationHandler;)V", null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, className, "handler", "Ljava/lang/reflect/InvocationHandler;");
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_FINAL, "getHandler", "()Ljava/lang/reflect/InvocationHandler;", null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, className, "handler", "Ljava/lang/reflect/InvocationHandler;");
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

    private void addProxyMethod(ClassWriter classWriter, String className, Method method, String bindName)
    {
        String descriptor = Type.getMethodDescriptor(method);
        int access = Modifier.isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
        MethodVisitor methodVisitor = classWriter.visitMethod(access, method.getName(), descriptor, null, null);
        {
            AnnotationVisitor annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
            annotationVisitor0.visitEnd();
        }
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
            methodVisitor.visitFieldInsn(GETSTATIC, "com/github/harbby/gadtry/aop/proxy/Proxy", "EMPTY", "[Ljava/lang/Object;");
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

    private static void pushIntNumber(MethodVisitor methodVisitor, int number)
    {
        if (number >= -1 && number <= 5) {
            // [-1,5] see: ICONST_M1, ICONST_0, ICONST_1, ICONST_2 , ... , ICONST_5
            methodVisitor.visitInsn(ICONST_0 + number);
        }
        else if (number >= Byte.MIN_VALUE && number <= Byte.MAX_VALUE) {
            // [-128~127]
            methodVisitor.visitIntInsn(BIPUSH, number);
        }
        else if (number >= Short.MIN_VALUE && number <= Short.MAX_VALUE) {
            methodVisitor.visitIntInsn(SIPUSH, number);
        }
        else {
            //number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE
            methodVisitor.visitIntInsn(LDC, number);
        }
    }

    private void addReturn(MethodVisitor methodVisitor, Method method)
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

    private void addInitConstructor(ClassWriter classWriter, Constructor<?> supperConstructor)
    {
        String descriptor = Type.getConstructorDescriptor(supperConstructor);
        Class<?> aClass = supperConstructor.getDeclaringClass();
        String internalName = Type.getInternalName(aClass);

        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0); // this
        for (int i = 0; i < supperConstructor.getParameterCount(); i++) {
            methodVisitor.visitVarInsn(ALOAD, i + 1);
        }

        methodVisitor.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", descriptor, false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, supperConstructor.getParameterCount());
        methodVisitor.visitEnd();
    }

    private void setStaticField(MethodVisitor methodVisitor, Method method, String className, String bindName)
    {
        Class<?> aClass = method.getDeclaringClass();
        methodVisitor.visitLdcInsn(Type.getType(aClass));
        methodVisitor.visitLdcInsn(method.getName());
        methodVisitor.visitLdcInsn(Type.getMethodDescriptor(method));
        methodVisitor.visitMethodInsn(INVOKESTATIC, "javassist/util/proxy/RuntimeSupport", "findMethod", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/reflect/Method;", false);
        methodVisitor.visitFieldInsn(PUTSTATIC, className, bindName, "Ljava/lang/reflect/Method;");
    }

    private void createMethods(ClassWriter classWriter, String className, Collection<Class<?>> ctInterfaces)
    {
        MethodVisitor setStaticMethodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        setStaticMethodVisitor.visitCode();
        int i = 0;
        for (Method method : proxyMethods) {
            final String methodFieldName = "_method" + i++;
            //1.
            addField(classWriter, ACC_PUBLIC | ACC_FINAL | ACC_STATIC, methodFieldName, "Ljava/lang/reflect/Method;");
            //2. add method
            addProxyMethod(classWriter, className, method, methodFieldName);
            //3. add static put
            setStaticField(setStaticMethodVisitor, method, className, methodFieldName);
        }
        setStaticMethodVisitor.visitInsn(RETURN);
        setStaticMethodVisitor.visitMaxs(3, 0);
        setStaticMethodVisitor.visitEnd();
    }

    private byte[] compile(String className, String baseClassInternalName)
    {
        ClassWriter classWriter = new ClassWriter(0); // ClassWriter.COMPUTE_MAXS
        List<Class<?>> interfaceList = new ArrayList<>(interfaceSet.size() + 1);
        interfaceList.add(ProxyAccess.class);
        interfaceList.add(Serializable.class);
        interfaceList.addAll(interfaceSet);

        String[] interfaces = interfaceList.stream().map(Type::getInternalName).toArray(String[]::new);
        classWriter.visit(version, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, className, null,
                baseClassInternalName, interfaces);

        //classWriter.visitSource(simpleName + ".java", null);

        addField(classWriter, ACC_PRIVATE, "handler", "Ljava/lang/reflect/InvocationHandler;");
        // add handler method
        addHandler(classWriter, className);
        addCallRealMethod(classWriter);
        //add proxy method
        createMethods(classWriter, className, interfaceSet);

        //add init Constructor
        //addInitConstructor(classWriter, baseClass.getConstructors()[0]);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    public byte[] generate()
    {
        String baseClassInternalName = Type.getInternalName(baseClass);
        return compile(className, baseClassInternalName);
    }
}
