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

import com.github.harbby.gadtry.base.Serializables;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class JVMLauncherMainTest
{
    @Test
    public void main() throws Exception
    {
        InputStream old = System.in;
        PrintStream oldOut = System.out;
        VmCallable<String> vmCallable = () -> {
            throw new RuntimeException("deno");
        };

        PrintStream stream = new PrintStream(oldOut)
        {
            @Override
            public void write(int b)
            {
                throw new IllegalArgumentException("port out of range:-1");
            }
        };
        System.setOut(stream);
        System.setErr(stream);
        try {
            System.setIn(new ByteArrayInputStream(Serializables.serialize(vmCallable)));
            JVMLauncher.main(new String[]{"-1"});
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "port out of range:-1");
        }
        finally {
            System.setIn(old);
            System.setOut(oldOut);
            System.setErr(oldOut);
        }
    }
}
