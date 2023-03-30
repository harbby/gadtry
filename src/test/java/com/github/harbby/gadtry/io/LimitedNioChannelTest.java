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

import com.github.harbby.gadtry.aop.MockGo;
import com.github.harbby.gadtry.base.Try;
import com.github.harbby.gadtry.collection.tuple.Tuple1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

import static com.github.harbby.gadtry.aop.MockGo.doReturn;
import static com.github.harbby.gadtry.aop.MockGo.when;
import static com.github.harbby.gadtry.aop.mockgo.MockGoArgument.anyLong;
import static java.util.Objects.requireNonNull;

public class LimitedNioChannelTest
{
    @Test
    public void limitedNioReadTest()
            throws IOException
    {
        File file = new File(requireNonNull(LimitedNioChannelTest.class.getClassLoader().getResource("blogCatalog-dataset/readme.txt")).getFile());
        int length = (int) file.length();
        Assertions.assertEquals(length, 1988);
        ByteBuffer allBytes = ByteBuffer.allocate(length);
        ByteBuffer tmp = ByteBuffer.allocate(128);
        try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
            LimitedNioChannel nioChannel = new LimitedNioChannel(fileChannel, 0, length / 2);
            while (nioChannel.read(tmp) != -1) {
                tmp.flip();
                allBytes.put(tmp);
                tmp.clear();
            }
        }
        Assertions.assertEquals(allBytes.position(), length / 2);
    }

    @Test
    public void otherLimitedNioChannelTest()
            throws IOException
    {
        SeekableByteChannel channel = MockGo.mock(SeekableByteChannel.class);
        Tuple1<Long> position = Tuple1.of(-1L);
        when(channel.size()).thenReturn(3L);
        when(channel.isOpen()).thenReturn(true);
        when(channel.position()).thenAround(p -> position.get());
        when(channel.position(anyLong())).thenAround(p -> {
            position.set((Long) p.getArgument(0));
            return p.proceed();
        });

        Try.of(() -> new LimitedNioChannel(channel, -1))
                .onSuccess(Assertions::fail)
                .matchException(IllegalStateException.class, e -> Assertions.assertEquals(e.getMessage(), "limit must be non-negative"))
                .doTry();

        LimitedNioChannel limitedNioChannel = new LimitedNioChannel(channel, 10);
        Try.of(() -> limitedNioChannel.write(ByteBuffer.allocate(1))).onSuccess(Assertions::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
        limitedNioChannel.position(5);
        Assertions.assertEquals(limitedNioChannel.position(), 5);
        Try.of(() -> limitedNioChannel.truncate(1)).onSuccess(Assertions::fail).matchException(UnsupportedOperationException.class, e -> {}).doTry();
        Assertions.assertEquals(3L, limitedNioChannel.size());
        Assertions.assertTrue(limitedNioChannel.isOpen());

        limitedNioChannel.close();
    }

    @Test
    public void limitInputStreamTest()
            throws IOException
    {
        File file = new File(requireNonNull(LimitedNioChannelTest.class.getClassLoader().getResource("blogCatalog-dataset/readme.txt")).getFile());
        int length = (int) file.length();
        Assertions.assertEquals(length, 1988);
        ByteBuffer allBytes = ByteBuffer.allocate(length);
        byte[] tmp = new byte[128];
        try (LimitInputStream limitInputStream = new LimitInputStream(new FileInputStream(file), length / 2)) {
            Assertions.assertEquals(limitInputStream.read(tmp, 0, 0), 0);
            int len = -1;
            while ((len = limitInputStream.read(tmp)) != -1) {
                allBytes.put(tmp, 0, len);
            }
            Assertions.assertEquals(limitInputStream.read(), -1);
        }
        Assertions.assertEquals(allBytes.position(), length / 2);
    }

    @Test
    public void limitInputStreamTest2()
            throws IOException
    {
        String msg = " hello world!";
        try (InputStream inputStream = new ByteBufferInputStream(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
                LimitInputStream limitInputStream = new LimitInputStream(inputStream, 100)) {
            try {
                limitInputStream.reset();
            }
            catch (IOException e) {
                Assertions.assertEquals(e.getMessage(), "Mark not set");
            }

            limitInputStream.skip(1);
            Assertions.assertTrue(limitInputStream.markSupported());
            Assertions.assertTrue(limitInputStream.available() > 0);
            limitInputStream.mark(5);
            Assertions.assertEquals(IOUtils.toString(limitInputStream, StandardCharsets.UTF_8), msg.trim());
            Assertions.assertEquals(limitInputStream.read(), -1);
            limitInputStream.reset();
            StringBuffer buffer = new StringBuffer();
            int b;
            while ((b = limitInputStream.read()) != -1) {
                buffer.append((char) b);
            }
            Assertions.assertEquals(buffer.toString(), "hello world!");
        }
    }

    @Test
    public void otherLimitInputStreamTest()
            throws IOException
    {
        InputStream inputStream = MockGo.mock(InputStream.class);
        doReturn(false).when(inputStream).markSupported();
        try (LimitInputStream limitInputStream = new LimitInputStream(inputStream, 100)) {
            limitInputStream.mark(32);
            try {
                limitInputStream.reset();
            }
            catch (IOException e) {
                Assertions.assertEquals(e.getMessage(), "Mark not supported");
            }
        }

        try {
            new LimitInputStream(inputStream, -1);
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "limit must be non-negative");
        }
    }
}
