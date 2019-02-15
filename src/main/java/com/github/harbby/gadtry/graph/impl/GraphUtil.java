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

import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.graph.Edge;
import com.github.harbby.gadtry.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphUtil
{
    private GraphUtil() {}

    public static class PrintContext
    {
        private final List<String> builder = new ArrayList<>();
        private final Set<String> looped = new HashSet<>();

        private List<Node> nodes;
        private String header;
    }

    public static void printShow(List<String> builder, List<Node> firstNodes)
    {
        Set<String> looped = new HashSet<>();
        printBuilder(builder, looped, ImmutableList.copy(firstNodes), "├");  //ImmutableList.copyOf()
    }

    public static void printShow(List<String> builder, Node... firstNodes)
    {
        printShow(builder, ImmutableList.of(firstNodes));
    }

    private static void printBuilder(List<String> builder, Set<String> looped, List<Node> nodes, String header)
    {
        for (int i = 0; i < nodes.size(); i++) {
            Node<?, ?> node = nodes.get(i);

            if (i == nodes.size() - 1) {  //end
                header = header.substring(0, header.length() - 1) + "└";
            }
            String name = node.getName(); //+ "[" + node.getId() + "]";
            String line = header + "────" + name;
            builder.add(line);

            List<Node> nexts = node.nextNodes().stream().filter(edge -> {
                String rowkey = node.getId() + "->" + edge.getOutNode().getId();
                return looped.add(rowkey);
            }).map(Edge::getOutNode).collect(Collectors.toList());

            String f = (node.nextNodes().size() > 1) ? "├" : "└";
            printBuilder(builder, looped, nexts, getNextLineHeader(line, name) + f);
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
