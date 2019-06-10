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
package com.github.harbby.gadtry.graph;

import java.io.Serializable;

import static com.github.harbby.gadtry.base.MoreObjects.toStringHelper;

public interface Edge<E, R>
        extends Serializable
{
    public abstract Node<E, R> getInNode();

    public abstract Node<E, R> getOutNode();

    public abstract R getData();

    public static class EdgeImpl<E, R>
            implements Edge<E, R>
    {
        private final Node<E, R> inNode;
        private final Node<E, R> outNode;
        private final R edgeData;

        private EdgeImpl(Node<E, R> inNode, Node<E, R> outNode, R edgeData)
        {
            this.inNode = inNode;
            this.outNode = outNode;
            this.edgeData = edgeData;
        }

        @Override
        public Node<E, R> getInNode()
        {
            return inNode;
        }

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

        @Override
        public String toString()
        {
            return toStringHelper(this)
                    .add("inNode", inNode)
                    .add("outNode", outNode)
                    .add("edgeData", edgeData)
                    .toString();
        }
    }

    static <E, R> Edge<E, R> createEdge(Node<E, R> inNode, Node<E, R> outNode, R edgeData)
    {
        return new EdgeImpl<>(inNode, outNode, edgeData);
    }

    static <E, R> Edge<E, R> createEdge(Node<E, R> inNode, Node<E, R> outNode)
    {
        return createEdge(inNode, outNode, null);
    }
}
