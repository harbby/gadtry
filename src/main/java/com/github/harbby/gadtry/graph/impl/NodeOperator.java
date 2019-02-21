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

import com.github.harbby.gadtry.graph.Data;
import com.github.harbby.gadtry.graph.Graph;

import java.util.function.UnaryOperator;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class NodeOperator<T>
        implements Data
{
    private final UnaryOperator<T> nodeFunc;
    private final ThreadLocal<T> data = new ThreadLocal<>();

    public T getOutput()
    {
        return data.get();
    }

    public NodeOperator(UnaryOperator<T> nodeFunc)
    {
        this.nodeFunc = nodeFunc;
    }

    public void action(NodeOperator<T> parentNode)
    {
        if (parentNode == null) { //根节点 source
            T outData = nodeFunc.apply(null);
            data.set(outData);
        }
        else {  //子节点 sink and transform
            T parentOutput = requireNonNull(parentNode.getOutput(), parentNode + " return is null");
            T outData = nodeFunc.apply(parentOutput);  //进行变换
            data.set(outData);
        }
    }

    public static <R> void runGraph(Graph<NodeOperator<R>, Data> graph)
    {
        graph.searchRuleRoute(route -> {
            NodeOperator<R> parentNode = route.getLastNode().getData();
            route.getEndNode().getData().action(parentNode);
            checkState(route.containsDeadRecursion(), "The Graph contains Dead Recursion: " + route);
            return true;
        });
    }
}
