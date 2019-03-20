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

import java.io.Serializable;

public interface Edge<E, R>
        extends Serializable
{
    public abstract Node<E, R> getOutNode();

    public abstract R getData();

    static <E, R> Edge<E, R> createEdge(Node<E, R> outNode, R edgeData)
    {
        return new Edge<E, R>()
        {
            @Override
            public Node<E, R> getOutNode()
            {
                return outNode;
            }

            @Override
            public R getData()
            {
                return edgeData;
            }
        };
    }
}
