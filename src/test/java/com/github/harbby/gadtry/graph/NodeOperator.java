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

import java.util.function.UnaryOperator;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class NodeOperator<T>
{
    private final UnaryOperator<T> nodeFunc;
    private T outData;
    private final String lable;

    public T getOutput()
    {
        return outData;
    }

    public NodeOperator(String lable, UnaryOperator<T> nodeFunc)
    {
        this.lable = lable;
        this.nodeFunc = nodeFunc;
    }

    public void action(NodeOperator<T> parentNode)
    {
        if (parentNode == null) { //根节点 source
            this.outData = nodeFunc.apply(null);
        }
        else {  //子节点 sink and transform
            T parentOutput = requireNonNull(parentNode.getOutput(), parentNode + " return is null");
            this.outData = nodeFunc.apply(parentOutput);
        }
    }

    public static <R> void runGraph(Graph<NodeOperator<R>, ?> graph)
    {
        graph.searchRuleRoute(route -> {
            NodeOperator<R> parentNode = route.getLastNode(1).getValue();
            route.getLastNode().getValue().action(parentNode);
            checkState(!route.containsLoop(), "The Graph contains Dead Recursion: " + route);
            return true;
        });
    }

    @Override
    public String toString()
    {
        return lable;
    }
}
