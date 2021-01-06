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
package com.github.harbby.gadtry.io;

import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.base.Platform;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IOUtilsTest
{
    @Test
    public void copyByTestCloseGiveTrue()
            throws IOException
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("IOUtilsTest".getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copyBytes(inputStream, outputStream, 1024, true);

        Assert.assertEquals("IOUtilsTest", outputStream.toString(UTF_8.name()));
    }

    @Test
    public void copyByTestGiveFalse()
            throws IOException, InstantiationException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = AopGo.proxy(PrintStream.class)
                .byInstance(Platform.allocateInstance(PrintStream.class))
                .aop(binder -> {
                    binder.doAround(proxyContext -> {
                        byte[] buf = (byte[]) proxyContext.getArgs()[0];
                        outputStream.write(buf, (int) proxyContext.getArgs()[1], (int) proxyContext.getArgs()[2]);
                        return null;
                    }).whereMethod(method -> method.getName().equals("write") &&
                            Arrays.equals(method.getParameterTypes(),
                                    new Class[] {byte[].class, int.class, int.class}));
                })
                .build();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("IOUtilsTest".getBytes(UTF_8))) {
            IOUtils.copyBytes(inputStream, printStream, 1024, false);
            Assert.assertEquals("IOUtilsTest", outputStream.toString(UTF_8.name()));
        }
    }

    @Test
    public void copyByTestReturnCheckError()
            throws InstantiationException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = AopGo.proxy(PrintStream.class)
                .byInstance(Platform.allocateInstance(PrintStream.class))
                .aop(binder -> {
                    binder.doAround(proxyContext -> true)
                            .whereMethod(methodInfo -> methodInfo.getName().equals("checkError"));
                })
                .build();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("IOUtilsTest".getBytes(UTF_8))) {
            IOUtils.copyBytes(inputStream, printStream, 1024, false);
            Assert.assertEquals("IOUtilsTest", outputStream.toString(UTF_8.name()));
            Assert.fail();
        }
        catch (IOException e) {
            Assert.assertEquals(e.getMessage(), "Unable to write to output stream.");
        }
    }
}
