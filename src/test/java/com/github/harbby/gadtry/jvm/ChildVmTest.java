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
package com.github.harbby.gadtry.jvm;

import com.github.harbby.gadtry.aop.MockGo;
import com.github.harbby.gadtry.base.Throwables;
import com.github.harbby.gadtry.base.Try;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;

import static com.github.harbby.gadtry.jvm.JVMLauncherImpl.VM_HEADER;

public class ChildVmTest
{
    @Test
    public void premainTest()
            throws Exception
    {
        Instrumentation instrumentation = MockGo.mock(Instrumentation.class);
        JvmAgent.premain(JVMLauncher.class.getName() + ":newClassName", instrumentation);
        Assert.assertNotNull(Class.forName(JVMLauncher.class.getPackage().getName() + ".newClassName"));
    }

    @Test
    public void writeEncodeTest()
            throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ChildVMSystemOutputStream outputStream = new ChildVMSystemOutputStream(new PrintStream(byteArrayOutputStream));
        outputStream.writeVmHeader();
        outputStream.write('a');
        outputStream.write((" hello" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        outputStream.release(false, "success".getBytes(StandardCharsets.UTF_8));
        Try.of(() -> outputStream.release(false, "".getBytes(StandardCharsets.UTF_8)))
                .onSuccess(Assert::fail)
                .matchException(IllegalStateException.class, e -> Assert.assertEquals(e.getMessage(), "channel closed")).doTry();
        outputStream.write('a');
        outputStream.write((" hook" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        //------------------------
        ChildVMChannelInputStream childVMChannelInputStream = new ChildVMChannelInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        Assert.assertEquals(childVMChannelInputStream.readLine(), "a hello");
        Assert.assertEquals(childVMChannelInputStream.readLine(), "a hook");
        Assert.assertTrue(childVMChannelInputStream.isSuccess());
        Assert.assertEquals(new String(childVMChannelInputStream.readResult(), StandardCharsets.UTF_8), "success");
        Assert.assertNull(childVMChannelInputStream.readLine());
    }

    @Test
    public void vmFailedWriteEncodeTest()
            throws IOException
    {
        String errorMsg = Throwables.getStackTraceAsString(new UnsupportedOperationException());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ChildVMSystemOutputStream outputStream = new ChildVMSystemOutputStream(new PrintStream(byteArrayOutputStream));
        outputStream.writeVmHeader();
        outputStream.release(true, errorMsg.getBytes(StandardCharsets.UTF_8));
        //---------------
        ChildVMChannelInputStream childVMChannelInputStream = new ChildVMChannelInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        Assert.assertNull(childVMChannelInputStream.readLine());
        Assert.assertFalse(childVMChannelInputStream.isSuccess());
        Assert.assertEquals(new String(childVMChannelInputStream.readResult(), StandardCharsets.UTF_8), errorMsg);
    }

    @Test
    public void errorEncodeTest()
            throws IOException
    {
        String errorMsg = Throwables.getStackTraceAsString(new UnsupportedOperationException());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(VM_HEADER);
        new DataOutputStream(byteArrayOutputStream).writeInt(-3);
        //---------------
        ChildVMChannelInputStream childVMChannelInputStream = new ChildVMChannelInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        try {
            childVMChannelInputStream.readLine();
        }
        catch (UnsupportedEncodingException e) {
            Assert.assertEquals(e.getMessage(), "Protocol error -3");
        }
    }
}
