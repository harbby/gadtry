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
package com.github.harbby.gadtry.collection;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileIteratorPlusTest
{
    public static final String HEADER_TXT = "src/license/LICENSE-HEADER.txt";
    public static final String TEST_TXT = "test.txt";

    @Test
    public void empty()
            throws Exception
    {
        IteratorPlus<Object> empty = IteratorPlus.empty();
        assertFalse(empty.hasNext());
        assertEquals(0, empty.size());
        assertTrue(empty.isEmpty());
        empty.close();
    }

    @Test(expected = NoSuchElementException.class)
    public void should_throw_exception_when_next_element_does_not_exist()
    {
        IteratorPlus<Object> empty = IteratorPlus.empty();
        empty.next();
    }

    @Test
    public void toStream()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            assertTrue(fileIterator.toStream().anyMatch(str -> str.contains("http")));
        }
    }

    @Test
    public void size()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            assertFalse(fileIterator.isEmpty());
            assertEquals(13, fileIterator.size());
        }
    }

    @Test
    public void should_get_shortest_sentence()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            assertEquals("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED.",
                    fileIterator
                            .map(String::toUpperCase)
                            .reduce((acc, element) -> element.length() > acc.length() ? element : acc).get());
        }
    }

    @Test
    public void should_get_longest_sentence()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            assertEquals("LIMITATIONS UNDER THE LICENSE.",
                    fileIterator
                            .map(String::toUpperCase)
                            .reduce((acc, element) -> acc.equals("") || element.length() < acc.length() ? element : acc).get());
        }
    }

    @Test
    public void map()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            IteratorPlus<String> mapIterator = fileIterator.map(String::toLowerCase);
            try (FileOutputStream fileOutputStream = new FileOutputStream(new File(TEST_TXT))) {
                while (mapIterator.hasNext()) {
                    fileOutputStream.write(mapIterator.next().getBytes(UTF_8));
                    fileOutputStream.write(System.lineSeparator().getBytes(UTF_8));
                }
            }
            assertFalse(mapIterator.hasNext());
            assertTrue(new File(TEST_TXT).exists());
        }

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(TEST_TXT))));
        assertEquals("copyright (c) 2018 the gadtry authors", bufferedReader.readLine());
        bufferedReader.readLine();
        assertEquals("licensed under the apache license, version 2.0 (the \"license\");", bufferedReader.readLine());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void should_throw_exception_when_invoke_method_remove_in_map()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            fileIterator.map(String::trim).remove();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void should_throw_exception_when_there_is_no_next_element_in_flatMap_iterator()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            final IteratorPlus<String> stringIteratorPlus = fileIterator.flatMap(str -> Arrays.asList(str.split(" ")).iterator());
            IntStream.range(0, 91).boxed().forEach(i -> stringIteratorPlus.next());
            assertFalse(stringIteratorPlus.hasNext());
            stringIteratorPlus.next();
        }
    }

    @Test
    public void filter()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            final IteratorPlus<String> iterator = fileIterator.filter(str -> str.contains("http")).map(String::trim);
            assertTrue(iterator.hasNext());
            assertEquals("http://www.apache.org/licenses/LICENSE-2.0", iterator.next());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void should_throw_exception_when_there_is_no_next_element_in_filter_iterator()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            final IteratorPlus<String> stringIteratorPlus = fileIterator.filter(str -> str.contains("http"));
            stringIteratorPlus.next();
            assertFalse(stringIteratorPlus.hasNext());
            stringIteratorPlus.next();
        }
    }

    @Test
    public void limit()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            assertEquals(3, fileIterator.limit(5).limit(3).size());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_limit_smaller_than_0()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            fileIterator.limit(-1);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void should_throw_exception_when_there_is_no_next_element_in_limit_iterator()
            throws Exception
    {
        try (FileIterator fileIterator = new FileIterator(new File(HEADER_TXT))) {
            final IteratorPlus<String> limit = fileIterator.limit(1);
            limit.next();
            assertFalse(limit.hasNext());
            limit.next();
        }
    }

    public static class FileIterator
            implements IteratorPlus<String>
    {
        private String current;

        private BufferedReader reader;

        public FileIterator(File file)
                throws FileNotFoundException
        {
            requireNonNull(file);
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }

        @Override
        public boolean hasNext()
        {
            if (current != null) {
                return true;
            }

            try {
                this.current = reader.readLine();
            }
            catch (IOException exception) {
                throw new RuntimeException("An exception occurred while reading the file.");
            }

            return current != null;
        }

        @Override
        public String next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException("There is no next line in this file.");
            }
            String next = this.current;
            this.current = null;
            return next;
        }

        @Override
        public void close()
                throws Exception
        {
            reader.close();
        }
    }
}
