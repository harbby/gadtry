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
package com.github.harbby.gadtry.graph.impl;

import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.github.harbby.gadtry.base.Strings.isBlank;

public class NodeImpl<E, R>
        implements Node<E, R>
{
    private final String id;
    private final String name;
    private final Map<String, Edge<E, R>> nextNodes;
    private final E data;

    public NodeImpl(String id, String name, Map<String, Edge<E, R>> nextNodes, E data)
    {
        this.id = id;
        this.name = name;
        this.nextNodes = Collections.unmodifiableMap(nextNodes);
        this.data = data;
    }

    @Override
    public E getData()
    {
        return data;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return isBlank(name) ? getId() : getId() + "[" + name + "]";
    }

    @Override
    public Collection<Edge<E, R>> nextNodes()
    {
        return nextNodes.values();
    }

    @Override
    public Optional<Edge<E, R>> getNextNode(String id)
    {
        return Optional.ofNullable(nextNodes.get(id));
    }

    @Override
    public String toString()
    {
        return "node:" + getId();
    }
}
