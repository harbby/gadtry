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

import com.github.harbby.gadtry.base.JavaTypes;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.SIPUSH;

public class AsmUtil
{
    private AsmUtil() {}

    public static void pushClass(MethodVisitor mv, Class<?> arg)
    {
        if (arg.isPrimitive()) {
            Class<?> wrapper = JavaTypes.getWrapperClass(arg);
            mv.visitFieldInsn(GETSTATIC,
                    Type.getInternalName(wrapper), "TYPE",
                    "Ljava/lang/Class;"); // stack = 6
        }
        else {
            mv.visitLdcInsn(Type.getType(arg));  // stack = 6
        }
    }

    public static void pushDefaultObjectValue(MethodVisitor mv, Class<?> aClass)
    {
        pushClass(mv, aClass);
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(JavaTypes.class),
                "getClassInitValue", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
    }

    /**
     * valueOf
     */
    public static void boxingCast(MethodVisitor mv, Class<?> aClass, Class<?> wrapperClass)
    {
        String wrapper = Type.getInternalName(wrapperClass);
        String desc = String.format("(%s)L%s;", Type.getDescriptor(aClass), wrapper);
        mv.visitMethodInsn(INVOKESTATIC, wrapper, "valueOf", desc, false);
    }

    public static void unboxingCast(MethodVisitor mv, Class<?> aClass, Class<?> wrapperClass)
    {
        throw new UnsupportedOperationException();
    }

    public static void pushIntNumber(MethodVisitor mv, int number)
    {
        if (number >= -1 && number <= 5) {
            // [-1,5] see: ICONST_M1, ICONST_0, ICONST_1, ICONST_2 , ... , ICONST_5
            mv.visitInsn(ICONST_0 + number);
        }
        else if (number >= Byte.MIN_VALUE && number <= Byte.MAX_VALUE) {
            // [-128~127]
            mv.visitIntInsn(BIPUSH, number);
        }
        else if (number >= Short.MIN_VALUE && number <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, number);
        }
        else {
            //number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE
            mv.visitIntInsn(LDC, number);
        }
    }
}
