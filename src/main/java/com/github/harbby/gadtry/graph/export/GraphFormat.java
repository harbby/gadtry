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
package com.github.harbby.gadtry.graph.export;

import com.github.harbby.gadtry.graph.Graph;

/**
 * Factory of a {@link GraphExporter}. Built-in formats live in {@link GraphFormats};
 * a custom format is usually just a constructor reference: {@code graph.export(MyExporter::new)}.
 */
@FunctionalInterface
public interface GraphFormat<N, E, X extends GraphExporter<N, E>>
{
    X create(Graph<N, E> graph);
}
