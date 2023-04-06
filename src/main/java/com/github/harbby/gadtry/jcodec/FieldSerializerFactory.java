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
package com.github.harbby.gadtry.jcodec;

import com.github.harbby.gadtry.aop.proxy2.AsmUtil;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Platform;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.github.harbby.gadtry.StaticAssert.DEBUG;
import static com.github.harbby.gadtry.jcodec.FieldSerializer.analyzeClass;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class FieldSerializerFactory
{
    private FieldSerializerFactory() {}

    private static final FieldSerializerFactory factory = new FieldSerializerFactory();

    @SuppressWarnings("unchecked")
    public static <T> FieldSerializer<T> makeSerializer(Jcodec jcodec, Class<? extends T> typeClass)
    {
        return (FieldSerializer<T>) factory.makeSerializer0(jcodec, typeClass);
    }

    private FieldSerializer<?> makeSerializer0(Jcodec jcodec, Class<?> typeClass)
    {
        List<FieldSerializer.FieldData> fieldDataList = analyzeClass(jcodec, typeClass);
        @SuppressWarnings({"rawtypes"})
        Serializer[] serializers = new Serializer[fieldDataList.size()];
        for (int i = 0; i < fieldDataList.size(); i++) {
            serializers[i] = fieldDataList.get(i).serializer;
        }
        try {
            Class<? extends FieldSerializer<?>> genClass = makeClass(typeClass, fieldDataList);
            return genClass.getConstructor(Jcodec.class, Class.class, Serializer[].class).newInstance(jcodec, typeClass, serializers);
        }
        catch (Exception e) {
            throw new JcodecException("jcodec failed", e);
        }
    }

    private <T> Class<? extends FieldSerializer<T>> makeClass(Class<? extends T> typeClass, List<FieldSerializer.FieldData> fieldDataList)
            throws NoSuchMethodException, IOException, IllegalAccessException
    {
        String classFullName = typeClass.getName() + "$GenSerializer";
        ClassLoader classLoader = typeClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        try {
            return classLoader.loadClass(classFullName).asSubclass(JavaTypes.classTag(Serializer.class));
        }
        catch (ClassNotFoundException ignored) {
        }

        if (fieldDataList.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        String className = classFullName.replace('.', '/');
        ClassWriter classWriter = new ClassWriter(COMPUTE_MAXS); // ClassWriter.COMPUTE_MAXS
        int version = Platform.getClassVersion();
        classWriter.visit(version, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, className, null, Type.getInternalName(FieldSerializer.class), null);
        classWriter.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "unsafe", "Lsun/misc/Unsafe;", null, null)
                .visitEnd();
        classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "serializers", "[Lcom/github/harbby/gadtry/jcodec/Serializer;", null, null)
                .visitEnd();
        // add static {}
        Method getUnsafeMethod = Platform.class.getMethod("getUnsafe");
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitMethodInsn(INVOKESTATIC, Type.getInternalName(getUnsafeMethod.getDeclaringClass()),
                getUnsafeMethod.getName(), Type.getMethodDescriptor(getUnsafeMethod), false);
        methodVisitor.visitFieldInsn(PUTSTATIC, className, "unsafe", "Lsun/misc/Unsafe;");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 0);
        methodVisitor.visitEnd();

        makeConstructor(className, classWriter);
        // add write
        addWriteMethod(className, classWriter, fieldDataList);
        // add read
        addReadMethod(className, classWriter, fieldDataList);
        classWriter.visitEnd();
        byte[] byteCode = classWriter.toByteArray();
        // IOUtils.write(byteCode, new File("out/" + className + ".class"));
        return Platform.defineClass(typeClass, byteCode).asSubclass(JavaTypes.classTag(FieldSerializer.class));
    }

    private void makeConstructor(String className, ClassWriter classWriter)
            throws NoSuchMethodException
    {
        String serializerArrDesc = Type.getDescriptor(Serializer[].class);
        String descriptor = String.format("(%sLjava/lang/Class;%s)V", Type.getDescriptor(Jcodec.class), serializerArrDesc);
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 2);
        Constructor<?> supperConstructor = FieldSerializer.class.getConstructor(Jcodec.class, Class.class);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(FieldSerializer.class), "<init>",
                Type.getConstructorDescriptor(supperConstructor), false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitFieldInsn(PUTFIELD, className, "serializers", serializerArrDesc);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(3, 4);
        methodVisitor.visitEnd();
    }

    private void addWriteMethod(String className, ClassWriter classWriter, List<FieldSerializer.FieldData> fieldDataList)
    {
        String descriptor = Type.getMethodDescriptor(WRITE_METHOD);
        int access = WRITE_METHOD.getModifiers() & 0x07; //Modifier.isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
        MethodVisitor methodVisitor = classWriter.visitMethod(access, WRITE_METHOD.getName(), descriptor, null, null);
        AnnotationVisitor annotationVisitor0 = methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
        annotationVisitor0.visitEnd();
        // add code
        methodVisitor.visitCode();
        for (int i = 0; i < fieldDataList.size(); i++) {
            FieldSerializer.FieldData fieldData = fieldDataList.get(i);
            Class<?> fieldClass = fieldData.getType();
            makeWriteField(className, methodVisitor, fieldData.field);
            if (fieldData.fieldType == FieldSerializer.FieldType.CLASS_AND_OBJET) {
                methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Jcodec.class), "writeClassAndObject",
                        "(Lcom/github/harbby/gadtry/jcodec/OutputView;Ljava/lang/Object;)V",
                        true);
            }
            else if (fieldData.fieldType == FieldSerializer.FieldType.OBJECT_OR_NULL) {
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, className, "serializers", "[Lcom/github/harbby/gadtry/jcodec/Serializer;");
                AsmUtil.pushIntNumber(methodVisitor, i);
                methodVisitor.visitInsn(AALOAD);
                methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Jcodec.class), "writeObjectOrNull",
                        "(Lcom/github/harbby/gadtry/jcodec/OutputView;Ljava/lang/Object;Lcom/github/harbby/gadtry/jcodec/Serializer;)V",
                        true);
            }
            else {
                assert !DEBUG || fieldData.fieldType == FieldSerializer.FieldType.OBJECT;
                if (!fieldClass.isPrimitive()) {
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    methodVisitor.visitFieldInsn(GETFIELD, className, "serializers", "[Lcom/github/harbby/gadtry/jcodec/Serializer;");
                    AsmUtil.pushIntNumber(methodVisitor, i);
                    methodVisitor.visitInsn(AALOAD);
                    methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Jcodec.class), "writeObject",
                            "(Lcom/github/harbby/gadtry/jcodec/OutputView;Ljava/lang/Object;Lcom/github/harbby/gadtry/jcodec/Serializer;)V",
                            true);
                }
            }
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(5, 4);
        methodVisitor.visitEnd();
    }

    private void makeWriteField(String className, MethodVisitor methodVisitor, Field field)
    {
        Class<?> fieldType = field.getType();
        String name = "Object";
        String desc;
        long offset = Platform.getUnsafe().objectFieldOffset(field);
        if (fieldType.isPrimitive()) {
            desc = Type.getDescriptor(fieldType);
            name = fieldType.getName().substring(0, 1).toUpperCase() + fieldType.getName().substring(1);
            methodVisitor.visitVarInsn(ALOAD, 2);
        }
        else {
            desc = "Ljava/lang/Object;";
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 2);  // stack 1
        }
        methodVisitor.visitFieldInsn(GETSTATIC, className, "unsafe", "Lsun/misc/Unsafe;"); // stack 2
        methodVisitor.visitVarInsn(ALOAD, 3); // // stack 3
        methodVisitor.visitLdcInsn(offset);  // // stack 4
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "get" + name,
                String.format("(Ljava/lang/Object;%s)%s", Type.getDescriptor(long.class), desc),
                false); // // stack 2
        if (fieldType.isPrimitive()) {
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(OutputView.class), "write" + name,
                    String.format("(%s)V", desc), true);  //stack 0
        }
    }

    private void addReadMethod(String className, ClassWriter classWriter, List<FieldSerializer.FieldData> fieldDataList)
            throws NoSuchMethodException
    {
        String descriptor = Type.getMethodDescriptor(READ_METHOD);
        int access = READ_METHOD.getModifiers() & 0x07; //Modifier.isPublic(method.getModifiers()) ? ACC_PUBLIC : ACC_PROTECTED;
        MethodVisitor methodVisitor = classWriter.visitMethod(access, READ_METHOD.getName(), descriptor, null, null);
        methodVisitor.visitCode();

        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, className, "typeClass", "Ljava/lang/Class;");
        Method newInstancemethod = FieldSerializer.class.getMethod("newInstance", Jcodec.class, InputView.class, Class.class);
        String desc = Type.getMethodDescriptor(newInstancemethod);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, className, "newInstance", desc, false);
        methodVisitor.visitVarInsn(ASTORE, 4);
        for (int i = 0; i < fieldDataList.size(); i++) {
            FieldSerializer.FieldData fieldData = fieldDataList.get(i);
            if (fieldData.fieldType == FieldSerializer.FieldType.OBJECT) {
                makeReadField(fieldData.field, className, methodVisitor, i, READ_OBJECT);
            }
            else if (fieldData.fieldType == FieldSerializer.FieldType.OBJECT_OR_NULL) {
                makeReadField(fieldData.field, className, methodVisitor, i, READ_OBJECT_OR_NULL);
            }
            else {
                makeReadField(fieldData.field, className, methodVisitor, i, READ_CLASS_AND_OBJECT);
            }
        }
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(8, 6);
        methodVisitor.visitEnd();
    }

    private static final Method READ_OBJECT;
    private static final Method READ_OBJECT_OR_NULL;
    private static final Method READ_CLASS_AND_OBJECT;
    private static final Method WRITE_METHOD;
    private static final Method READ_METHOD;

    static {
        try {
            READ_OBJECT = Jcodec.class.getMethod("readObject", InputView.class, Class.class, Serializer.class);
            READ_OBJECT_OR_NULL = Jcodec.class.getMethod("readObjectOrNull", InputView.class, Class.class, Serializer.class);
            READ_CLASS_AND_OBJECT = Jcodec.class.getMethod("readClassAndObject", InputView.class);
            WRITE_METHOD = Serializer.class.getMethod("write", Jcodec.class, OutputView.class, Object.class);
            READ_METHOD = Serializer.class.getMethod("read", Jcodec.class, InputView.class, Class.class);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private void makeReadField(Field field, String className, MethodVisitor methodVisitor, int i, Method readMethod)
    {
        Class<?> fieldType = field.getType();
        long offset = Platform.getUnsafe().objectFieldOffset(field);
        if (fieldType.isPrimitive()) {
            String name = fieldType.getName().substring(0, 1).toUpperCase() + fieldType.getName().substring(1);
            methodVisitor.visitFieldInsn(GETSTATIC, className, "unsafe", "Lsun/misc/Unsafe;");
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitLdcInsn(offset);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(InputView.class), "read" + name,
                    "()" + Type.getDescriptor(fieldType),
                    true);
            String desc = String.format("(Ljava/lang/Object;J%s)V", Type.getDescriptor(fieldType));
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "put" + name, desc, false);
            return;
        }
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 2);
        //---
        methodVisitor.visitFieldInsn(GETSTATIC, className, "unsafe", "Lsun/misc/Unsafe;");
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitLdcInsn(offset);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 2);
        if (readMethod == READ_CLASS_AND_OBJECT) {
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(readMethod.getDeclaringClass()), readMethod.getName(), Type.getMethodDescriptor(readMethod), true);
        }
        else {
            methodVisitor.visitLdcInsn(Type.getType(fieldType));
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, className, "serializers", "[Lcom/github/harbby/gadtry/jcodec/Serializer;");
            AsmUtil.pushIntNumber(methodVisitor, i);
            methodVisitor.visitInsn(AALOAD);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(readMethod.getDeclaringClass()),
                    readMethod.getName(), Type.getMethodDescriptor(readMethod), true);
        }
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V", false);
    }
}
