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
package com.github.harbby.gadtry.spi;

import com.github.harbby.gadtry.base.Iterators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceConfigurationError;
import java.util.Set;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class ServiceLoad
{
    private ServiceLoad() {}

    private static final String PREFIX = "META-INF/services/";   // copy form ServiceLoader

    public static <T> Iterable<T> serviceLoad(Class<T> service, ClassLoader loader)
    {
        requireNonNull(service, "service is null");
        requireNonNull(loader, "loader is null");
        final String fullName = PREFIX + service.getName();
        Set<String> names = new HashSet<>();
        try (InputStream in = loader.getResourceAsStream(fullName)) {
            if (in == null) {
                return Collections.emptyList();
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            int lc = 1;
            do {
                lc = parseLine(service, loader.getResource(fullName), r, lc, names);
            }
            while (lc >= 0);
        }
        catch (IOException e) {
            throw new ServiceConfigurationError(service.getName() + ": " + "Error reading configuration file", e);
        }
        return () -> Iterators.map(names.iterator(), className -> {
            try {
                Class<?> c = Class.forName(className, false, loader);
                checkState(service.isAssignableFrom(c), ServiceConfigurationError::new, "Provider %s not a subtype", className);
                return service.cast(c.getConstructor().newInstance());
            }
            catch (ClassNotFoundException e) {
                throw new ServiceConfigurationError("Provider " + className + " not found");
            }
            catch (Exception e) {
                throw new ServiceConfigurationError(service.getName() + ": " + "Provider " + className + " could not be instantiated", e);
            }
        });
    }

    /**
     * copy to java8 and java11
     */
    private static int parseLine(Class<?> service, URL u, BufferedReader r, int lc, Set<String> names)
            throws IOException
    {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
                fail(service, u, lc, "Illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            int start = Character.charCount(cp);
            for (int i = start; i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
            }
            names.add(ln);
        }
        return lc + 1;
    }

    private static void fail(Class<?> service, URL u, int line, String msg)
            throws ServiceConfigurationError
    {
        fail(service, u + ":" + line + ": " + msg);
    }

    private static void fail(Class<?> service, String msg)
            throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }
}
