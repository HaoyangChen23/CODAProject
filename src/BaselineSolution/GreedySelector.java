package BaselineSolution;

import main.Misc;
import model.*;
import utilities.Settings;

import java.util.*;

public class GreedySelector {
    private ArrayList<SimpleDFSCode> dfsCodes;
    private Graph TRANS;
    private ArrayList<SimpleDFSCode> selectedPatterns;
    private Set<Integer> coveredNodes;

    public int getCoveredNodesSize() {
        return coveredNodes.size();
    }

    public GreedySelector(ArrayList<SimpleDFSCode> dfsCodes, Graph TRANS) {
        this.dfsCodes = dfsCodes;
        this.TRANS = TRANS;
        this.selectedPatterns = new ArrayList<>();
        this.coveredNodes = new HashSet<>();
    }

    public ArrayList<SimpleDFSCode> selectTopKPatterns(ArrayList<SimpleDFSCode> candidates, int k) {
        System.out.println("Starting pattern selection with " + candidates.size() + " candidates");
        selectedPatterns.clear();
        

        k = Math.min(k, candidates.size());
        

        NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> root = new TreeMap<>();
        

        for (SimpleDFSCode dfsCode : candidates) {
            if (dfsCode == null || dfsCode.isEmpty()) {
                continue;
            }
            

            DFS firstDFS = dfsCode.getFirst();
            System.out.println("Processing pattern with first edge: " + 
                             firstDFS.fromLabel + "-[" + firstDFS.eLabel + "]->" + firstDFS.toLabel);


            int support = calculateSupport(dfsCode);
            System.out.println("Pattern support: " + support);


            if (support > 0) {
                selectedPatterns.add(dfsCode);
                if (selectedPatterns.size() >= k) {
                    break;
                }
            }

        }
        

        if (selectedPatterns.size() > k) {
            selectedPatterns = new ArrayList<>(selectedPatterns.subList(0, k));
        }
        
        System.out.println("Selected " + selectedPatterns.size() + " patterns");
        printSelectedPatterns();
        return selectedPatterns;
    }

    private int calculateSupport(SimpleDFSCode dfsCode) {
        int support = 0;

        for (int from = 0; from < TRANS.size(); ++from) {
            if (isPatternMatch(dfsCode, from)) {
                support++;
            }
        }
        return support;
    }

    private boolean isPatternMatch(SimpleDFSCode dfsCode, int startNode) {
        DFS firstDFS = dfsCode.getFirst();
        

        if (TRANS.get(startNode).label != firstDFS.fromLabel) {
            return false;
        }


        for (Edge e : TRANS.get(startNode).edge) {
            if (e.eLabel == firstDFS.eLabel && 
                TRANS.get(e.to).label == firstDFS.toLabel) {

                if (dfsCode.size() == 1) {
                    return true;
                }

                Set<Integer> visited = new HashSet<>();
                visited.add(startNode);
                visited.add(e.to);
                return matchRemainingEdges(dfsCode, 1, e.to, visited);
            }
        }
        return false;
    }

    private boolean matchRemainingEdges(SimpleDFSCode dfsCode, int currentEdgeIndex, 
                                      int currentNode, Set<Integer> visited) {
        if (currentEdgeIndex >= dfsCode.size()) {
            return true;
        }

        DFS currentDFS = dfsCode.get(currentEdgeIndex);
        

        for (Edge e : TRANS.get(currentNode).edge) {
            if (!visited.contains(e.to) && 
                e.eLabel == currentDFS.eLabel && 
                TRANS.get(e.to).label == currentDFS.toLabel) {
                
                visited.add(e.to);
                if (matchRemainingEdges(dfsCode, currentEdgeIndex + 1, e.to, visited)) {
                    return true;
                }
                visited.remove(e.to);
            }
        }
        return false;
    }

    public void printSelectedPatterns() {
        System.out.println("\n----------------------- Selected Patterns -----------------------");
        System.out.println("Top-" + Settings.k + " Results:");
        
        if (selectedPatterns.isEmpty()) {
            System.out.println("No patterns selected.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (int i = 0; i < Math.min(selectedPatterns.size(), Settings.k); i++) {
            SimpleDFSCode pattern = selectedPatterns.get(i);
            sb.append(patternToString(pattern));
            if (i < Math.min(selectedPatterns.size(), Settings.k) - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        
        System.out.println(sb.toString());
    }

    public String patternToString(SimpleDFSCode pattern) {
        StringBuilder sb = new StringBuilder();
        sb.append("Pattern(");
        
        for (int i = 0; i < pattern.size(); i++) {
            DFS edge = pattern.get(i);
            sb.append(edge.fromLabel)
              .append("-[")
              .append(edge.eLabel)
              .append("]-")
              .append(edge.toLabel);
            
            if (i < pattern.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append(")");
        return sb.toString();
    }

    public ArrayList<SimpleDFSCode> getSelectedPatterns() {
        return selectedPatterns;
    }

    public double calculateCoverage() {

        coveredNodes.clear();
        for (SimpleDFSCode pattern : selectedPatterns) {
            for (int node = 0; node < TRANS.size(); node++) {
                if (isPatternMatch(pattern, node)) {

                    coveredNodes.add(node);

                    addMatchedNodes(pattern, node);
                }
            }
        }
        

        double coverage = (double) coveredNodes.size() / TRANS.size();
        System.out.println("\nCoverage Statistics:");
        System.out.println("Total nodes in graph: " + TRANS.size());
        System.out.println("Covered nodes: " + coveredNodes.size());
        System.out.println("Coverage ratio: " + String.format("%.2f%%", coverage * 100));
        
        return coverage;
    }

    private void addMatchedNodes(SimpleDFSCode pattern, int startNode) {
        Set<Integer> visited = new HashSet<>();
        visited.add(startNode);
        DFS firstDFS = pattern.getFirst();
        

        for (Edge e : TRANS.get(startNode).edge) {
            if (e.eLabel == firstDFS.eLabel && 
                TRANS.get(e.to).label == firstDFS.toLabel) {
                coveredNodes.add(e.to);
                visited.add(e.to);

                if (pattern.size() > 1) {
                    addRemainingMatchedNodes(pattern, 1, e.to, visited);
                }
            }
        }
    }

    private void addRemainingMatchedNodes(SimpleDFSCode pattern, int edgeIndex, 
                                        int currentNode, Set<Integer> visited) {
        if (edgeIndex >= pattern.size()) {
            return;
        }

        DFS currentDFS = pattern.get(edgeIndex);
        for (Edge e : TRANS.get(currentNode).edge) {
            if (!visited.contains(e.to) && 
                e.eLabel == currentDFS.eLabel && 
                TRANS.get(e.to).label == currentDFS.toLabel) {
                
                coveredNodes.add(e.to);
                visited.add(e.to);
                addRemainingMatchedNodes(pattern, edgeIndex + 1, e.to, visited);
            }
        }
    }
}




