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

import com.github.harbby.gadtry.base.Throwables;
import com.github.harbby.gadtry.collection.MutableSet;
import com.github.harbby.gadtry.collection.tuple.Tuple1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : http://socialcomputing.asu.edu/datasets/BlogCatalog
 */
public class BlogCatalogDatasetTest
{
    private static final Graph<String, Void> graph;

    static {
        Graph.GraphBuilder<String, Void> graphBuilder = ImmutableGraph.builder();
        try (CloseableIterator<String> iterator = getResourceAsIterator("blogCatalog-dataset/data/nodes.csv")) {
            while (iterator.hasNext()) {
                String line = iterator.next();
                graphBuilder.addNode(line);
            }
        }
        catch (IOException e) {
            Throwables.throwThrowable(e);
        }

        try (CloseableIterator<String> iterator = getResourceAsIterator("blogCatalog-dataset/data/edges.csv")) {
            while (iterator.hasNext()) {
                String line = iterator.next();
                String[] split = line.split(",");
                graphBuilder.addEdge(split[0], split[1]);
            }
        }
        catch (IOException e) {
            Throwables.throwThrowable(e);
        }

        graph = graphBuilder.create();
    }

    private static CloseableIterator<String> getResourceAsIterator(String path)
            throws IOException
    {
        return new FileCloseableIterator(BlogCatalogDatasetTest.class.getClassLoader().getResourceAsStream(path));
    }

    @Test
    public void searchBegien1GiveSizeMax2ReturnRoutes12548()
    {
        /*
         * 搜索从1出发所有小于3的人
         * */
        List<Route<String, Void>> routeList = graph.searchRuleRoute("1", route -> {
            boolean next = route.size() <= 2 && !route.containsLoop();
            return next;
        }).stream().filter(route -> route.size() == 2).collect(Collectors.toList());
        Assertions.assertEquals(12548, routeList.size());
    }

    @Test
    public void searchRoutesReturn1130GiveBegin1End7Max2()
    {
        /*
         * 搜索从1到7 The maximum depth is 3
         * */
        List<Route<String, Void>> routeList = new ArrayList<>();
        graph.searchRuleRoute("1", route -> {
            boolean next = route.size() < 3 && !route.containsLoop();
            if (next && route.getLastNodeId().equals("7")) {
                routeList.add(route);
                return false;
            }
            return next;
        });
        Assertions.assertEquals(6, routeList.size());
    }

    @Test
    public void searchGive1to7Max3SizeReturn6()
    {
        SearchBuilder.Mode[] modes = SearchBuilder.Mode.values();
        Assertions.assertEquals(modes.length, 3);
        for (SearchBuilder.Mode mode : modes) {
            List<Route<String, Void>> routeList = new ArrayList<>();
            SearchResult<String, Void> result = graph.search()
                    .mode(mode)
                    .beginNode("1")
                    .nextRule(route -> {
                        boolean next = route.size() < 2 && !route.containsLoop();
                        if (route.getLastNodeId().equals("7")) {
                            routeList.add(route);
                            return false;
                        }
                        return next;
                    }).search();

            Assertions.assertEquals(6, routeList.size());
            Assertions.assertEquals(11805, result.getFindNodeNumber());
        }
    }

    @Test
    public void recursiveDepthFirstGlobalRuleReturn0()
    {
        List<Route<String, Void>> routeList = new ArrayList<>();
        SearchResult<String, Void> result = graph.search()
                .mode(SearchBuilder.Mode.RECURSIVE_DEPTH_FIRST)
                .beginNode("1")
                .nextRule(route -> {
                    boolean next = route.size() < 3 && !route.containsLoop();
                    if (next && route.getLastNodeId().equals("2")) {
                        routeList.add(route);
                        return false;
                    }
                    return next;
                })
                .globalRule(context -> context.getFindNodeNumber() < 200 && (System.currentTimeMillis() - context.getSearchStartTime()) < 10_000)  //找到200个人时就结束
                .search();

        Assertions.assertEquals(1, routeList.size());
        Assertions.assertEquals(200, result.getFindNodeNumber());
        Assertions.assertTrue((System.currentTimeMillis() - result.getSearchStartTime()) < 10_000);
    }

    @Test
    public void searchApiGiveGlobalMax200()
    {
        SearchBuilder<String, Void> builder = graph.search()
                .beginNode("1")
                .nextRule(route -> {
                    return route.size() < 3 && !route.containsLoop() && !route.getLastNode(1).getValue().equals("7");
                })
                .globalRule(context -> context.getFindNodeNumber() < 200);  //找到200个人时就结束

        SearchResult<String, Void> result = builder.mode(SearchBuilder.Mode.BREADTH_FIRST).search();
        List<Route<String, Void>> routeList = result.getRoutes()
                .stream()
                .filter(route -> route.getLastNodeId().equals("7"))
                .collect(Collectors.toList());
        Assertions.assertEquals(3, routeList.size());

        routeList = builder.mode(SearchBuilder.Mode.DEPTH_FIRST)
                .search().getRoutes()
                .stream()
                .filter(route -> route.getLastNodeId().equals("7"))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, routeList.size());
    }

    @Test
    public void searchApiGlobalFindAny()
    {
        SearchResult<String, Void> result = graph.search()
                .mode(SearchBuilder.Mode.DEPTH_FIRST)
                .beginNode("1")
                .nextRule(route -> {
                    return route.size() < 3 && !route.containsLoop();
                })
                .globalRule(context -> !context.getLastRoute().getLastNodeId().equals("1047"))
                .search();  //找到200个人时就结束

        Set<String> routeList = result.getRoutes()
                .stream()
                .filter(route -> route.getLastNodeId().equals("1047"))
                .map(x -> String.join("-", x.getIds())).collect(Collectors.toSet());

        Assertions.assertEquals(MutableSet.of("1-9-1047"), routeList);
        Assertions.assertEquals(result.getFindNodeNumber(), 185);
    }

    public static interface CloseableIterator<T>
            extends Iterator<T>, Closeable
    {
    }

    public static class FileCloseableIterator
            implements CloseableIterator<String>
    {
        private final BufferedReader reader;

        private final Tuple1<String> line = new Tuple1<>(null);

        public FileCloseableIterator(InputStream inputStream)
                throws IOException
        {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
            line.f1 = reader.readLine();
        }

        @Override
        public boolean hasNext()
        {
            return line.f1 != null;
        }

        @Override
        public String next()
        {
            String old = line.f1;
            try {
                line.f1 = reader.readLine();
                return old;
            }
            catch (IOException e) {
                throw Throwables.throwThrowable(e);
            }
        }

        @Override
        public void close()
                throws IOException
        {
            reader.close();
        }
    }
}
