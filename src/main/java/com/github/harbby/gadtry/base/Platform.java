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
import sun.nio.ch.DirectBuffer;
import sun.reflect.ReflectionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSource;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public final class Platform
{
    private Platform() {}

    private static final Unsafe unsafe;
    private static final int classVersion = getClassVersion0();
    private static final String JAVA_FULL_VERSION = System.getProperty("java.version");
    private static final int javaVersion = getJavaVersion0();

    public static Unsafe getUnsafe()
    {
        return unsafe;
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
     * @return [base, dataOffset]
     */
    public static long[] allocateAlignMemory(long len, long alignByte)
    {
        checkArgument(Maths.isPowerOfTwo(alignByte), "Number %s power of two", alignByte);

        long size = Math.max(1L, len + alignByte);
        long base = 0;
        try {
            base = unsafe.allocateMemory(size);
        }
        catch (OutOfMemoryError x) {
            throw x;
        }
        //unsafe.setMemory(base, size, (byte) 0);

        long dataOffset;
        if (base % alignByte != 0) {
            // Round up to page boundary
            dataOffset = base + alignByte - (base & (alignByte - 1));
        }
        else {
            dataOffset = base;
        }
        long[] res = new long[2];
        res[0] = base;
        res[1] = dataOffset;
        return res;
    }

    public static void freeMemory(long address)
    {
        unsafe.freeMemory(address);
    }

    public static void registerForClean(AutoCloseable closeable)
    {
        createCleaner(closeable, (Runnable) () -> {
            try {
                closeable.close();
            }
            catch (Exception e) {
                throwException(e);
            }
        });
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

    public static boolean isWin()
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

    public static boolean isJdkClass(Class<?> aClass)
    {
        if (aClass.getName().startsWith("java.") || aClass.getName().startsWith("jdk.")) {
            return true;
        }
        return false;
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
        if (getJavaVersion() > 8) { //jdk9+
            return jdk.internal.ref.Cleaner.create(ob, thunk);
        }
        //jdk9+: --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED
        try {
            Method createMethod = Class.forName("sun.misc.Cleaner").getDeclaredMethod("create", Object.class, Runnable.class);
            createMethod.setAccessible(true);
            return createMethod.invoke(null, ob, thunk);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw Throwables.throwsThrowable(e);
        }
    }

    /**
     * java16 need: --add-opens=java.base/java.lang=ALL-UNNAMED
     */
    public static Class<?> defineClass(byte[] classBytes, ClassLoader classLoader)
    {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>) defineClass.invoke(classLoader, null, classBytes, 0, classBytes.length);
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e1) {
            throwException(e1);
        }
        throw new IllegalStateException("unchecked");
    }

    /**
     * Converts the class byte code to a java.lang.Class object
     * This method is available in Java 9 or later
     */
    public static Class<?> defineClass(Class<?> buddyClass, byte[] classBytes)
    {
        try {
            Platform.class.getModule().addReads(buddyClass.getModule());
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandles.Lookup prvlookup = MethodHandles.privateLookupIn(buddyClass, lookup);
            return prvlookup.defineClass(classBytes);
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            //buddyClass has no permission to define the class
            throw Throwables.throwsThrowable(e);
        }
    }

    /**
     * 绕开jdk9模块化引入的访问限制:
     * 1. 非法反射访问警告消除
     * 2. --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED 型访问限制消除， 现在通过这个方法我们可以访问任何jdk限制的内部代码
     * 注: java16开始，你需要提供额外的java运行时参数: --add-opens=java.base/java.lang=ALL-UNNAMED 来使用该方法提供的功能
     * <p>
     *
     * @param hostClass   action将获取hostClass的内部实现反射访问权限
     * @param targetClass 希望获取权限的类
     * @throws java.lang.NoClassDefFoundError user dep class
     * @since jdk9+
     */
    public static void addOpenJavaModules(Class<?> hostClass, Class<?> targetClass)
    {
        checkState(getJavaVersion() > 8, "This method can only run above Jdk9+");
        try {
            Method getModule = Class.class.getMethod("getModule");
            Class<?> mouleClass = getModule.getReturnType();
            Method getPackageName = Class.class.getMethod("getPackageName");
            Method addOpens = mouleClass.getMethod("addOpens", String.class, mouleClass);
            Method isOpenMethod = mouleClass.getMethod("isOpen", String.class, mouleClass);
            //---------------------
            boolean isNamed = (boolean) mouleClass.getMethod("isNamed").invoke(getModule.invoke(targetClass));
            checkState(!isNamed, "targetClass " + targetClass + " must UNNAMED moule");
            //----------------------
            Object hostModule = getModule.invoke(hostClass);
            String hostPackageName = (String) getPackageName.invoke(hostClass);

            Object actionModule = getModule.invoke(targetClass);
            boolean isOpen = (boolean) isOpenMethod.invoke(hostModule, hostPackageName, actionModule);
            if (isOpen) {
                addOpens.invoke(hostModule, hostPackageName, actionModule);
            }
            else {
                addOpens.invoke(getModule.invoke(mouleClass), (String) getPackageName.invoke(mouleClass), getModule.invoke(Platform.class));
                Method method = mouleClass.getDeclaredMethod("implAddExportsOrOpens", String.class, mouleClass, boolean.class, boolean.class);
                method.setAccessible(true);
                //--add-opens=java.base/$hostPackageName=ALL-UNNAMED
                method.invoke(hostModule, hostPackageName, actionModule, /*open*/true, /*syncVM*/true);
            }
        }
        catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(e);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throwException(e);
        }
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
     * jdk9+ need: --add-opens=java.base/java.nio=ALL-UNNAMED
     *
     * @param size allocate mem size
     * @return ByteBuffer
     */
    public static ByteBuffer allocateDirectBuffer(int size)
    {
        try {
            Class<?> cls = Class.forName("java.nio.DirectByteBuffer");
            Constructor<?> constructor = cls.getDeclaredConstructor(Long.TYPE, Integer.TYPE);
            constructor.setAccessible(true);
            Field cleanerField = cls.getDeclaredField("cleaner");
            cleanerField.setAccessible(true);
            long memory = unsafe.allocateMemory(size);
            ByteBuffer buffer = (ByteBuffer) constructor.newInstance(memory, size);
            Object cleaner = createCleaner(buffer, (Runnable) () -> unsafe.freeMemory(memory));
            //Cleaner cleaner = Cleaner.create(buffer, () -> _UNSAFE.freeMemory(memory));
            cleanerField.set(buffer, cleaner);
            return buffer;
        }
        catch (Exception e) {
            throwException(e);
        }
        throw new IllegalStateException("unreachable");
    }

    /**
     * Free DirectBuffer
     *
     * @param buffer DirectBuffer waiting to be released
     */
    public static void freeDirectBuffer(ByteBuffer buffer)
    {
        checkState(buffer.isDirect(), "buffer not direct");
        sun.nio.ch.DirectBuffer directBuffer = (DirectBuffer) buffer;
        if (getJavaVersion() > 8) {
            directBuffer.cleaner().clean();
        }
        else {
            try {
                Object cleaner = sun.nio.ch.DirectBuffer.class.getMethod("cleaner")
                        .invoke(directBuffer);
                Class.forName("sun.misc.Cleaner").getMethod("clean").invoke(cleaner);
            }
            catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                throwException(e);
            }
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
            throws InstantiationException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Constructor<T> superCons = (Constructor<T>) Object.class.getConstructor();
        ReflectionFactory reflFactory = ReflectionFactory.getReflectionFactory();
        Constructor<T> c = (Constructor<T>) reflFactory.newConstructorForSerialization(tClass, superCons);
        return c.newInstance();
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

    /**
     * Limits the number of bytes to copy per {@link Unsafe#copyMemory(long, long, long)} to
     * allow safepoint polling during a large copy.
     */
    private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    static {
        sun.misc.Unsafe obj = null;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            obj = (sun.misc.Unsafe) unsafeField.get(null);
        }
        catch (Throwable cause) {
            throwException(cause);
        }
        unsafe = requireNonNull(obj);
    }
}
