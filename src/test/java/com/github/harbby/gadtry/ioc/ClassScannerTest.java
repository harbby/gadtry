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
package com.github.harbby.gadtry.ioc;

import com.github.harbby.gadtry.spi.ClassScanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Set;

@Deprecated
@ExtendWith({})
public class ClassScannerTest
        implements Serializable
{
    @Test
    public void scanTest()
            throws IOException, URISyntaxException
    {
        ClassScanner scanner = ClassScanner.builder("com.github.harbby.gadtry")
                .subclassOf(Serializable.class)
                .annotated(ExtendWith.class)
                .classLoader(this.getClass().getClassLoader())
                .filter(aClass -> !aClass.isEnum())
                .scan();
        Set<Class<?>> classSet = scanner.getClasses();
        Assertions.assertTrue(!classSet.isEmpty());
        Assertions.assertTrue(classSet.contains(ClassScannerTest.class));
    }

    @Test
    public void getFilterTest()
            throws IOException, URISyntaxException
    {
        ClassScanner scanner = ClassScanner.builder("com.github.harbby.gadtry").scan();

        Set<Class<?>> classSet = scanner.getClassWithAnnotated(ExtendWith.class, Deprecated.class);
        Assertions.assertTrue(classSet.contains(ClassScannerTest.class));

        classSet = scanner.getClassWithSubclassOf(Serializable.class);
        Assertions.assertTrue(classSet.contains(ClassScannerTest.class));
        for (Class<?> aClass : classSet) {
            Assertions.assertTrue(Serializable.class.isAssignableFrom(aClass));
        }
    }

    @Test
    public void jarProtocolScanTest()
            throws IOException, URISyntaxException
    {
        Set<Class<?>> classes = ClassScanner.scanClasses("com.github.harbby.gadtry.ioc", this.getClass().getClassLoader());
        Assertions.assertTrue(classes.size() > 0);
    }

    @Test
    public void annotatedOfTest()
            throws IOException, URISyntaxException
    {
        ClassScanner scanner = ClassScanner.builder("com.github.harbby.gadtry")
                .annotated(Deprecated.class, Test.class)
                .scan();
        Set<Class<?>> classSet = scanner.getClasses();
        Assertions.assertTrue(!classSet.isEmpty());
        Assertions.assertTrue(classSet.contains(ClassScannerTest.class));
        for (Class<?> aClass : classSet) {
            boolean check = aClass.getAnnotation(Deprecated.class) != null || aClass.getAnnotation(Test.class) != null;
            Assertions.assertTrue(check);
        }
    }

    @Test
    public void filterTest()
            throws IOException, URISyntaxException
    {
        ClassScanner scanner = ClassScanner.builder("com.github.harbby.gadtry")
                .filter(aClass -> aClass.getName().equals(ClassScannerTest.class.getName()))
                .scan();
        Set<Class<?>> classSet = scanner.getClasses();
        Assertions.assertEquals(1, classSet.size());
        Assertions.assertTrue(classSet.contains(ClassScannerTest.class));
    }
}
