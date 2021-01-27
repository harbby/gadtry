package com.github.harbby.gadtry.collection;

import org.junit.Test;

import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class FileIteratorPlusTest {
    public static final String HEADER_TXT = "/Users/duzeyan/git/gadtry/src/license/LICENSE-HEADER.txt";
    public static final String TEST_TXT = "test.txt";

    @Test
    public void empty() {
        IteratorPlus<Object> empty = IteratorPlus.empty();
        assertFalse(empty.hasNext());
        assertEquals(0, empty.size());
    }

    @Test
    public void isEmpty() {
        IteratorPlus<Object> empty = IteratorPlus.empty();
        assertTrue(empty.isEmpty());
    }

    @Test
    public void size() throws FileNotFoundException {
        assertEquals(13, new FileIterator(new File(HEADER_TXT)).size());
    }

    @Test
    public void reduce() throws IOException {
        assertEquals("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED.",
                new FileIterator(new File(HEADER_TXT))
                        .map(String::toUpperCase)
                        .reduce((acc, element) -> element.length() > acc.length() ? element : acc).get());

        assertEquals("LIMITATIONS UNDER THE LICENSE.",
                new FileIterator(new File(HEADER_TXT))
                        .map(String::toUpperCase)
                        .reduce((acc, element) -> acc.equals("") || element.length() < acc.length() ? element : acc).get());
    }

    @Test
    public void map() throws IOException {
        FileIterator fileIterator = new FileIterator(new File(HEADER_TXT));
        IteratorPlus<String> mapIterator = fileIterator.map(String::toLowerCase);
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(TEST_TXT))) {
            while (mapIterator.hasNext()) {
                fileOutputStream.write(mapIterator.next().getBytes(UTF_8));
                fileOutputStream.write(System.lineSeparator().getBytes(UTF_8));
            }
        }
        assertTrue(new File(TEST_TXT).exists());

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(TEST_TXT))));
        assertEquals("copyright (c) 2018 the gadtry authors", bufferedReader.readLine());
        bufferedReader.readLine();
        assertEquals("licensed under the apache license, version 2.0 (the \"license\");", bufferedReader.readLine());
    }

    @Test
    public void filter() throws FileNotFoundException {
        FileIterator fileIterator = new FileIterator(new File(HEADER_TXT));
        final IteratorPlus<String> iterator = fileIterator.filter(str -> str.contains("http")).map(String::trim);
        assertTrue(iterator.hasNext());
        assertEquals("http://www.apache.org/licenses/LICENSE-2.0", iterator.next());
    }

    @Test
    public void limit() throws FileNotFoundException {
        FileIterator fileIterator = new FileIterator(new File(HEADER_TXT));
        assertEquals(3, fileIterator.limit(5).limit(3).size());
    }

    public static class FileIterator implements IteratorPlus<String> {
        private String current;

        private BufferedReader reader;

        public FileIterator(File file) throws FileNotFoundException {
            Objects.requireNonNull(file);
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }

        @Override
        public boolean hasNext() {
            if (Objects.nonNull(current)) {
                return true;
            }

            try {
                this.current = reader.readLine();
            } catch (IOException exception) {
                throw new RuntimeException("An exception occurred while reading the file.");
            }

            return Objects.nonNull(current);
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException("There is no next line in this file.");
            }
            String next = this.current;
            this.current = null;
            return next;
        }

        @Override
        public void close() throws Exception {
            reader.close();
        }
    }
}