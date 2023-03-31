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

import com.github.harbby.gadtry.io.IOUtils;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.util.Comparator;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public final class Platform
{
    private Platform() {}

    /**
     * Limits the number of bytes to copy per {@link Unsafe#copyMemory(long, long, long)} to
     * allow safepoint polling during a large copy.
     */
    private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    static final Unsafe unsafe;
    private static final int classVersion = getClassVersion0();
    private static final String JAVA_FULL_VERSION = System.getProperty("java.version");
    private static final int javaVersion = getJavaVersion0();

    public static Unsafe getUnsafe()
    {
        return unsafe;
    }

    private static int pageSize = -1;
    private static final Class<?> DIRECT_BYTE_BUFFER_CLASS;
    private static final long DBB_CLEANER_FIELD_OFFSET;
    private static final long DBB_ADDRESS_FIELD_OFFSET;
    private static final long DBB_CAPACITY_FIELD_OFFSET;
    private static final Method DBB_CLEANER_CREATE_METHOD;
    private static final Method DBB_CLEANER_CLEAN_METHOD;
    private static final Method DBB_BYTE_BUFFER_CLEAR_METHOD;

    static {
        Unsafe obj = null;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            obj = (sun.misc.Unsafe) requireNonNull(unsafeField.get(null));
        }
        catch (Exception cause) {
            throwException(cause);
        }
        unsafe = obj;

        try {
            Class<?> cls = Class.forName("java.nio.DirectByteBuffer");
            Field dbbCleanerField = cls.getDeclaredField("cleaner");
            DBB_CLEANER_FIELD_OFFSET = getUnsafe().objectFieldOffset(dbbCleanerField);
            DBB_ADDRESS_FIELD_OFFSET = getUnsafe().objectFieldOffset(Buffer.class.getDeclaredField("address"));
            DBB_CAPACITY_FIELD_OFFSET = getUnsafe().objectFieldOffset(Buffer.class.getDeclaredField("capacity"));
            DBB_CLEANER_CREATE_METHOD = dbbCleanerField.getType().getMethod("create", Object.class, Runnable.class);
            DIRECT_BYTE_BUFFER_CLASS = cls;
            DBB_CLEANER_CLEAN_METHOD = dbbCleanerField.getType().getMethod("clean");
            DBB_BYTE_BUFFER_CLEAR_METHOD = Buffer.class.getMethod("clear");
        }
        catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
    }

    /**
     * Java9引入模块化后，一些内部api具有破坏性兼容性。
     * 这里的方法多数为Java9以上平台所添加的新方法
     */
    interface ExtPlatform
    {
        Class<?> defineClass(Class<?> buddyClass, byte[] classBytes)
                throws IllegalAccessException;

        void freeDirectBuffer(ByteBuffer buffer);

        Object createCleaner(Object ob, Runnable thunk);

        ClassLoader getBootstrapClassLoader();

        long getProcessPid(Process process);

        long getCurrentProcessId();

        boolean isOpen(Class<?> source, Class<?> target);

        boolean isDirectMemoryPageAligned();

        public Class<?> defineHiddenClass(Class<?> buddyClass, byte[] classBytes, boolean initialize)
                throws IllegalAccessException;

        public <A> Comparator<A> getArrayComparator(Class<A> typeClass);
    }

    private static class ExtPlatformHolder
    {
        private static final ExtPlatform extPlatform;

        static {
            ExtPlatform obj;
            ClassLoader classLoader = Platform.class.getClassLoader();
            try {
                Class<?> aClass = classLoader.loadClass("com.github.harbby.gadtry.base.ExtPlatformImpl");
                obj = (ExtPlatform) aClass.getDeclaredConstructor().newInstance();
            }
            catch (Exception e) {
                throw new PlatFormUnsupportedOperation(e);
            }
            extPlatform = obj;
        }

        public static ExtPlatform getExtPlatform()
        {
            return extPlatform;
        }
    }

    public static <A> Comparator<A> getArrayComparator(Class<A> typeClass)
    {
        if (getJavaVersion() < 9) {
            throw new PlatFormUnsupportedOperation("not support java8");
        }
        else {
            checkArgument(typeClass.isArray() && typeClass.getComponentType().isPrimitive(), "must be primitive array.class");
            return ExtPlatformHolder.getExtPlatform().getArrayComparator(typeClass);
        }
    }

    public static byte[] readClassByteCode(Class<?> aClass)
            throws IOException
    {
        CodeSource codeSource = requireNonNull(aClass.getProtectionDomain().getCodeSource(), "not found codeSource");
        URL location = codeSource.getLocation();
        if ("jar".equals(location.getProtocol()) || "file".equals(location.getProtocol())) {
            try (InputStream inputStream = new URL(location, aClass.getName().replace(".", "/") + ".class").openStream()) {
                return IOUtils.readAllBytes(inputStream);
            }
        }
        throw new UnsupportedOperationException("not support location " + location);
    }

    public static long allocateMemory(long bytes)
    {
        return unsafe.allocateMemory(bytes);  //默认按16个字节对齐
    }

    /**
     * copy  {@link java.nio.ByteBuffer#allocateDirect(int)}
     * must alignByte = Math.sqrt(alignByte)
     *
     * @param len       allocate direct mem size
     * @param alignByte default 16
     * @return base address
     */
    public static long allocateAlignMemory(long len, int alignByte)
    {
        checkArgument(Maths.isPowerOfTwo(alignByte), "Number %s power of two", alignByte);
        if (len < 0) {
            throw new IllegalArgumentException("capacity < 0: (" + len + " < 0)");
        }

        long size = Math.max(1L, len + alignByte);
        long base = unsafe.allocateMemory(size);
        unsafe.setMemory(base, size, (byte) 0);
        // long dataOffset = getAlignedDataAddress(base, alignByte);
        return base;
    }

    public static long getAlignedDataAddress(long base, int alignByte)
    {
        long dataOffset;
        if (base % alignByte != 0) {
            // Round up to page boundary
            dataOffset = base + alignByte - (base & (alignByte - 1));
        }
        else {
            dataOffset = base;
        }
        return dataOffset;
    }

    public static void freeMemory(long address)
    {
        unsafe.freeMemory(address);
    }

    public static int getJavaVersion()
    {
        return javaVersion;
    }

    public static int getClassVersion()
    {
        return classVersion;
    }

    private static int getJavaVersion0()
    {
        String javaSpecVersion = requireNonNull(System.getProperty("java.specification.version"),
                "not found value for System.getProperty(java.specification.version)");
        final String[] split = javaSpecVersion.split("\\.");
        final int[] version = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            version[i] = Integer.parseInt(split[i]);
        }
        if (version[0] == 1) {
            return version[1];
        }
        else {
            return version[0];
        }
    }

    /**
     * java8 52
     * java9 53
     * java10 54
     * java11 55
     * java15 59
     * java16 60
     * java17 61
     *
     * @return vm class major version
     */
    private static int getClassVersion0()
    {
        String javaClassVersion = requireNonNull(System.getProperty("java.class.version"),
                "not found value for System.getProperty(java.class.version)");
        final String[] split = javaClassVersion.split("\\.");
        final int[] version = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            version[i] = Integer.parseInt(split[i]);
        }
        //assert version[0] == 0;
        return version[0];
    }

    public static boolean isWindows()
    {
        return osName().startsWith("Windows");
    }

    public static boolean isMac()
    {
        return osName().startsWith("Mac OS X");
    }

    public static boolean isLinux()
    {
        return osName().startsWith("Linux");
    }

    public static String osName()
    {
        return System.getProperty("os.name", "");
    }

    public static boolean isOpen(Class<?> source, Class<?> target)
    {
        checkState(getJavaVersion() > 8, "java version > 8");
        return ExtPlatformHolder.getExtPlatform().isOpen(source, target);
    }

    /**
     * jdk8:  sun.misc.VM.latestUserDefinedLoader()
     * jdk9+: jdk.internal.misc.VM.latestUserDefinedLoader()
     *
     * @return latestUserDefinedLoader
     */
    public static ClassLoader latestUserDefinedLoader()
    {
        throw new UnsupportedOperationException("jdk8:  sun.misc.VM.latestUserDefinedLoader(). " +
                "or jdk9+: jdk.internal.misc.VM.latestUserDefinedLoader()");
    }

    /**
     * 用户系统类加载器
     *
     * @return appClassLoader
     */
    public static ClassLoader getAppClassLoader()
    {
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * 获取jdk类加载器
     *
     * @return bootstrap ClassLoader
     */
    public static ClassLoader getBootstrapClassLoader()
    {
        if (Platform.getJavaVersion() > 8) {
            return ExtPlatformHolder.getExtPlatform().getBootstrapClassLoader();
        }
        // jars: Class.forName("sun.misc.Launcher").getMethod("getBootstrapClassPath")
        return ClassLoader.getSystemClassLoader().getParent();
    }

    public static long getCurrentProcessId()
    {
        if (Platform.getJavaVersion() > 8) {
            return ExtPlatformHolder.getExtPlatform().getCurrentProcessId();
        }
        if (Platform.isWindows() || Platform.isLinux() || Platform.isMac()) {
            return Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        }
        else {
            throw new PlatFormUnsupportedOperation("java" + getJavaVersion() + " get pid does not support current system " + osName());
        }
    }

    public static long getProcessPid(Process process)
    {
        if (Platform.getJavaVersion() > 8) { //>= java9
            return ExtPlatformHolder.getExtPlatform().getProcessPid(process);
        }
        if (Platform.isLinux() || Platform.isMac()) {
            try {
                Field field = process.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                return (int) field.get(process);
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                throw new PlatFormUnsupportedOperation(e);
            }
        }
        else if (Platform.isWindows()) {
            try {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);

                com.sun.jna.platform.win32.WinNT.HANDLE handle = new com.sun.jna.platform.win32.WinNT.HANDLE();
                handle.setPointer(com.sun.jna.Pointer.createConstant((long) field.get(process)));
                return com.sun.jna.platform.win32.Kernel32.INSTANCE.GetProcessId(handle);
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                throw new PlatFormUnsupportedOperation(e);
            }
        }
        else {
            throw new UnsupportedOperationException("get pid when java.version < 9 only support UNIX Linux windows macOS");
        }
    }

    /**
     * java16 need: --add-opens=java.base/java.lang=ALL-UNNAMED
     * If java version> 16 it is recommended to use {@link Platform#defineClass(Class, byte[])}
     */
    public static Class<?> defineClass(byte[] classBytes, ClassLoader classLoader)
            throws PlatFormUnsupportedOperation, IllegalAccessException
    {
        requireNonNull(classLoader, "classLoader is null");
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>) defineClass.invoke(classLoader, null, classBytes, 0, classBytes.length);
        }
        catch (NoSuchMethodException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
        catch (InvocationTargetException e) {
            throw new PlatFormUnsupportedOperation(e.getTargetException());
        }
    }

    public static Class<?> defineHiddenClass(Class<?> buddyClass, byte[] classBytes, boolean initialize)
            throws IllegalAccessException
    {
        checkState(getJavaVersion() >= 15, "This method is available in Java 15 or later");
        return ExtPlatformHolder.getExtPlatform().defineHiddenClass(buddyClass, classBytes, initialize);
    }

    /**
     * Converts the class byte code to a java.lang.Class object
     * This method is available in Java 9 or later
     */
    public static Class<?> defineClass(Class<?> buddyClass, byte[] classBytes)
            throws IllegalAccessException
    {
        checkState(getJavaVersion() > 8, "This method is available in Java 9 or later");
        return ExtPlatformHolder.getExtPlatform().defineClass(buddyClass, classBytes);
    }

    public static long reallocateMemory(long address, long oldSize, long newSize)
    {
        long newMemory = unsafe.allocateMemory(newSize);
        copyMemory(null, address, null, newMemory, oldSize);
        unsafe.freeMemory(address);
        return newMemory;
    }

    /**
     * Uses internal JDK APIs to allocate a DirectByteBuffer while ignoring the JVM's
     * MaxDirectMemorySize limit (the default limit is too low and we do not want to require users
     * to increase it).
     * <p>
     * jdk9+ need:
     * --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
     * --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED
     *
     * @param capacity allocate mem size
     * @return ByteBuffer
     */
    public static ByteBuffer allocateDirectBuffer(int capacity)
            throws PlatFormUnsupportedOperation
    {
        if (getJavaVersion() > 8) {
            boolean isDirectMemoryPageAligned = ExtPlatformHolder.getExtPlatform().isDirectMemoryPageAligned();
            if (!isDirectMemoryPageAligned) {
                return ByteBuffer.allocateDirect(capacity);
            }
        }
        long address = unsafe.allocateMemory(capacity);
        return warpByteBuff(capacity, address, address);
    }

    public static int pageSize()
    {
        if (pageSize == -1) {
            pageSize = unsafe.pageSize();
        }
        return pageSize;
    }

    public static ByteBuffer allocateDirectBuffer(int capacity, int alignMemory)
            throws PlatFormUnsupportedOperation
    {
        if (getJavaVersion() > 8) {
            boolean isDirectMemoryPageAligned = ExtPlatformHolder.getExtPlatform().isDirectMemoryPageAligned();
            if (!isDirectMemoryPageAligned) {
                checkArgument(Maths.isPowerOfTwo(alignMemory), "Number %s power of two", alignMemory);
                if (capacity < 0) {
                    throw new IllegalArgumentException("capacity < 0: (" + capacity + " < 0)");
                }

                int size = Math.max(1, capacity + alignMemory);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
                long baseAddress = unsafe.getLong(byteBuffer, DBB_ADDRESS_FIELD_OFFSET);
                long dataAddress = getAlignedDataAddress(baseAddress, alignMemory);
                unsafe.putLong(byteBuffer, DBB_ADDRESS_FIELD_OFFSET, dataAddress);
                return byteBuffer;
            }
            else {
                if (alignMemory == pageSize()) {
                    return ByteBuffer.allocateDirect(capacity);
                }
            }
        }
        long baseAddress = allocateAlignMemory(capacity, alignMemory);
        long dataAddress = getAlignedDataAddress(baseAddress, alignMemory);
        return warpByteBuff(capacity, baseAddress, dataAddress);
    }

    private static ByteBuffer warpByteBuff(int capacity, long baseAddress, long dataAddress)
    {
        try {
            //Constructor<?> constructor = DIRECT_BYTE_BUFFER_CLASS.getDeclaredConstructor(Long.TYPE, Integer.TYPE);
            //ByteBuffer buffer = (ByteBuffer) constructor.newInstance(memory, size);
            ByteBuffer buffer = (ByteBuffer) unsafe.allocateInstance(DIRECT_BYTE_BUFFER_CLASS);
            unsafe.putLong(buffer, DBB_ADDRESS_FIELD_OFFSET, dataAddress);
            unsafe.putInt(buffer, DBB_CAPACITY_FIELD_OFFSET, capacity);
            Object cleaner = createCleaner(buffer, (Runnable) () -> unsafe.freeMemory(baseAddress));
            unsafe.putObject(buffer, DBB_CLEANER_FIELD_OFFSET, cleaner);
            DBB_BYTE_BUFFER_CLEAR_METHOD.invoke(buffer);
            return buffer;
        }
        catch (Exception e) {
            unsafe.freeMemory(baseAddress);
            throw new PlatFormUnsupportedOperation(e);
        }
    }

    /**
     * Creates a new cleaner.
     *
     * @param ob    the referent object to be cleaned
     * @param thunk The cleanup code to be run when the cleaner is invoked.  The
     *              cleanup code is run directly from the reference-handler thread,
     *              so it should be as simple and straightforward as possible.
     * @return The new cleaner
     */
    public static Object createCleaner(Object ob, Runnable thunk)
    {
        if (getJavaVersion() > 8) {
            return ExtPlatformHolder.getExtPlatform().createCleaner(ob, thunk);
        }
        try {
            return DBB_CLEANER_CREATE_METHOD.invoke(null, ob, thunk);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new PlatFormUnsupportedOperation("unchecked", e);
        }
    }

    /**
     * Free DirectBuffer
     *
     * @param buffer DirectBuffer waiting to be released
     */
    public static void freeDirectBuffer(ByteBuffer buffer)
    {
        checkState(buffer.isDirect(), "buffer not direct");
        if (getJavaVersion() > 8) {
            ExtPlatformHolder.getExtPlatform().freeDirectBuffer(buffer);
            return;
        }
        try {
            Object cleaner = unsafe.getObject(buffer, DBB_CLEANER_FIELD_OFFSET);
            DBB_CLEANER_CLEAN_METHOD.invoke(cleaner);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("unchecked", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> tClass)
            throws InstantiationException
    {
        return (T) unsafe.allocateInstance(tClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance2(Class<T> tClass)
            throws PlatFormUnsupportedOperation
    {
        try {
            Constructor<T> superCons = (Constructor<T>) Object.class.getConstructor();
            ReflectionFactory reflFactory = ReflectionFactory.getReflectionFactory();
            Constructor<T> c = (Constructor<T>) reflFactory.newConstructorForSerialization(tClass, superCons);
            return c.newInstance();
        }
        catch (InvocationTargetException | InstantiationException | NoSuchMethodException | IllegalAccessException e) {
            throw new PlatFormUnsupportedOperation(e);
        }
    }

    public static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length)
    {
        // Check if dstOffset is before or after srcOffset to determine if we should copy
        // forward or backwards. This is necessary in case src and dst overlap.
        if (dstOffset < srcOffset) {
            while (length > 0) {
                long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
                unsafe.copyMemory(src, srcOffset, dst, dstOffset, size);
                length -= size;
                srcOffset += size;
                dstOffset += size;
            }
        }
        else {
            srcOffset += length;
            dstOffset += length;
            while (length > 0) {
                long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
                srcOffset -= size;
                dstOffset -= size;
                unsafe.copyMemory(src, srcOffset, dst, dstOffset, size);
                length -= size;
            }
        }
    }

    /**
     * Raises an exception bypassing compiler checks for checked exceptions.
     *
     * @param t Throwable
     */
    public static void throwException(Throwable t)
    {
        unsafe.throwException(t);
    }
}
