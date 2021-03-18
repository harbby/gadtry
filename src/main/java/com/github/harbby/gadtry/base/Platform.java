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

import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.compiler.ByteClassLoader;
import com.github.harbby.gadtry.compiler.JavaClassCompiler;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.Modifier;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public final class Platform
{
    private Platform() {}

    private static final Unsafe unsafe;

    public static Unsafe getUnsafe()
    {
        return unsafe;
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
        try {
            Method createMethod = Class.forName("sun.misc.Cleaner").getDeclaredMethod("create", Object.class, Runnable.class);
            createMethod.setAccessible(true);
            return createMethod.invoke(null, ob, thunk);
        }
        catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throwException(e);
        }
        //jdk9+
        //run vm: --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED
        try {
            Class<?> cleanerCLass = Class.forName("jdk.internal.ref.Cleaner");
            addOpenJavaModules(cleanerCLass, Platform.class);
            return cleanerCLass
                    .getMethod("create", Object.class, Runnable.class)
                    .invoke(null, ob, thunk);
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

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

    public static Class<?> defineClassByUnsafe(byte[] classBytes, ClassLoader classLoader)
    {
        final ProtectionDomain defaultDomain =
                new ProtectionDomain(new CodeSource(null, (Certificate[]) null),
                        null, classLoader, null);
        try {
            Method method = Unsafe.class.getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
            return (Class<?>) method.invoke(unsafe, null, classBytes, 0, classBytes.length, classLoader, defaultDomain);
        }
        catch (NoSuchMethodException e) {
            //jdk9+
            try {
                Class<?> aClass = Class.forName("jdk.internal.misc.Unsafe");
                //add-opens aClass Module to Platform.class
                addOpenJavaModules(aClass, Platform.class);
                Object theInternalUnsafe = aClass.getMethod("getUnsafe").invoke(null);
                return (Class<?>) aClass.getMethod("defineClass",
                        String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class)
                        .invoke(theInternalUnsafe, null, classBytes, 0, classBytes.length, classLoader, defaultDomain);
            }
            catch (Exception e1) {
                throw new IllegalStateException(e1);
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throwException(e);
        }
        throw new IllegalStateException("unchecked");
    }

    /**
     * java8 52
     * java11 55
     * java15 59
     *
     * @return vm class version
     */
    public static int getVmClassVersion()
    {
        return (int) Float.parseFloat(System.getProperty("java.class.version"));
    }

    /**
     * 绕开jdk9模块化引入的访问限制:
     * 1. 非法反射访问警告消除
     * 2. --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED 型访问限制消除， 现在通过他我们可以访问任何jdk限制的内部代码
     * <p>
     *
     * @param hostClass   action将获取hostClass的权限
     * @param targetClass 希望获取权限的类
     * @throws java.lang.NoClassDefFoundError user dep class
     * @since jdk9+
     */
    public static void addOpenJavaModules(Class<?> hostClass, Class<?> targetClass)
    {
        checkState(getVmClassVersion() > 52, "This method can only run above Jdk9+");
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

    /**
     * forRemoval = true
     * 存在一些缺陷:
     * 1: 目前存在action不能依赖任何用户类,否则会throw java.lang.NoClassDefFoundError
     * 2: 不支持java8 lambda
     *
     * @param hostClass 宿主类
     * @param action    被复制的类
     * @return obj
     * @throws Exception any Exception
     * @since = "jdk15"
     * <p>
     * 复制action类的字节码，注册成hostClass匿名内部类
     */
    public static Object defineAnonymousClass(Class<?> hostClass, Object action)
            throws Exception
    {
        Map<String, Object> actionFieldValues = new HashMap<>();
        for (Field f : action.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            actionFieldValues.put(f.getName(), f.get(action));
        }

        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.getCtClass(action.getClass().getName());
        ctClass.setName(hostClass.getName() + "$");

        ctClass.setModifiers(Modifier.setPublic(ctClass.getModifiers())); // set Modifier.PUBLIC
        ctClass.setSuperclass(classPool.getCtClass(Object.class.getName()));
        ctClass.setInterfaces(new CtClass[0]);
        for (CtConstructor c : ctClass.getConstructors()) {
            ctClass.removeConstructor(c);
        }

        ClassLoader bootClassLoader = ClassLoaders.getBootstrapClassLoader();
        for (CtField ctField : ctClass.getDeclaredFields()) {
            if (ctField.getFieldInfo().getName().startsWith("this$0")) {
                ctClass.removeField(ctField);
                continue;
            }
            //check class is system jdk
            CtClass checkType = ctField.getType();
            while (checkType.isArray()) {
                checkType = ctField.getType().getComponentType();
            }

            checkState(checkType.isPrimitive() || bootClassLoader.loadClass(checkType.getName()) != null);
            ctField.setModifiers(Modifier.setPublic(ctField.getModifiers()));
            if (Modifier.isFinal(ctField.getModifiers())) {
                ctField.setModifiers(ctField.getModifiers() & (~Modifier.FINAL));
            }
        }
        //---init
        Class<?> anonymousClass = Platform.defineAnonymousClass(hostClass, ctClass.toBytecode(), new Object[0]);
        Object instance = Platform.allocateInstance(anonymousClass);
        for (Field f : instance.getClass().getFields()) {
            f.setAccessible(true);
            f.set(instance, actionFieldValues.get(f.getName()));
        }
        return instance;
    }

    /**
     * 创建匿名内部类
     *
     * @param hostClass  宿主类
     * @param classBytes 内部类的字节码
     * @param cpPatches  cpPatches
     * @return 创建并加载的匿名类，注意该类只能访问bootClassLoader中的系统类,无法访问任何用户类,该类会获得hostClass的访问域
     */
    public static Class<?> defineAnonymousClass(Class<?> hostClass, byte[] classBytes, Object[] cpPatches)
    {
        try {
            return (Class<?>) Unsafe.class.getMethod("defineAnonymousClass", Class.class, byte[].class, Object[].class)
                    .invoke(unsafe, hostClass, classBytes, cpPatches);
        }
        catch (NoSuchMethodException ignored) {
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnsupportedOperationException(e);
        }
        //jdk15+
        checkState(getVmClassVersion() >= 59, "This method can only run above Jdk15+");
        try {
            Platform.addOpenJavaModules(MethodHandles.Lookup.class, Platform.class);
            Constructor<?> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) constructor.newInstance(hostClass);

            Object options = Array.newInstance(Class.forName(MethodHandles.Lookup.class.getName() + "$ClassOption"), 0);
            lookup = (MethodHandles.Lookup) MethodHandles.Lookup.class.getMethod("defineHiddenClass", byte[].class, boolean.class, options.getClass())
                    .invoke(lookup, classBytes, true, options);
            return lookup.lookupClass();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
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

    public interface DirectBufferCloseable
    {
        void free(sun.nio.ch.DirectBuffer directBuffer);
    }

    private static final Supplier<DirectBufferCloseable> closeableSupplier = Lazys.goLazy(() -> {
        JavaClassCompiler javaClassCompiler = new JavaClassCompiler();
        String className = "DirectBufferCloseableImpl";
        String classCode = "public class DirectBufferCloseableImpl\n" +
                "            implements com.github.harbby.gadtry.base.Platform.DirectBufferCloseable\n" +
                "    {\n" +
                "        @Override\n" +
                "        public void free(sun.nio.ch.DirectBuffer buffer)\n" +
                "        {\n" +
                "            com.github.harbby.gadtry.base.MoreObjects.checkState(buffer.cleaner() != null, \"LEAK: directBuffer was not set Cleaner\");\n" +
                "            if (" + Platform.class.getName() + ".getVmClassVersion() > 52) {\n" +
                "                " + Platform.class.getName() + ".addOpenJavaModules(buffer.cleaner().getClass(), this.getClass());\n" +
                "            }\n" +
                "            buffer.cleaner().clean();\n" +
                "        }\n" +
                "    }";
        List<String> ops = getVmClassVersion() > 52 ? ImmutableList.of("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED")
                : Collections.emptyList();
        byte[] bytes = javaClassCompiler.doCompile(className, classCode, ops).getClassByteCodes().get(className);
        ByteClassLoader byteClassLoader = new ByteClassLoader(Platform.class.getClassLoader());
        Class<DirectBufferCloseable> directBufferCloseableClass = (Class<DirectBufferCloseable>) byteClassLoader.loadClass(className, bytes);
        try {
            return directBufferCloseableClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw Throwables.throwsThrowable(e);
        }
    });

    /**
     * Free DirectBuffer
     * 可能需要在编译中加入:
     * --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
     * --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED
     *
     * @param buffer DirectBuffer waiting to be released
     */
    public static void freeDirectBuffer(ByteBuffer buffer)
    {
        checkState(buffer.isDirect(), "buffer not direct");
        closeableSupplier.get().free((DirectBuffer) buffer);
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
