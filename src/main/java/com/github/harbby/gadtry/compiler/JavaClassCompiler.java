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

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Javac Compiler
 */
public class JavaClassCompiler
{
    private final List<String> compilerOptions;
    private final JavaCompiler compiler;
    private final JavaFileManagerImpl fileManager;
    private final DiagnosticListener<JavaFileObject> listener = diagnostic -> {
        if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
            String message = diagnostic.toString() + " code:\n" + diagnostic.getCode();
            throw new CompileException(message);
        }
    };

    public JavaClassCompiler()
    {
        this(ToolProvider.getSystemJavaCompiler(), null, false);
    }

    public JavaClassCompiler(ClassLoader classLoader)
    {
        this(ToolProvider.getSystemJavaCompiler(), classLoader, false);
    }

    public JavaClassCompiler(JavaCompiler compiler, ClassLoader classLoader, boolean debug)
    {
        this.compiler = compiler;
        this.fileManager = new JavaFileManagerImpl(compiler.getStandardFileManager(listener, null, UTF_8), classLoader);
        this.compilerOptions = Collections.singletonList(debug ? "-g:source,lines,vars" : "-g:none");
    }

    public JavaSourceObject doCompile(final String className, final String sourceCode)
    {
        JavaSourceObject javaSource = new JavaSourceObject(className, sourceCode);
        JavaCompiler.CompilationTask task = compiler.getTask(null,
                fileManager,
                listener, compilerOptions,
                null,
                Collections.singleton(javaSource));
        if (!task.call()) {
            throw new CompileException("Compilation failed");
        }
        if (!javaSource.isCompiled()) {
            throw new CompileException(className + ": Class file not created by compilation.");
        }
        return javaSource;
    }
}
