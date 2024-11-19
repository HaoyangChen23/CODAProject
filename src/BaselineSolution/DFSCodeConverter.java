package BaselineSolution;

import dataStructures.HPListGraph;
import dataStructures.IntIterator;
import java.util.*;
import model.*;
import search.SearchLatticeNode;

public class DFSCodeConverter {

  public static SimpleDFSCode convertToSimpleDFSCode(SearchLatticeNode<Integer, Integer> node) {
    SimpleDFSCode simpleDfsCode = new SimpleDFSCode();
    HPListGraph<Integer, Integer> graph = node.getHPlistGraph();

    Set<Integer> visited = new HashSet<>();

    Set<Integer> processedEdges = new HashSet<>();

    int startNode = 0;
    dfsTraversal(graph, startNode, visited, processedEdges, simpleDfsCode);

    return simpleDfsCode;
  }

  private static void dfsTraversal(
      HPListGraph<Integer, Integer> graph,
      int currentNode,
      Set<Integer> visited,
      Set<Integer> processedEdges,
      SimpleDFSCode simpleDfsCode) {
    visited.add(currentNode);
    System.out.println("Visiting node: " + currentNode);

    IntIterator edgeIt = graph.getEdgeIndices(currentNode);
    while (edgeIt.hasNext()) {
      int edgeIdx = edgeIt.next();
      System.out.println("\nProcessing EdgeID:" + edgeIdx);

      if (processedEdges.contains(edgeIdx)) {
        continue;
      }

      int nodeA = graph.getNodeA(edgeIdx);
      int nodeB = graph.getNodeB(edgeIdx);
      System.out.println("Edge connects nodes: " + nodeA + " -> " + nodeB);

      int neighborNode = (nodeA == currentNode) ? nodeB : nodeA;

      try {

        Object fromLabelObj = graph.getNodeLabel(currentNode);
        Object toLabelObj = graph.getNodeLabel(neighborNode);
        Object edgeLabelObj = graph.getEdgeLabel(edgeIdx);

        int fromLabel = convertToInt(fromLabelObj);
        int toLabel = convertToInt(toLabelObj);
        int edgeLabel = convertToInt(edgeLabelObj);

        if (fromLabel != -1 && toLabel != -1 && edgeLabel != -1) {
          DFS dfs = new DFS();
          dfs.from = currentNode;
          dfs.to = neighborNode;
          dfs.fromLabel = fromLabel;
          dfs.toLabel = toLabel;
          dfs.eLabel = edgeLabel;

          simpleDfsCode.add(dfs);
          processedEdges.add(edgeIdx);

          if (!visited.contains(neighborNode)) {
            dfsTraversal(graph, neighborNode, visited, processedEdges, simpleDfsCode);
          }
        }
      } catch (Exception e) {
        System.out.println("Error processing labels for edge " + edgeIdx + ": " + e.getMessage());
      }
    }
  }

  private static int convertToInt(Object label) {
    if (label == null) {
      return -1;
    }
    if (label instanceof Integer) {
      return (Integer) label;
    }
    if (label instanceof String) {
      try {
        return Integer.parseInt((String) label);
      } catch (NumberFormatException e) {
        return Math.abs(label.toString().hashCode());
      }
    }
    return Math.abs(label.hashCode());
  }

  public static ArrayList<SimpleDFSCode> convertResultList(
      ArrayList<SearchLatticeNode<Integer, Integer>> results) {
    ArrayList<SimpleDFSCode> simpleDfsCodes = new ArrayList<>();
    System.out.println("Converting " + results.size() + " search results");
    for (SearchLatticeNode<Integer, Integer> node : results) {
      SimpleDFSCode simpleDfsCode = convertToSimpleDFSCode(node);
      if (simpleDfsCode != null) {
        if (!simpleDfsCode.isEmpty()) {
          simpleDfsCodes.add(simpleDfsCode);
          System.out.println(
              "Successfully converted DFS code with " + simpleDfsCode.size() + " edges");
        } else {
          System.out.println("Generated empty DFS code");
        }
      } else {
        System.out.println("Failed to convert node to DFS code");
      }
    }

    System.out.println("Generated " + simpleDfsCodes.size() + " valid DFS codes");
    return simpleDfsCodes;
  }
}
