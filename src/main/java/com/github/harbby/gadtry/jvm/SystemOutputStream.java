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

import com.github.harbby.gadtry.base.Lazys;
import com.github.harbby.gadtry.memory.Platform;
import com.github.harbby.gadtry.memory.PrivilegedAction;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.function.Supplier;

public class SystemOutputStream
        extends OutputStream
{
    private static final Supplier<SystemOutputStream> systemOutGetOrInit = Lazys.goLazy(() -> {
        return (SystemOutputStream) Platform.doPrivileged(FilterOutputStream.class, new PrivilegedAction<>()
        {
            @Override
            public Object run()
                    throws Exception
            {
                Field field = FilterOutputStream.class.getDeclaredField("out");
                field.setAccessible(true);
                Object mock = ClassLoader.getSystemClassLoader().loadClass("com.github.harbby.gadtry.jvm.SystemOutputStream")
                        .getConstructor(OutputStream.class)
                        .newInstance((OutputStream) field.get(System.out));
                field.set(System.out, mock);
                field.set(System.err, mock);
                return mock;
            }
        });
    });

    public static SystemOutputStream getOrCreate()
    {
        return systemOutGetOrInit.get();
    }

    private final OutputStream out;
    private volatile boolean tryClose;

    public SystemOutputStream(
            OutputStream out)
    {
        this.out = out;
    }

    @Override
    public void write(int b)
            throws IOException
    {
        this.write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b)
            throws IOException
    {
        this.write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len)
            throws IOException
    {
        if (tryClose) {
            return;
        }
        if ((len - off) == 1 && buf[0] == 10) { //filter '\n'
            return;
        }

        int length = len;
        if (buf[buf.length - 1] == 10) {  //use trim()
            length = len - 1;
        }

        out.write(1);
        this.writeInt(length - off);
        out.write(buf, off, length);
    }

    private void writeInt(int v)
            throws IOException
    {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v) & 0xFF);
    }

    @Override
    public void close()
            throws IOException
    {
        tryClose = true;
    }

    public synchronized void release(byte[] resultBytes)
            throws IOException
    {
        out.write(0);
        this.writeInt(resultBytes.length);
        out.write(resultBytes);
        out.flush();
        //out.close();
    }
}
