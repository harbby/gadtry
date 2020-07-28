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
package com.github.harbby.gadtry.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ClassLoaders
{
    private ClassLoaders() {}

    public static ClassLoader latestUserDefinedLoader()
    {
        try {
            try {
                Method method = Class.forName("sun.misc.VM").getDeclaredMethod("latestUserDefinedLoader");
                method.setAccessible(true);
                return (ClassLoader) method.invoke(null);
            }
            catch (ClassNotFoundException e) {
                Method method = Class.forName("jdk.internal.misc.VM").getDeclaredMethod("latestUserDefinedLoader");
                method.setAccessible(true);
                return (ClassLoader) method.invoke(null);
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new UnsupportedOperationException("this jdk " + System.getProperty("java.version")
                    + " not support latestUserDefinedLoader");
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            throw Throwables.throwsThrowable(e);
        }
    }
}
