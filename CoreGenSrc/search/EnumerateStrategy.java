/**
 * created May 16, 2006
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *     <p>Copyright 2006 Marc Woerlein
 *     <p>This file is part of parsemis.
 *     <p>Licence: LGPL: http://www.gnu.org/licenses/lgpl.html EPL:
 *     http://www.eclipse.org/org/documents/epl-v10.php See the LICENSE file in the project's
 *     top-level directory for details.
 */
package search;

import AlgorithmInterface.Algorithm;
import dataStructures.Canonizable;
import dataStructures.DFSCode;
import dataStructures.Frequency;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.IntFrequency;
import dataStructures.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import joinAlgorithm.Diversity;
import joinAlgorithm.JoinMNI;
import joinAlgorithm.UpperBoundOfMNI;
import topKresults.MaxHeap;
import utilities.Settings;

/**
 * @param <NodeType> the type of the node labels (will be hashed and checked with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked with .equals(..))
 */
public class EnumerateStrategy<NodeType, EdgeType> implements Strategy<NodeType, EdgeType> {
  private Extender<NodeType, EdgeType> extender;
  private Graph kGraph; // KG
  private static int[][] maxDegreeMatrix; // for degree checking
  private HashMap<String, Integer> patternEdgeMap; // meta index

  public EnumerateStrategy(Graph kGraph, HashMap<String, Integer> patternEdgeMap) {
    this.kGraph = kGraph;
    this.patternEdgeMap = patternEdgeMap;
  }

  public ArrayList<SearchLatticeNode<NodeType, EdgeType>> search(
      final Algorithm<NodeType, EdgeType> extend,
      DFSCode<NodeType, EdgeType> corePattern,
      HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> initialsMap) {
    ArrayList<Integer> sortedNodeLabels = kGraph.getSortedFreqLabels();
    maxDegreeMatrix = kGraph.getMaxDegreeMatrix();
    //		HashSet<Integer> coreCover = corePattern.getCoverage();

    // optimization: meta index for pattern extension
    if (Settings.HMT) {
      extender = extend.PatExtMeta(patternEdgeMap, maxDegreeMatrix, sortedNodeLabels, kGraph);
    } else if (!Settings.HMT) {
      // use rightmost path extension
      extender = extend.getExtender(0);
    }

    // Initialize max-heap to store candidate pattern set CS;
    MaxHeap<NodeType, EdgeType> HHeap = new MaxHeap<NodeType, EdgeType>();
    // Initialize top-k pattern result set R
    ArrayList<SearchLatticeNode<NodeType, EdgeType>> resultSet = new ArrayList<>();
    SearchLatticeNode<NodeType, EdgeType> mostFreqPat = null;
    if (corePattern.getPatternSize() != 1) {
      mostFreqPat = corePattern; // P*
    } else { // the core pattern only has one node
      for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> initialEntry :
          initialsMap.entrySet()) {
        SearchLatticeNode<NodeType, EdgeType> initialEdge = initialEntry.getKey();
        initialEdge.setCoverVertices(((DFSCode<NodeType, EdgeType>) initialEdge).getCoverage());
        Frequency freq = initialEntry.getValue();
        HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> map =
            new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
        map.put(initialEdge, freq);
        HHeap.insert(map);
      }
      HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> hMap = HHeap.popHeap(0);
      for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> hEntry :
          hMap.entrySet()) { // only one item
        SearchLatticeNode<NodeType, EdgeType> pattern = hEntry.getKey();
        mostFreqPat = pattern;
        resultSet.add(mostFreqPat);
      }
    }

    int freqCount = 0;
    int divCount = 0;
    //
    while (resultSet.size() < 3 * Settings.k) {
      // extend via meta-index
      Collection<SearchLatticeNode<NodeType, EdgeType>> children =
          extender.getChildren(mostFreqPat);
      for (SearchLatticeNode<NodeType, EdgeType> child : children) {
        final Canonizable can = (Canonizable) child;
        if (!can.isCanonical()) { // DFS code minimum
          System.out.println("Not Canonizable!!!");
          continue;
        }
        // compute MNI upper bound
        Query qry = new Query((HPListGraph<Integer, Double>) child.getHPlistGraph());
        UpperBoundOfMNI MNIupper = new UpperBoundOfMNI(qry, (DFSCode<NodeType, EdgeType>) child);
        IntFrequency MNIub = MNIupper.getUpperBound(0);
        HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> childMap =
            new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
        childMap.put(child, MNIub);
        HHeap.insert(childMap);
      }

      // find the next most frequent pattern
      // maxHeap: The next most frequent pattern can be found among the descendants of P and the
      // remained  patterns
      // Initialize max-heap exact MNI pattern set
      MaxHeap<NodeType, EdgeType> EHeap = new MaxHeap<NodeType, EdgeType>();
      OUT:
      while (HHeap.getSize() != 0) {
        HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> map =
            new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
        HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> hMap =
            HHeap.popHeap(0); // upper bound or exact MNI
        for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> hEntry :
            hMap.entrySet()) { // only one item
          SearchLatticeNode<NodeType, EdgeType> pattern = hEntry.getKey();
          if (pattern.getMNIfrequency() != null) {
            System.out.println("has MNI before !!");
            map.put(pattern, pattern.getMNIfrequency());
            EHeap.insert(map);
            break OUT;
          }
          if (EHeap.getSize() != 0) {
            HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> eMap = EHeap.mostFrequent(0);
            for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> eEntry :
                eMap.entrySet()) { // only one item
              if (hEntry.getValue().compareTo(eEntry.getValue()) > 0) {
                // join MNI
                System.out.println("count MNI of :\n" + pattern);
                JoinMNI kCPAA = new JoinMNI((DFSCode<NodeType, EdgeType>) pattern);
                kCPAA.joinMNIPlus(eEntry.getValue());
                freqCount++;
                map.put(pattern, pattern.getMNIfrequency());
                EHeap.insert(map);
              } else {
                map.put(pattern, hEntry.getValue());
                HHeap.insert(map);
                break OUT;
              }
            }
          } else if (EHeap.getSize() == 0) {
            System.out.println("count MNI of :\n" + pattern);
            JoinMNI kCPAA = new JoinMNI((DFSCode<NodeType, EdgeType>) pattern);
            kCPAA.joinMNIPlus(new IntFrequency(0));
            freqCount++;
            map.put(pattern, pattern.getMNIfrequency());
            EHeap.insert(map);
          }
        }
      }
      // the next most frequent pattern
      HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> mostMap = EHeap.popHeap(0);
      for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry :
          mostMap.entrySet()) { // only one item
        mostFreqPat = entry.getKey();
      }

      // insert the patterns of EHeap into HHeap
      List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> lists = EHeap.getHeap();
      for (int i = 0; i < lists.size(); i++) {
        HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> hMap = lists.get(i);
        HHeap.insert(hMap);
      }

      // diversity checking
      Query qry = new Query((HPListGraph<Integer, Double>) mostFreqPat.getHPlistGraph());
      Diversity div = new Diversity(qry);
      System.out.println("Checking diversity ...\n" + mostFreqPat);
      System.out.println("MNI:" + mostFreqPat.getMNIfrequency());
      @SuppressWarnings("unchecked")
      boolean add = div.diversityCheck(mostFreqPat, resultSet);
      divCount++;
      if (add) {
        if (mostFreqPat.getCoverVertices() == null) {
          // evaluate the covered vertices set of P in KG
          div.coverVerticeSetFirst(mostFreqPat);
        }
        resultSet.add(mostFreqPat);
      }
      System.out.println("already has top-:" + resultSet.size());
    }
    System.out.println("Top-" + Settings.k + " List:\n" + resultSet);
    System.out.println("maxheap size:" + HHeap.getSize());
    System.out.println("freqCount: " + freqCount);
    System.out.println("divCount: " + divCount);
    return resultSet;
  }
}
