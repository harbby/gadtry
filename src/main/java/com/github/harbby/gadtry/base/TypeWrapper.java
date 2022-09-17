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
package com.github.harbby.gadtry.base;

/**
 * see: sun.invoke.util.Wrapper;
 */
public enum TypeWrapper
{
    //        wrapperType      simple     primitiveType  simple     char  emptyArray     format
    BOOLEAN(Boolean.class, boolean.class, 'Z', new boolean[0], false),
    // These must be in the order defined for widening primitive conversions in JLS 5.1.2
    // Avoid boxing integral types here to defer initialization of internal caches
    BYTE(Byte.class, byte.class, 'B', new byte[0], (byte) 0),
    SHORT(Short.class, short.class, 'S', new short[0], (short) 0),
    CHAR(Character.class, char.class, 'C', new char[0], (char) 0),
    INT(Integer.class, int.class, 'I', new int[0], 0),
    LONG(Long.class, long.class, 'J', new long[0], 0L),
    FLOAT(Float.class, float.class, 'F', new float[0], 0F),
    DOUBLE(Double.class, double.class, 'D', new double[0], 0D),
    // VOID must be the last type, since it is "assignable" from any other type:
    VOID(Void.class, void.class, 'V', null, null);

    private final Class<?> wrapperType;
    private final Class<?> primitiveType;
    private final char basicTypeChar;
    private final Object emptyArray;
    private final Object defaultValue;

    TypeWrapper(Class<?> wtype, Class<?> ptype, char tchar, Object emptyArray, Object defaultValue)
    {
        this.wrapperType = wtype;
        this.primitiveType = ptype;
        this.basicTypeChar = tchar;
        this.emptyArray = emptyArray;
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }

    public Class<?> primitiveType()
    {
        return primitiveType;
    }

    /**
     * What is the wrapper type for this wrapper?
     */
    public Class<?> wrapperType()
    {
        return wrapperType;
    }

    // primitive array support
    public Object makePrimitiveTypeArray(int len)
    {
        return java.lang.reflect.Array.newInstance(primitiveType, len);
    }

    public Class<?> arrayType()
    {
        return emptyArray.getClass();
    }

    public Object getEmptyArray()
    {
        return emptyArray;
    }

    public char basicTypeChar()
    {
        return basicTypeChar;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> aClass)
    {
        if (aClass.isPrimitive()) {
            return (T) forPrimitiveType(aClass).defaultValue;
        }
        return null;
    }

    /**
     * Query:  Is the given type a wrapper, such as {@code Integer} or {@code Void}?
     */
    public static boolean isWrapperType(Class<?> type)
    {
        return findWrapperType(type) != null;
    }

    /**
     * Query:  Is the given type a primitive, such as {@code int} or {@code void}?
     */
    public static boolean isPrimitiveType(Class<?> type)
    {
        return type.isPrimitive();
    }

    /**
     * If {@code type} is a primitive type, return the corresponding
     * wrapper type, else return {@code type} unchanged.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> asWrapperType(Class<T> type)
    {
        return (Class<T>) forPrimitiveType(type).wrapperType;
    }

    /**
     * If {@code type} is a wrapper type, return the corresponding
     * primitive type, else return {@code type} unchanged.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> asPrimitiveType(Class<T> type)
    {
        return (Class<T>) forWrapperType(type).primitiveType;
    }

    public static char basicTypeChar(Class<?> type)
    {
        if (!type.isPrimitive()) {
            return 'L';
        }
        else {
            return forPrimitiveType(type).basicTypeChar();
        }
    }

    /**
     * Return the wrapper that wraps values into the given wrapper type.
     * If it is {@code Object}, return {@code OBJECT}.
     * Otherwise, it must be a wrapper type.
     * The type must not be a primitive type.
     *
     * @throws IllegalArgumentException for unexpected types
     */
    public static TypeWrapper forWrapperType(Class<?> type)
    {
        TypeWrapper w = findWrapperType(type);
        if (w != null) {
            return w;
        }
        for (TypeWrapper x : values()) {
            if (x.wrapperType == type) {
                throw new InternalError(); // redo hash function
            }
        }
        throw new IllegalArgumentException("not wrapper: " + type);
    }

    /**
     * Return the wrapper that wraps values of the given type.
     * The type may be {@code Object}, meaning the {@code OBJECT} wrapper.
     * Otherwise, the type must be a primitive.
     *
     * @throws IllegalArgumentException for unexpected types
     */
    public static TypeWrapper forPrimitiveType(Class<?> type)
    {
        TypeWrapper w = findPrimitiveType(type);
        if (w != null) {
            return w;
        }
        if (type.isPrimitive()) {
            throw new InternalError(); // redo hash function
        }
        throw new IllegalArgumentException("not primitive: " + type);
    }

    public static TypeWrapper forPrimitiveType(char basicTypeChar)
    {
        switch (basicTypeChar) {
            case 'I':
                return INT;
            case 'J':
                return LONG;
            case 'S':
                return SHORT;
            case 'B':
                return BYTE;
            case 'C':
                return CHAR;
            case 'F':
                return FLOAT;
            case 'D':
                return DOUBLE;
            case 'Z':
                return BOOLEAN;
            case 'V':
                return VOID;
            default:
                throw new IllegalArgumentException("not primitive: " + basicTypeChar);
        }
    }

    static TypeWrapper findPrimitiveType(Class<?> type)
    {
        TypeWrapper w = FROM_PRIM[hashPrim(type)];
        if (w != null && w.primitiveType == type) {
            return w;
        }
        return null;
    }

    static TypeWrapper findWrapperType(Class<?> type)
    {
        TypeWrapper w = FROM_WRAP[hashWrap(type)];
        if (w != null && w.wrapperType == type) {
            return w;
        }
        return null;
    }

    /**
     * Return the wrapper that corresponds to the given bytecode
     * signature character.  Return {@code OBJECT} for the character 'L'.
     *
     * @throws IllegalArgumentException for any non-signature character or {@code '['}.
     */
    public static TypeWrapper forBasicType(char type)
    {
        TypeWrapper w = FROM_CHAR[hashChar(type)];
        if (w != null && w.basicTypeChar == type) {
            return w;
        }
        for (TypeWrapper x : values()) {
            if (w.basicTypeChar == type) {
                throw new InternalError(); // redo hash function
            }
        }
        throw new IllegalArgumentException("not basic type char: " + type);
    }

    // Note on perfect hashes:
    //   for signature chars c, do (c + (c >> 1)) % 16
    //   for primitive type names n, do (n[0] + n[2]) % 16
    // The type name hash works for both primitive and wrapper names.
    // You can add "java/lang/Object" to the primitive names.
    // But you add the wrapper name Object, use (n[2] + (3*n[1])) % 16.
    private static final TypeWrapper[] FROM_PRIM = new TypeWrapper[16];
    private static final TypeWrapper[] FROM_WRAP = new TypeWrapper[16];
    private static final TypeWrapper[] FROM_CHAR = new TypeWrapper[16];

    private static int hashPrim(Class<?> x)
    {
        String xn = x.getName();
        if (xn.length() < 3) {
            return 0;
        }
        return (xn.charAt(0) + xn.charAt(2)) % 16;
    }

    private static int hashWrap(Class<?> x)
    {
        String xn = x.getName();
        final int offset = 10; // java.lang.
        if (xn.length() < offset + 3) {
            return 0;
        }
        return (xn.charAt(offset) + xn.charAt(offset + 2)) % 16;
    }

    private static int hashChar(char x)
    {
        return (x + (x >> 1)) % 16;
    }

    static {
        for (TypeWrapper w : values()) {
            int pi = hashPrim(w.primitiveType);
            int wi = hashWrap(w.wrapperType);
            int ci = hashChar(w.basicTypeChar);
            assert (FROM_PRIM[pi] == null);
            assert (FROM_WRAP[wi] == null);
            assert (FROM_CHAR[ci] == null);
            FROM_PRIM[pi] = w;
            FROM_WRAP[wi] = w;
            FROM_CHAR[ci] = w;
        }
        //assert(jdk.sun.invoke.util.WrapperTest.test(false));
    }
}
