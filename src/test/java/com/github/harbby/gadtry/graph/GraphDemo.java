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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Checks.checkState;

/**
 * 某图计算题目
 */
public class GraphDemo
{
    private Graph<Data, EdgeData> graph;

    @Before
    public void before()
    {
        //AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7
        this.graph = Graph.<Data, EdgeData>builder()
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

    private static class EdgeData
            implements Data
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
        //1-5题目
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
        //题目6
        List<Route<Data, EdgeData>> routes = graph.searchRuleRoute("C", "C", route -> route.size() <= 3);
        Assert.assertEquals(2, routes.size());
        List<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toList());
        Assert.assertTrue(paths.contains("C-D-C") && paths.contains("C-E-B-C"));
    }

    @Test
    public void test7SearchEq4Return3AToC()
    {
        //题目7
        List<Route<Data, EdgeData>> routes = graph.searchRuleRoute("A", "C", route -> route.size() <= 4)
                .stream().filter(x -> x.size() == 4)
                .collect(Collectors.toList());

        Assert.assertEquals(3, routes.size());
        List<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toList());
        Assert.assertEquals(paths.toString(), "[A-B-C-D-C, A-D-C-D-C, A-D-E-B-C]");
    }

    @Test
    public void test8SearchMinRouteReturn9AToC()
    {
        //题目8 找出A到C的最短距离
        //Route route = graph.searchMinRoute("A", "C");
        List<Route<Data, EdgeData>> minRoutes = searchMinRoute(graph, "A", "C");
        long distances = getRouteDistance(minRoutes.get(0));

        Assert.assertEquals(9L, distances);
    }

    @Test
    public void test9SearchMinRouteReturn9BToB()
    {
        //题目9 找出B到B的最短距离
        List<Route<Data, EdgeData>> minRoutes = searchMinRoute(graph, "B", "B");
        long distances = getRouteDistance(minRoutes.get(0));
        Assert.assertEquals(9L, distances);
    }

    @Test
    public void test10SearchMaxDistances30RouteReturn7CToC()
    {
        //题目10 找出c to c 距离30以内的路程
        //Set<Route> routes = graph.searchMinRoute("C", "C", 30);
        List<Route<Data, EdgeData>> routes = graph.searchRuleRoute("C", "C", route -> {
            long distances = getRouteDistance(route);
            return distances < 30;
        });
        Assert.assertEquals(7, routes.size());
    }

    private static long getRouteDistance(Route<Data, EdgeData> route)
    {
        return route.getEdges().stream().mapToLong(x -> x.getData().getDistance()).sum();
    }

    private static List<Route<Data, EdgeData>> searchMinRoute(Graph<Data, EdgeData> graph, String first, String end)
    {
        List<Route<Data, EdgeData>> minRoutes = new ArrayList<>();
        List<Route<Data, EdgeData>> searchRoutes = graph.searchRuleRoute(first, route -> {
            if (end.equals(route.getEndNodeId())) {
                if (minRoutes.isEmpty()) {
                    minRoutes.add(route);
                    return false;
                }

                long min = getRouteDistance(minRoutes.get(0));
                long a = route.getEdges().stream().mapToLong(x -> x.getData().getDistance()).sum();
                if (a == min) {
                    minRoutes.add(route);  //注意可能有多条
                }
                else if (a < min) {
                    minRoutes.clear();
                    minRoutes.add(route);
                }
                return false;
            }
            else {
                //这里给出理论: 起点和终点不同时如果一个点在轨迹中出现两次那么它一定不是最短路径.
                return route.containsDeadRecursion();  //如果出现两次则无须继续递归查找
            }
        });
        checkState(!minRoutes.isEmpty(), "NO SUCH ROUTE " + first + " TO " + end);
        return minRoutes;
    }
}
