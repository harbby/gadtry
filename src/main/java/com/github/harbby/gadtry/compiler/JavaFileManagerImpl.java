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
package com.github.harbby.gadtry.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

class JavaFileManagerImpl
        extends ForwardingJavaFileManager<JavaFileManager>
{
    private final ClassLoader classLoader;

    protected JavaFileManagerImpl(JavaFileManager fileManager, ClassLoader classLoader)
    {
        super(fileManager);
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader(Location location)
    {
        return classLoader;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
            throws IOException
    {
        return super.list(location,
                packageName,
                kinds.stream()
                        .filter(x -> x != Kind.SOURCE)
                        .collect(Collectors.toSet()),
                recurse);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
            throws IOException
    {
        checkState(sibling instanceof JavaSourceObject, "not a JavaFileObjectImpl: " + sibling);
        JavaClassObject outputFile = new JavaClassObject(className, Kind.CLASS);
        ((JavaSourceObject) sibling).addJavaCompiledClass(outputFile);
        return outputFile;
    }
}
