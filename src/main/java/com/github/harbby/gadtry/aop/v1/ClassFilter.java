/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.aop.v1;

import com.github.harbby.gadtry.aop.model.ClassInfo;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.annotation.Annotation;

public interface ClassFilter<T>
{
    public T withPackage(String packageName);

    public T classAnnotated(Class<? extends Annotation>... classAnnotations);

    public T classes(Class<?>... inputClass);

    /**
     * or
     *
     * @param subclasses sub class
     * @return FilterBuilder
     */
    public T subclassOf(Class<?>... subclasses);

    public T whereClass(Function<ClassInfo, Boolean> whereClass);
}
