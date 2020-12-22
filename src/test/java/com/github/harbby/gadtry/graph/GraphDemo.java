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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

/**
 * 某图计算题目
 */
public class GraphDemo
{
    private Graph<Void, EdgeData> graph;

    @Before
    public void before()
    {
        //AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7
        this.graph = ImmutableGraph.<Void, EdgeData>builder()
                .addNode("A")
                .addNode("B")
                .addNode("C")
                .addNode("D")
                .addNode("E")
                .addEdge("A", "B", new EdgeData(5))  //A到B距离为5
                .addEdge("B", "C", new EdgeData(4))
                .addEdge("C", "D", new EdgeData(8))
                .addEdge("D", "C", new EdgeData(8))
                .addEdge("D", "E", new EdgeData(6))
                .addEdge("A", "D", new EdgeData(5))
                .addEdge("C", "E", new EdgeData(2))
                .addEdge("E", "B", new EdgeData(3))
                .addEdge("A", "E", new EdgeData(7))
                .create();

        //---打印出已A为起点的网络
        graph.printShow("A").forEach(System.out::println);
    }

    private class EdgeData
    {
        private final long distance;

        public EdgeData(long distance)
        {
            this.distance = distance;
        }

        public long getDistance()
        {
            return distance;
        }
    }

    @Test
    public void test1_5()
            throws Exception
    {
        //1-5
        Assert.assertEquals(9L, getRouteDistance(graph.getRoute("A", "B", "C")));
        Assert.assertEquals(5L, getRouteDistance(graph.getRoute("A", "D")));
        Assert.assertEquals(13L, getRouteDistance(graph.getRoute("A", "D", "C")));
        Assert.assertEquals(22L, getRouteDistance(graph.getRoute("A", "E", "B", "C", "D")));
        try {
            graph.getRoute("A", "E", "D");  //5
        }
        catch (Exception e) {
            Assert.assertEquals("NO SUCH ROUTE", e.getMessage());
        }
    }

    @Test
    public void test6SearchMax3Return2CToC()
    {
        //6  找出C到C经过最多3个点的路线
        List<Route<Void, EdgeData>> routes = graph.searchRuleRoute("C", "C", route -> route.size() <= 3);
        Assert.assertEquals(2, routes.size());
        List<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toList());
        Assert.assertTrue(paths.contains("C-D-C") && paths.contains("C-E-B-C"));
    }

    @Test
    public void test7SearchEq4Return3AToC()
    {
        //7 找出A到C恰好经过4个点的路线
        List<Route<Void, EdgeData>> routes = graph.searchRuleRoute("A", "C", route -> route.size() <= 4)
                .stream().filter(x -> x.size() == 4)
                .collect(Collectors.toList());

        Assert.assertEquals(3, routes.size());
        Set<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toSet());
        Assert.assertEquals(paths, MutableSet.of("A-B-C-D-C", "A-D-C-D-C", "A-D-E-B-C"));
    }

    @Test
    public void searchApi7SearchEq4Return3AToC()
    {
        //7 找出A到C恰好经过4个点的路线
        List<Route<Void, EdgeData>> routes = graph.search()
                .beginNode("A")
                .endNode("C")
                .optimizer(SearchBuilder.Optimizer.BREADTH_FIRST)
                .nextRule(route -> route.size() <= 4)
                .globalRule(searchContext -> searchContext.getSearchTime() < 5_000)
                .search()
                .getRoutes()
                .stream().filter(x -> x.size() == 4).collect(Collectors.toList());

        Set<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toSet());
        Assert.assertEquals(paths, MutableSet.of("A-B-C-D-C", "A-D-C-D-C", "A-D-E-B-C"));
    }

    @Test
    public void test8SearchMinRouteReturn9AToC()
    {
        //8 找出A到C的最短距离线路
        List<Route<Void, EdgeData>> minRoutes = searchMinRoute(graph, "A", "C");
        long distances = getRouteDistance(minRoutes.get(0));

        Assert.assertEquals(9L, distances);
    }

    @Test
    public void test9SearchMinRouteReturn9BToB()
    {
        //9 找出B到B的最短距离线路
        List<Route<Void, EdgeData>> minRoutes = searchMinRoute(graph, "B", "B");
        long distances = getRouteDistance(minRoutes.get(0));
        Assert.assertEquals(9L, distances);
    }

    @Test
    public void test10SearchMaxDistances30RouteReturn7CToC()
    {
        //10 找出c to c 距离30以内的线路
        List<Route<Void, EdgeData>> routes = graph.searchRuleRoute("C", "C", route -> {
            long distances = getRouteDistance(route);
            return distances < 30;
        });
        Assert.assertEquals(7, routes.size());
    }

    @Test
    public void searchApi10SearchGiveMaxDistances30Return7ByCToC()
    {
        //10 找出c to c 距离30以内的线路
        List<Route<Void, EdgeData>> routes = graph.search()
                .beginNode("C")
                .endNode("C")
                .optimizer(SearchBuilder.Optimizer.DEPTH_FIRST)
                .nextRule(route -> {
                    long distances = getRouteDistance(route);
                    return distances < 30;
                })
                .globalRule(searchContext -> searchContext.getSearchTime() < 5_000)
                .search()
                .getRoutes();
        Assert.assertEquals(7, routes.size());
    }

    private static long getRouteDistance(Route<Void, EdgeData> route)
    {
        return route.getEdges().stream().mapToLong(edge -> edge.getData().getDistance()).sum();
    }

    private static List<Route<Void, EdgeData>> searchMinRoute(Graph<Void, EdgeData> graph, String first, String end)
    {
        List<Route<Void, EdgeData>> minRoutes = new ArrayList<>();
        List<Route<Void, EdgeData>> searchRoutes = graph.searchRuleRoute(first, route -> {
            if (end.equals(route.getLastNodeId())) {
                if (minRoutes.isEmpty()) {
                    minRoutes.add(route);
                    return false;
                }

                long minDistance = getRouteDistance(minRoutes.get(0));  //取出目前认为最短的第一个轨迹计算距离
                long distance = getRouteDistance(route);    //计算出当前轨迹距离
                if (distance == minDistance) {
                    minRoutes.add(route);  //注意可能有多条
                }
                else if (distance < minDistance) {
                    minRoutes.clear();
                    minRoutes.add(route);
                }
                return false;
            }
            else {
                //这里给出理论: 起点和终点不同时如果一个点在轨迹中出现两次那么它一定不是最短路径.
                return !route.findDeadLoop();  //如果出现两次则无须继续递归查找
            }
        });
        checkState(!minRoutes.isEmpty(), "NO SUCH ROUTE " + first + " TO " + end);
        return minRoutes;
    }
}
