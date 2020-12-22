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

import com.github.harbby.gadtry.collection.MutableSet;
import org.junit.Assert;
import org.junit.Test;
import sun.nio.cs.StreamDecoder;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Throwables.throwsException;
import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author : http://socialcomputing.asu.edu/datasets/BlogCatalog
 */
public class BlogCatalogDataset
{
    private static final Graph<Void, Void> graph;

    static {
        File dataDir = new File(BlogCatalogDataset.class.getClassLoader().getResource("blogCatalog-dataset/data").getFile());
        Graph.GraphBuilder<Void, Void> graphBuilder = ImmutableGraph.builder();

        try (CloseableIterator<String> iterator = new FileCloseableIterator(new File(dataDir, "nodes.csv"), 4096)) {
            while (iterator.hasNext()) {
                String line = iterator.next();
                graphBuilder.addNode(line);
            }
        }
        catch (IOException e) {
            throwsThrowable(e);
        }

        try (CloseableIterator<String> iterator = new FileCloseableIterator(new File(dataDir, "edges.csv"), 4096)) {
            while (iterator.hasNext()) {
                String line = iterator.next();
                String[] split = line.split(",");
                graphBuilder.addEdge(split[0], split[1]);
            }
        }
        catch (IOException e) {
            throwsThrowable(e);
        }

        graph = graphBuilder.create();
    }

    @Test
    public void searchBegien1GiveSizeMax2ReturnRoutes12548()
    {
        /*
         * 搜索从1出发所有小于3的人
         * */
        List<Route<Void, Void>> routeList = graph.searchRuleRoute("1", route -> {
            boolean next = route.size() <= 2 && !route.findDeadLoop();
            return next;
        }).stream().filter(route -> route.size() == 2).collect(Collectors.toList());
        Assert.assertEquals(12548, routeList.size());
    }

    @Test
    public void searchRoutesReturn1130GiveBegin1End7Max2()
    {
        /*
         * 搜索从1到7 The maximum depth is 3
         * */
        List<Route<Void, Void>> routeList = new ArrayList<>();
        graph.searchRuleRoute("1", route -> {
            boolean next = route.size() < 3 && !route.findDeadLoop();
            if (next && route.getLastNodeId().equals("7")) {
                routeList.add(route);
                return false;
            }
            return next;
        });
        Assert.assertEquals(6, routeList.size());
    }

    @Test
    public void searchGive1to7Max3SizeReturn6()
    {
        SearchBuilder.Optimizer[] optimizers = new SearchBuilder.Optimizer[] {
                SearchBuilder.Optimizer.DEPTH_FIRST,
                SearchBuilder.Optimizer.BREADTH_FIRST,
                SearchBuilder.Optimizer.RECURSIVE_DEPTH_FIRST
        };

        for (SearchBuilder.Optimizer optimizer : optimizers) {
            List<Route<Void, Void>> routeList = new ArrayList<>();
            SearchResult<Void, Void> result = graph.search()
                    .optimizer(optimizer)
                    .beginNode("1")
                    .nextRule(route -> {
                        boolean next = route.size() < 2 && !route.findDeadLoop();
                        if (route.getLastNodeId().equals("7")) {
                            routeList.add(route);
                            return false;
                        }
                        return next;
                    }).search();

            Assert.assertEquals(6, routeList.size());
            Assert.assertEquals(11805, result.getFindNodeNumber());
        }
    }

    @Test
    public void recursiveDepthFirstGlobalRuleReturn0()
    {
        List<Route<Void, Void>> routeList = new ArrayList<>();
        SearchResult<Void, Void> result = graph.search()
                .optimizer(SearchBuilder.Optimizer.RECURSIVE_DEPTH_FIRST)
                .beginNode("1")
                .nextRule(route -> {
                    boolean next = route.size() < 3 && !route.findDeadLoop();
                    if (next && route.getLastNodeId().equals("2")) {
                        routeList.add(route);
                        return false;
                    }
                    return next;
                })
                .globalRule(context -> context.getFindNodeNumber() < 200 && (System.currentTimeMillis() - context.getSearchStartTime()) < 10_000)  //找到200个人时就结束
                .search();

        Assert.assertEquals(1, routeList.size());
        Assert.assertEquals(200, result.getFindNodeNumber());
        Assert.assertTrue((System.currentTimeMillis() - result.getSearchStartTime()) < 10_000);
    }

    @Test
    public void searchApiGiveGlobalMax200()
    {
        SearchBuilder<Void, Void> builder = graph.search()
                .beginNode("1")
                .nextRule(route -> {
                    return route.size() < 3 && !route.findDeadLoop() && !route.getLastNode(1).getId().equals("7");
                })
                .globalRule(context -> context.getFindNodeNumber() < 200);  //找到200个人时就结束

        SearchResult<Void, Void> result = builder.optimizer(SearchBuilder.Optimizer.BREADTH_FIRST).search();
        List<Route<Void, Void>> routeList = result.getRoutes()
                .stream()
                .filter(route -> route.getLastNodeId().equals("7"))
                .collect(Collectors.toList());
        Assert.assertEquals(3, routeList.size());

        routeList = builder.optimizer(SearchBuilder.Optimizer.DEPTH_FIRST)
                .search().getRoutes()
                .stream()
                .filter(route -> route.getLastNodeId().equals("7"))
                .collect(Collectors.toList());
        Assert.assertEquals(2, routeList.size());
    }

    @Test
    public void searchApiGlobalFindAny()
    {
        SearchResult<Void, Void> result = graph.search()
                .optimizer(SearchBuilder.Optimizer.DEPTH_FIRST)
                .beginNode("1")
                .nextRule(route -> {
                    return route.size() < 3 && !route.findDeadLoop();
                })
                .globalRule(context -> !context.getLastRoute().getLastNodeId().equals("1047"))
                .search();  //找到200个人时就结束

        Set<String> routeList = result.getRoutes()
                .stream()
                .filter(route -> route.getLastNodeId().equals("1047"))
                .map(x -> String.join("-", x.getIds())).collect(Collectors.toSet());

        Assert.assertEquals(MutableSet.of("1-9-1047"), routeList);
        Assert.assertEquals(result.getFindNodeNumber(), 185);
    }

    public static interface CloseableIterator<T>
            extends Iterator<T>, Closeable
    {
    }

    public static class FileCloseableIterator
            implements CloseableIterator<String>
    {
        private final FileInputStream inputStream;
        private final BufferedReader reader;
        private final FileChannel channel;

        private String line;

        public FileCloseableIterator(File path, int buffSize)
                throws IOException
        {
            this(Paths.get(path.toURI()), buffSize);
        }

        public FileCloseableIterator(Path path, int buffSize)
                throws IOException
        {
            this.inputStream = new FileInputStream(path.toFile());
            this.channel = inputStream.getChannel();
            StreamDecoder streamDecoder = StreamDecoder.forDecoder(channel, UTF_8.newDecoder(), buffSize);
            this.reader = new BufferedReader(streamDecoder);
            this.line = reader.readLine();
        }

        @Override
        public boolean hasNext()
        {
            return line != null;
        }

        @Override
        public String next()
        {
            String old = line;
            try {
                line = reader.readLine();
                return old;
            }
            catch (IOException e) {
                throw throwsException(e);
            }
        }

        @Override
        public void close()
                throws IOException
        {
            if (reader != null) {
                reader.close();
            }
            if (channel != null) {
                channel.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
