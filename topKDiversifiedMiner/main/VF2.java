package main;


import model.*;
import java.util.*;

public class VF2 {
    private final Graph pattern;
    private final Graph target;
    private final Map<Integer, Integer> patternInDegree;
    private final Map<Integer, Integer> patternOutDegree;
    private final Map<Integer, Integer> graphInDegree;
    private final Map<Integer, Integer> graphOutDegree;

    public VF2(Graph pattern, Graph target) {
        this.pattern = pattern;
        this.target = target;
        this.patternInDegree = new HashMap<>();
        this.patternOutDegree = new HashMap<>();
        this.graphInDegree = new HashMap<>();
        this.graphOutDegree = new HashMap<>();
        computeDegrees();
    }

    public List<Map<Integer, Integer>> findMatches() {
        List<Map<Integer, Integer>> allMatches = new ArrayList<>();
        Map<Integer, Integer> current = new HashMap<>();
        match(current, allMatches);
        return allMatches;
    }

    private void computeDegrees() {
        computeGraphDegrees(pattern, patternInDegree, patternOutDegree);
        computeGraphDegrees(target, graphInDegree, graphOutDegree);
    }

    private void computeGraphDegrees(Graph g,
                                     Map<Integer, Integer> inDegree,
                                     Map<Integer, Integer> outDegree) {
        // 初始化度数
        for (int i = 0; i < g.size(); i++) {
            inDegree.put(i, 0);
            outDegree.put(i, 0);
        }

        // 计算每个顶点的入度和出度
        for (int i = 0; i < g.size(); i++) {
            Vertex v = g.get(i);
            for (Edge e : v.edge) {
                outDegree.put(i, outDegree.get(i) + 1);
                inDegree.put(e.to, inDegree.get(e.to) + 1);
            }
        }
    }

    private void match(Map<Integer, Integer> current,
                       List<Map<Integer, Integer>> allMatches) {
        if (current.size() == pattern.size()) {
            allMatches.add(new HashMap<>(current));
            return;
        }

        int nextPatternVertex = selectNextVertex(current);

        for (int i = 0; i < target.size(); i++) {
            if (isMatchable(nextPatternVertex, i, current)) {
                current.put(nextPatternVertex, i);
                match(current, allMatches);
                current.remove(nextPatternVertex);
            }
        }
    }

    private int selectNextVertex(Map<Integer, Integer> current) {
        Set<Integer> matched = current.keySet();
        Set<Integer> candidates = new HashSet<>();

        // 收集所有与已匹配顶点相邻的未匹配顶点
        for (int v : matched) {
            for (Edge e : pattern.get(v).edge) {
                if (!matched.contains(e.to)) {
                    candidates.add(e.to);
                }
            }
        }

        if (!candidates.isEmpty()) {
            return candidates.iterator().next();
        }

        for (int i = 0; i < pattern.size(); i++) {
            if (!matched.contains(i)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isMatchable(int patternVertex, int graphVertex,
                                Map<Integer, Integer> current) {
        // 1. 检查标签匹配
        if (pattern.get(patternVertex).label != target.get(graphVertex).label) {
            return false;
        }

        // 2. 检查是否已被匹配
        if (current.containsValue(graphVertex)) {
            return false;
        }

        // 3. 检查度数约束
        if (patternInDegree.get(patternVertex) > graphInDegree.get(graphVertex) ||
                patternOutDegree.get(patternVertex) > graphOutDegree.get(graphVertex)) {
            return false;
        }

        // 4. 检查邻接关系
        for (Map.Entry<Integer, Integer> entry : current.entrySet()) {
            int p = entry.getKey();
            int g = entry.getValue();

            // 检查出边
            boolean hasPatternEdge = hasEdge(pattern, p, patternVertex);
            boolean hasGraphEdge = hasEdge(target, g, graphVertex);
            if (hasPatternEdge != hasGraphEdge) {
                return false;
            }

            // 检查入边
            hasPatternEdge = hasEdge(pattern, patternVertex, p);
            hasGraphEdge = hasEdge(target, graphVertex, g);
            if (hasPatternEdge != hasGraphEdge) {
                return false;
            }
        }

        return true;
    }

    private boolean hasEdge(Graph g, int from, int to) {
        for (Edge e : g.get(from).edge) {
            if (e.to == to) {
                return true;
            }
        }
        return false;
    }
}
