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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

/**
 * 某图计算题目
 */
public class GraphDemoTest
{
    private Graph<String, Integer> graph;

    @BeforeEach
    public void before()
    {
        //AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7
        this.graph = ImmutableGraph.<String, Integer>builder()
                .addNode("A")
                .addNode("B")
                .addNode("C")
                .addNode("D")
                .addNode("E")
                .addEdge("A", "B", 5)  //A到B距离为5
                .addEdge("B", "C", 4)
                .addEdge("C", "D", 8)
                .addEdge("D", "C", 8)
                .addEdge("D", "E", 6)
                .addEdge("A", "D", 5)
                .addEdge("C", "E", 2)
                .addEdge("E", "B", 3)
                .addEdge("A", "E", 7)
                .create();

        //---打印出已A为起点的网络
        graph.printShow("A").forEach(System.out::println);
    }

    @Test
    public void test1_5()
            throws Exception
    {
        //1-5
        Assertions.assertEquals(9L, getRouteDistance(graph.getRoute("A", "B", "C")));
        Assertions.assertEquals(5L, getRouteDistance(graph.getRoute("A", "D")));
        Assertions.assertEquals(13L, getRouteDistance(graph.getRoute("A", "D", "C")));
        Assertions.assertEquals(22L, getRouteDistance(graph.getRoute("A", "E", "B", "C", "D")));
        try {
            graph.getRoute("A", "E", "D");  //5
        }
        catch (Exception e) {
            Assertions.assertEquals("NO SUCH ROUTE", e.getMessage());
        }
    }

    @Test
    public void test6SearchMax3Return2CToC()
    {
        //6  找出C到C经过最多3个点的路线
        List<Route<String, Integer>> routes = graph.searchRuleRoute("C", "C", route -> route.size() <= 3);
        Assertions.assertEquals(2, routes.size());
        List<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toList());
        Assertions.assertTrue(paths.contains("C-D-C") && paths.contains("C-E-B-C"));
    }

    @Test
    public void test7SearchEq4Return3AToC()
    {
        //7 找出A到C恰好经过4个点的路线
        List<Route<String, Integer>> routes = graph.searchRuleRoute("A", "C", route -> route.size() <= 4)
                .stream().filter(x -> x.size() == 4)
                .collect(Collectors.toList());

        Assertions.assertEquals(3, routes.size());
        Set<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toSet());
        Assertions.assertEquals(paths, MutableSet.of("A-B-C-D-C", "A-D-C-D-C", "A-D-E-B-C"));
    }

    @Test
    public void searchApi7SearchEq4Return3AToC()
    {
        //7 找出A到C恰好经过4个点的路线
        List<Route<String, Integer>> routes = graph.search()
                .beginNode("A")
                .endNode("C")
                .mode(SearchBuilder.Mode.BREADTH_FIRST)
                .nextRule(route -> route.size() <= 4)
                .globalRule(searchContext -> searchContext.getSearchTime() < 5_000)
                .search()
                .getRoutes()
                .stream().filter(x -> x.size() == 4).collect(Collectors.toList());

        Set<String> paths = routes.stream().map(x -> String.join("-", x.getIds())).collect(Collectors.toSet());
        Assertions.assertEquals(paths, MutableSet.of("A-B-C-D-C", "A-D-C-D-C", "A-D-E-B-C"));
    }

    @Test
    public void test8SearchMinRouteReturn9AToC()
    {
        //8 找出A到C的最短距离线路
        List<Route<String, Integer>> minRoutes = searchMinRoute(graph, "A", "C");
        long distances = getRouteDistance(minRoutes.get(0));

        Assertions.assertEquals(9L, distances);
    }

    @Test
    public void test9SearchMinRouteReturn9BToB()
    {
        //9 找出B到B的最短距离线路
        List<Route<String, Integer>> minRoutes = searchMinRoute(graph, "B", "B");
        long distances = getRouteDistance(minRoutes.get(0));
        Assertions.assertEquals(9L, distances);
    }

    @Test
    public void test10SearchMaxDistances30RouteReturn7CToC()
    {
        //10 找出c to c 距离30以内的线路
        List<Route<String, Integer>> routes = graph.searchRuleRoute("C", "C", route -> {
            long distances = getRouteDistance(route);
            return distances < 30;
        });
        Assertions.assertEquals(7, routes.size());
    }

    @Test
    public void searchApi10SearchGiveMaxDistances30Return7ByCToC()
    {
        //10 找出c to c 距离30以内的线路
        List<Route<String, Integer>> routes = graph.search()
                .beginNode("C")
                .endNode("C")
                .mode(SearchBuilder.Mode.DEPTH_FIRST)
                .nextRule(route -> {
                    long distances = getRouteDistance(route);
                    return distances < 30;
                })
                .globalRule(searchContext -> searchContext.getSearchTime() < 5_000)
                .search()
                .getRoutes();
        Assertions.assertEquals(7, routes.size());
    }

    private static long getRouteDistance(Route<String, Integer> route)
    {
        return route.getEdges().stream().mapToLong(GraphEdge::getValue).sum();
    }

    private static List<Route<String, Integer>> searchMinRoute(Graph<String, Integer> graph, String first, String end)
    {
        List<Route<String, Integer>> minRoutes = new ArrayList<>();
        List<Route<String, Integer>> searchRoutes = graph.searchRuleRoute(first, route -> {
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
                return !route.containsLoop();  //如果出现两次则无须继续递归查找
            }
        });
        checkState(!minRoutes.isEmpty(), "NO SUCH ROUTE " + first + " TO " + end);
        return minRoutes;
    }
}
