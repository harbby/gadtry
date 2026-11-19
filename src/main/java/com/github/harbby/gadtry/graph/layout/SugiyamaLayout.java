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
package com.github.harbby.gadtry.graph.layout;

import com.github.harbby.gadtry.graph.Graph;
import com.github.harbby.gadtry.graph.GraphEdge;
import com.github.harbby.gadtry.graph.GraphNode;
import com.github.harbby.gadtry.graph.export.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Pure-Java layered (Sugiyama style) layout with no external dependency:
 * <ol>
 * <li>every weakly connected component is laid out on its own, components are stacked across the flow;</li>
 * <li>cycles are broken by reversing DFS back edges;</li>
 * <li>longest-path layering, sources are pulled next to their closest successor;</li>
 * <li>long edges are split by virtual nodes;</li>
 * <li>crossings are reduced with alternating barycenter sweeps, keeping the best ordering found;</li>
 * <li>nodes are pulled towards the mean of their neighbours while keeping one step of separation.</li>
 * </ol>
 * Self loops are ignored. The result is deterministic for a given graph.
 */
public final class SugiyamaLayout
        implements GraphLayout
{
    private static final int MAX_SWEEPS = 24;
    private static final int MAX_SWEEPS_WITHOUT_IMPROVEMENT = 4;
    private static final int COORDINATE_PASSES = 8;

    @Override
    public <N, E> Map<N, Point> layout(Graph<N, E> graph, LayoutOptions options)
    {
        requireNonNull(graph, "graph is null");
        requireNonNull(options, "options is null");
        List<GraphNode<N, E>> nodes = new ArrayList<>(graph.nodes());
        int n = nodes.size();
        Map<N, Integer> index = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            index.put(nodes.get(i).getValue(), i);
        }
        int[][] successors = new int[n][];
        for (int i = 0; i < n; i++) {
            Collection<GraphEdge<N, E>> outEdges = nodes.get(i).outEdges();
            int[] targets = new int[outEdges.size()];
            int k = 0;
            for (GraphEdge<N, E> edge : outEdges) {
                targets[k++] = index.get(edge.getOutNode().getValue());
            }
            successors[i] = targets;
        }

        int[] rankOf = new int[n];
        double[] crossOf = new double[n];
        int[] localIndex = new int[n];
        double offset = 0;
        for (int[] component : components(successors)) {
            double width = layoutComponent(component, successors, localIndex, rankOf, crossOf, offset);
            offset += width + 1;
        }

        Map<N, Point> result = new LinkedHashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            double along = rankOf[i] * options.rankStep();
            double across = crossOf[i] * options.crossStep();
            Point point = options.getDirection() == Direction.LEFT_TO_RIGHT ? Point.of(along, across) : Point.of(across, along);
            result.put(nodes.get(i).getValue(), point);
        }
        return result;
    }

    /**
     * @return weakly connected components, ordered by their first node
     */
    private static List<int[]> components(int[][] successors)
    {
        int n = successors.length;
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        for (int u = 0; u < n; u++) {
            for (int v : successors[u]) {
                int a = find(parent, u);
                int b = find(parent, v);
                if (a != b) {
                    parent[Math.max(a, b)] = Math.min(a, b);
                }
            }
        }
        Map<Integer, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            groups.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        }
        List<int[]> components = new ArrayList<>(groups.size());
        for (List<Integer> group : groups.values()) {
            components.add(group.stream().mapToInt(Integer::intValue).toArray());
        }
        return components;
    }

    private static int find(int[] parent, int i)
    {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    /**
     * Lays out one component, writing ranks and cross coordinates (shifted by {@code offset}).
     *
     * @return the largest cross coordinate of the component relative to {@code offset}
     */
    private static double layoutComponent(int[] members, int[][] globalSuccessors, int[] localIndex,
            int[] rankOut, double[] crossOut, double offset)
    {
        int m = members.length;
        for (int i = 0; i < m; i++) {
            localIndex[members[i]] = i;
        }
        int[][] succ = new int[m][];
        int[] inDegree = new int[m];
        for (int i = 0; i < m; i++) {
            int[] targets = globalSuccessors[members[i]];
            int[] local = new int[targets.length];
            int k = 0;
            for (int target : targets) {
                int v = localIndex[target];
                if (v != i) {
                    local[k++] = v;
                    inDegree[v]++;
                }
            }
            succ[i] = Arrays.copyOf(local, k);
        }

        int[] dfsOrder = new int[m];
        int[][] dag = removeCycles(succ, inDegree, dfsOrder);
        int[] rank = assignRanks(dag);

        // layered graph: real nodes 0..m-1 followed by virtual nodes
        List<List<Integer>> up = new ArrayList<>();
        List<List<Integer>> down = new ArrayList<>();
        List<Integer> ranks = new ArrayList<>();
        int maxRank = 0;
        for (int i = 0; i < m; i++) {
            up.add(new ArrayList<>());
            down.add(new ArrayList<>());
            ranks.add(rank[i]);
            maxRank = Math.max(maxRank, rank[i]);
        }
        for (int u = 0; u < m; u++) {
            for (int v : dag[u]) {
                int previous = u;
                for (int r = rank[u] + 1; r < rank[v]; r++) {
                    int virtual = ranks.size();
                    up.add(new ArrayList<>());
                    down.add(new ArrayList<>());
                    ranks.add(r);
                    link(previous, virtual, up, down);
                    previous = virtual;
                }
                link(previous, v, up, down);
            }
        }

        List<List<Integer>> layerLists = new ArrayList<>();
        for (int r = 0; r <= maxRank; r++) {
            layerLists.add(new ArrayList<>());
        }
        for (int i = 0; i < m; i++) {
            layerLists.get(rank[dfsOrder[i]]).add(dfsOrder[i]);
        }
        for (int i = m; i < ranks.size(); i++) {
            layerLists.get(ranks.get(i)).add(i);
        }
        int[][] order = new int[layerLists.size()][];
        for (int r = 0; r < order.length; r++) {
            order[r] = layerLists.get(r).stream().mapToInt(Integer::intValue).toArray();
        }

        int[] pos = new int[ranks.size()];
        order = minimizeCrossings(order, up, down, pos);
        double[] x = assignCoordinates(order, up, down, ranks.size());

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double value : x) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        for (int i = 0; i < m; i++) {
            rankOut[members[i]] = rank[i];
            crossOut[members[i]] = x[i] - min + offset;
        }
        return max - min;
    }

    private static void link(int from, int to, List<List<Integer>> up, List<List<Integer>> down)
    {
        down.get(from).add(to);
        up.get(to).add(from);
    }

    /**
     * Iterative DFS starting from sources; back edges are reversed and duplicates dropped.
     *
     * @return successors of the resulting acyclic graph
     */
    private static int[][] removeCycles(int[][] succ, int[] inDegree, int[] dfsOrder)
    {
        int m = succ.length;
        int[] roots = new int[m];
        int k = 0;
        for (int i = 0; i < m; i++) {
            if (inDegree[i] == 0) {
                roots[k++] = i;
            }
        }
        for (int i = 0; i < m; i++) {
            if (inDegree[i] != 0) {
                roots[k++] = i;
            }
        }

        List<List<Integer>> dag = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            dag.add(new ArrayList<>());
        }
        Set<Long> seen = new HashSet<>();
        byte[] state = new byte[m];  // 0 = new, 1 = on stack, 2 = done
        int visited = 0;
        Deque<int[]> stack = new ArrayDeque<>();
        for (int root : roots) {
            if (state[root] != 0) {
                continue;
            }
            state[root] = 1;
            dfsOrder[visited++] = root;
            stack.push(new int[] {root, 0});
            while (!stack.isEmpty()) {
                int[] top = stack.peek();
                int u = top[0];
                if (top[1] < succ[u].length) {
                    int v = succ[u][top[1]++];
                    if (state[v] == 1) {
                        addEdge(dag, seen, v, u);
                    }
                    else {
                        addEdge(dag, seen, u, v);
                        if (state[v] == 0) {
                            state[v] = 1;
                            dfsOrder[visited++] = v;
                            stack.push(new int[] {v, 0});
                        }
                    }
                }
                else {
                    state[u] = 2;
                    stack.pop();
                }
            }
        }
        int[][] result = new int[m][];
        for (int i = 0; i < m; i++) {
            result[i] = dag.get(i).stream().mapToInt(Integer::intValue).toArray();
        }
        return result;
    }

    private static void addEdge(List<List<Integer>> dag, Set<Long> seen, int from, int to)
    {
        if (seen.add(((long) from << Integer.SIZE) | to)) {
            dag.get(from).add(to);
        }
    }

    /**
     * Longest-path layering; afterwards every source moves next to its closest successor.
     */
    private static int[] assignRanks(int[][] dag)
    {
        int m = dag.length;
        int[] inDegree = new int[m];
        for (int[] targets : dag) {
            for (int v : targets) {
                inDegree[v]++;
            }
        }
        int[] remaining = inDegree.clone();
        int[] rank = new int[m];
        Deque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < m; i++) {
            if (remaining[i] == 0) {
                queue.add(i);
            }
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : dag[u]) {
                rank[v] = Math.max(rank[v], rank[u] + 1);
                if (--remaining[v] == 0) {
                    queue.add(v);
                }
            }
        }
        for (int i = 0; i < m; i++) {
            if (inDegree[i] == 0 && dag[i].length > 0) {
                int closest = Integer.MAX_VALUE;
                for (int v : dag[i]) {
                    closest = Math.min(closest, rank[v]);
                }
                rank[i] = closest - 1;
            }
        }
        int minRank = Integer.MAX_VALUE;
        for (int r : rank) {
            minRank = Math.min(minRank, r);
        }
        for (int i = 0; i < m; i++) {
            rank[i] -= minRank;
        }
        return rank;
    }

    private static int[][] minimizeCrossings(int[][] order, List<List<Integer>> up, List<List<Integer>> down, int[] pos)
    {
        updatePositions(order, pos);
        long best = crossings(order, down, pos);
        int[][] bestOrder = copy(order);
        int withoutImprovement = 0;
        for (int sweep = 0; sweep < MAX_SWEEPS && best > 0; sweep++) {
            if (sweep % 2 == 0) {
                for (int r = 1; r < order.length; r++) {
                    sortByBarycenter(order[r], up, pos);
                }
            }
            else {
                for (int r = order.length - 2; r >= 0; r--) {
                    sortByBarycenter(order[r], down, pos);
                }
            }
            long current = crossings(order, down, pos);
            if (current < best) {
                best = current;
                bestOrder = copy(order);
                withoutImprovement = 0;
            }
            else if (++withoutImprovement >= MAX_SWEEPS_WITHOUT_IMPROVEMENT) {
                break;
            }
        }
        updatePositions(bestOrder, pos);
        return bestOrder;
    }

    private static void sortByBarycenter(int[] layer, List<List<Integer>> neighbors, int[] pos)
    {
        int k = layer.length;
        double[] barycenter = new double[k];
        Integer[] slots = new Integer[k];
        for (int i = 0; i < k; i++) {
            slots[i] = i;
            List<Integer> adjacent = neighbors.get(layer[i]);
            if (adjacent.isEmpty()) {
                barycenter[i] = pos[layer[i]];
            }
            else {
                double sum = 0;
                for (int v : adjacent) {
                    sum += pos[v];
                }
                barycenter[i] = sum / adjacent.size();
            }
        }
        // stable sort: ties keep the current order
        Arrays.sort(slots, Comparator.comparingDouble(i -> barycenter[i]));
        int[] sorted = new int[k];
        for (int i = 0; i < k; i++) {
            sorted[i] = layer[slots[i]];
        }
        for (int i = 0; i < k; i++) {
            layer[i] = sorted[i];
            pos[sorted[i]] = i;
        }
    }

    /**
     * Counts crossings between every pair of adjacent layers in O(E log V).
     */
    private static long crossings(int[][] order, List<List<Integer>> down, int[] pos)
    {
        long total = 0;
        for (int r = 0; r + 1 < order.length; r++) {
            List<Long> edges = new ArrayList<>();
            for (int u : order[r]) {
                for (int v : down.get(u)) {
                    edges.add(((long) pos[u] << Integer.SIZE) | pos[v]);
                }
            }
            Collections.sort(edges);
            int size = order[r + 1].length;
            int[] tree = new int[size + 1];
            int inserted = 0;
            for (long edge : edges) {
                int target = (int) edge;
                int notGreater = 0;
                for (int i = target + 1; i > 0; i -= i & -i) {
                    notGreater += tree[i];
                }
                total += inserted - notGreater;
                for (int i = target + 1; i <= size; i += i & -i) {
                    tree[i]++;
                }
                inserted++;
            }
        }
        return total;
    }

    private static double[] assignCoordinates(int[][] order, List<List<Integer>> up, List<List<Integer>> down, int total)
    {
        double[] x = new double[total];
        for (int[] layer : order) {
            for (int i = 0; i < layer.length; i++) {
                x[layer[i]] = i;
            }
        }
        for (int pass = 0; pass < COORDINATE_PASSES; pass++) {
            if (pass % 2 == 0) {
                for (int r = 1; r < order.length; r++) {
                    align(order[r], up, x);
                }
            }
            else {
                for (int r = order.length - 2; r >= 0; r--) {
                    align(order[r], down, x);
                }
            }
        }
        return x;
    }

    /**
     * Moves nodes towards the mean of their neighbours. Averaging a left-packed and a right-packed
     * placement keeps the layer order and at least one step between neighbours.
     */
    private static void align(int[] layer, List<List<Integer>> neighbors, double[] x)
    {
        int k = layer.length;
        if (k == 0) {
            return;
        }
        double[] desired = new double[k];
        for (int i = 0; i < k; i++) {
            List<Integer> adjacent = neighbors.get(layer[i]);
            if (adjacent.isEmpty()) {
                desired[i] = x[layer[i]];
            }
            else {
                double sum = 0;
                for (int v : adjacent) {
                    sum += x[v];
                }
                desired[i] = sum / adjacent.size();
            }
        }
        double[] left = new double[k];
        double[] right = new double[k];
        left[0] = desired[0];
        for (int i = 1; i < k; i++) {
            left[i] = Math.max(desired[i], left[i - 1] + 1);
        }
        right[k - 1] = desired[k - 1];
        for (int i = k - 2; i >= 0; i--) {
            right[i] = Math.min(desired[i], right[i + 1] - 1);
        }
        for (int i = 0; i < k; i++) {
            x[layer[i]] = (left[i] + right[i]) / 2;
        }
    }

    private static void updatePositions(int[][] order, int[] pos)
    {
        for (int[] layer : order) {
            for (int i = 0; i < layer.length; i++) {
                pos[layer[i]] = i;
            }
        }
    }

    private static int[][] copy(int[][] order)
    {
        int[][] copy = new int[order.length][];
        for (int i = 0; i < order.length; i++) {
            copy[i] = order[i].clone();
        }
        return copy;
    }
}
