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
package com.github.harbby.gadtry.graph.impl;

import com.github.harbby.gadtry.collection.MutableList;
import com.github.harbby.gadtry.graph.Node;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * throws StackOverflowError
 */
public class GraphUtil
{
    private GraphUtil() {}

    public static List<String> printShow(List<? extends Node<?, ?>> firstNodes)
    {
        return printBuilder(firstNodes);  //MutableList.copyOf()
    }

    public static List<String> printShow(Node<?, ?>... firstNodes)
    {
        return printShow(MutableList.of(firstNodes));
    }

    private static class NextStep
    {
        private final Node<?, ?> node;
        private final String header;

        public NextStep(Node<?, ?> node, String header)
        {
            this.node = node;
            this.header = header;
        }

        public Node<?, ?> getNode()
        {
            return node;
        }

        public String getHeader()
        {
            return header;
        }
    }

    private static List<String> printBuilder(List<? extends Node<?, ?>> beginNodes)
    {
        Deque<NextStep> queue = new LinkedList<>();
        List<String> builder = new LinkedList<>();
        builder.add("/");

        Set<String> looped = new HashSet<>();
        final String beginHeader = "├";

        beginNodes.forEach(x -> queue.addLast(new NextStep(x, beginHeader)));
        while (!queue.isEmpty()) {
            NextStep nextStep = queue.pop();
            String header = nextStep.getHeader();
            Node<?, ?> node = nextStep.getNode();

            String line = header + "────" + node.getId();
            builder.add(line);

            String f = (node.nextNodes().size() > 1) ? "├" : "└";
            String nextHeader = getNextLineHeader(line, node.getId()) + f;

            //push next nodes...
            if (node.nextNodes().size() == 1) {  //end
                nextHeader = nextHeader.substring(0, nextHeader.length() - 1) + "└";
            }
            String finalNextHeader = nextHeader;
            node.nextNodes().stream().filter(edge -> {
                String rowkey = node.getId() + "->" + edge.getOutNode().getId();
                return looped.add(rowkey);
            }).forEach(edge -> queue.push(new NextStep(edge.getOutNode(), finalNextHeader)));
        }

        return builder;
    }

    private static String getNextLineHeader(String lastLine, String id)
    {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < lastLine.length() - id.length(); i++) {
            char a1 = lastLine.charAt(i);
            switch (a1) {
                case '├':
                case '│':
                    buffer.append("│");
                    break;
                default:
                    buffer.append(" ");
            }
        }
        return buffer.toString();
    }
}
