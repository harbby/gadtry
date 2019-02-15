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
package com.github.harbby.gadtry.graph;

import java.util.Collection;
import java.util.Optional;

public interface Node<E>
{
    public abstract String getId();

    public default String getName()
    {
        return getId();
    }

    public default void action(Node<E> parentNode) {}

    /**
     * Get all child nodes of the current node
     *
     * @return List child nodes
     */
    public Collection<Edge<E>> nextNodes();

    public void addNextNode(Edge<E> node);

    public Optional<Edge<E>> getNextNode(String id);

    @Override
    public abstract String toString();
}
