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

import com.github.harbby.gadtry.base.Throwables;

import javax.tools.SimpleJavaFileObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JavaSourceObject
        extends SimpleJavaFileObject
{
    private final String sourceCode;
    private final Map<String, JavaClassObject> outputFiles = new HashMap<>();

    private final String className;

    public JavaSourceObject(final String className, final String sourceCode)
    {
        super(makeURI(className), Kind.SOURCE);
        this.className = className;
        this.sourceCode = sourceCode;
    }

    public boolean isCompiled()
    {
        return !outputFiles.isEmpty();
    }

    /**
     * 含内部类
     * @return className,class bytes
     */
    public Map<String, byte[]> getClassByteCodes()
    {
        Map<String, byte[]> results = new HashMap<>();
        for (JavaClassObject outputFile : outputFiles.values()) {
            results.put(outputFile.getClassName(), outputFile.getClassBytes());
        }
        return results;
    }

    void addJavaCompiledClass(JavaClassObject outputFile)
    {
        outputFiles.put(className, outputFile);
    }

    @Override
    public Reader openReader(final boolean ignoreEncodingErrors)
            throws IOException
    {
        return new StringReader(sourceCode);
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors)
            throws IOException
    {
        return sourceCode;
    }

    @Override
    public OutputStream openOutputStream()
    {
        throw new UnsupportedOperationException();
    }

    static URI makeURI(final String canonicalClassName)
    {
        final int dotPos = canonicalClassName.lastIndexOf('.');
        final String simpleClassName = dotPos == -1 ? canonicalClassName : canonicalClassName.substring(dotPos + 1);
        try {
            return new URI(simpleClassName + Kind.SOURCE.extension);
        }
        catch (URISyntaxException e) {
            throw Throwables.throwsThrowable(e);
        }
    }
}
