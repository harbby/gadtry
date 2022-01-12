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

import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphUtil
{
    private GraphUtil() {}

    public static List<String> printShow(List<Node<?, ?>> firstNodes)
    {
        return printBuilder(firstNodes);
    }

    public static List<String> printShow(Node<?, ?>... firstNodes)
    {
        return printShow(ImmutableList.copy(firstNodes));
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

    private static List<String> printBuilder(List<Node<?, ?>> beginNodes)
    {
        Deque<NextStep> queue = new LinkedList<>();
        List<String> builder = new LinkedList<>();
        Set<String> looped = new HashSet<>();
        builder.add("/");
        pushNext(queue, beginNodes, "");

        NextStep nextStep;
        while ((nextStep = queue.pollFirst()) != null) {
            Node<?, ?> node = nextStep.getNode();
            String line = nextStep.getHeader() + "────" + node.getId();
            builder.add(line);

            String nextHeader = getNextLineHeader(line, node.getId());
            //push next nodes...
            List<Node<?, ?>> nexts = node.nextNodes().stream().filter(edge -> {
                String path = node.getId() + "->" + edge.getOutNode().getId();
                return looped.add(path);
            }).map(Edge::getOutNode).collect(Collectors.toList());
            pushNext(queue, nexts, nextHeader);
        }
        return builder;
    }

    private static void pushNext(Deque<NextStep> queue, List<Node<?, ?>> nexts, String nextHeader)
    {
        for (int i = nexts.size() - 1; i >= 0; i--) {
            if (i == nexts.size() - 1) {  //end
                queue.addFirst(new NextStep(nexts.get(i), nextHeader + "└"));
            }
            else {
                queue.addFirst(new NextStep(nexts.get(i), nextHeader + "├"));
            }
        }
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
