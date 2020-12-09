package com.github.harbby.gadtry.jvm;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Random;

public class JvmAgent
{
    private static final String CLASS_NAME = JVMLauncher.class.getName();

    public static void premain(String agentArgs, Instrumentation inst)
    {
        inst.addTransformer(new ClassFileTransformer()
        {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer)
                    throws IllegalClassFormatException
            {
                if (!CLASS_NAME.replace(".", "/").equals(className)) {
                    return null;
                }
                ClassPool cp = ClassPool.getDefault();
                cp.appendClassPath(new LoaderClassPath(loader));

                try {
                    CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
                    cc.setName(CLASS_NAME + ".demo" + new Random().nextInt(10));
                    return cc.toBytecode();
                }
                catch (IOException | CannotCompileException e) {
                    return null;
                }
            }
        });
    }
}
