/**  
 * Project Name:kFPD_opt  
 * File Name:DivEvaluation.java  
 * Package Name:joinAlgorithm  
 * Date:Jul 1, 2021  
 * Copyright (c) 2021, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package joinAlgorithm;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.DFSCode;
import dataStructures.HPListGraph;
import dataStructures.IntIterator;
import dataStructures.Query;
import search.SearchLatticeNode;
import utilities.Settings;

/**  
 * ClassName:DivEvaluation   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Jul 1, 2021   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class Diversity<NodeType, EdgeType> {
	
	private int qrySize;
	private HPListGraph<Integer, Double> qryGraph;
	private static HashMap<Integer, Integer> nodeLabelMap;// <nodeIdx, nodeLabel>
	private static HashMap<Integer, HashSet<Integer>> coverVerticesMap; //
	private Query qry;
	private static final int DEF_DIV_SCALE = 3;

	
	public Diversity(Query qry) {
		this.qry = qry;// candidate pattern P: graph format
		this.qryGraph = qry.getListGraph();
		this.qrySize = qryGraph.getNodeCount();
		this.nodeLabelMap = qry.getNodeLabelMap();
		this.coverVerticesMap = new HashMap<Integer, HashSet<Integer>>();
	}

	/*
	 * @param: P, R, tau
	 * 
	 */
	public boolean diversityCheck(SearchLatticeNode<NodeType, EdgeType> pat,
			ArrayList<SearchLatticeNode<NodeType, EdgeType>> resultSet) {
		if (pat.getCoverVertices() == null) {
			HashMap<Integer, HashSet<Integer>> domainMap = pat.getDomMap();
			// marked vertices set by join:O(P)
			HashSet<Integer> markedSet = new HashSet<Integer>();
			for (Entry<Integer, HashSet<Integer>> entry : domainMap.entrySet()) {
				HashSet<Integer> nodeSet = entry.getValue();
				markedSet.addAll(nodeSet);
			}
			// node domains from MNI upper bound
			HashSet<Integer> upperDomainSet = pat.getUpperVertices();

			// for each pattern in result set, check diversity
			for (int i = 0; i < resultSet.size(); i++) {
				SearchLatticeNode<NodeType, EdgeType> rPattern = resultSet.get(i);
				System.out.println("pattern in R set: \n" + rPattern);
				// get the covered vertices set of P' in KG
				HashSet<Integer> fullCoverSet = rPattern.getCoverVertices();
				if (divLowerBound(fullCoverSet, markedSet, upperDomainSet) < Settings.t) {
					// evaluate the covered vertices set of P in KG
					boolean ifDiv = coverVerticeSet(pat, fullCoverSet, upperDomainSet);
					if (!ifDiv) {
						return false;
					}
				}
			}
		} else if (pat.getCoverVertices() != null) {
			// for each pattern in result set, check diversity
			HashSet<Integer> patCoverSet = pat.getCoverVertices();
			for (int i = 0; i < resultSet.size(); i++) {
				SearchLatticeNode<NodeType, EdgeType> rPattern = resultSet.get(i);
				System.out.println("pattern in R set: \n" + rPattern);
				// get the covered vertices set of P' in KG
				HashSet<Integer> fullCoverSet = rPattern.getCoverVertices();
				if (exactDiv(patCoverSet, fullCoverSet) < Settings.t) {
					return false;
				}
			}
		}

		return true;
	}

	/*
	 * diversity lower bound evaluation
	 * 
	 */
	public double divLowerBound(HashSet<Integer> fullCoverSet, HashSet<Integer> markedJoinSet,
			HashSet<Integer> upperDomainSet) {
		// intersection
		HashSet<Integer> interSet = new HashSet<>();
		interSet.addAll(fullCoverSet);
		interSet.retainAll(upperDomainSet);
		// union set
		HashSet<Integer> unionSet = new HashSet<>();
		unionSet.addAll(fullCoverSet);
		unionSet.addAll(markedJoinSet);
//		System.out.println("lower bound:"+divFunction(interSet.size(), unionSet.size()));
		return divFunction(interSet.size(), unionSet.size());
	}
	
	/*
	 * diversity lower bound evaluation
	 * 
	 */
	public double divUpperBound(HashSet<Integer> fullCoverSet, HashSet<Integer> markedJoinSet,
			HashSet<Integer> upperDomainSet) {
		// intersection
		HashSet<Integer> interSet = new HashSet<>();
		interSet.addAll(fullCoverSet);
		interSet.retainAll(upperDomainSet);
		// union set
		HashSet<Integer> unionSet = new HashSet<>();
		unionSet.addAll(fullCoverSet);
		unionSet.addAll(markedJoinSet);
		System.out.println("lower bound:"+divFunction(interSet.size(), unionSet.size()));
		return divFunction(interSet.size(), unionSet.size());
	}
	
	/*
	 * exact diversity evaluation
	 */
	public double exactDiv(HashSet<Integer> patSet, HashSet<Integer> otherSet) {
		// intersection
		HashSet<Integer> interSet = new HashSet<>();
		interSet.addAll(patSet);
		interSet.retainAll(otherSet);
		// union set
		HashSet<Integer> unionSet = new HashSet<>();
		unionSet.addAll(patSet);
		unionSet.addAll(otherSet);
		System.out.println("exact diversity:"+divFunction(interSet.size(), unionSet.size()));
		return divFunction(interSet.size(), unionSet.size());
	}
	
	public double divFunction(int a, int b) {
		BigDecimal in = new BigDecimal(a);
        BigDecimal un = new BigDecimal(b);
        BigDecimal r = in.divide(un, DEF_DIV_SCALE, BigDecimal.ROUND_HALF_UP);
        BigDecimal tmp = new BigDecimal(1);
        BigDecimal result = tmp.subtract(r);
		return result.doubleValue();
	}
	
	
	/*
	 * covered vertices set evaluation
	 */
	public boolean coverVerticeSet(SearchLatticeNode<NodeType, EdgeType> pattern, HashSet<Integer> fullCoverSet,
			HashSet<Integer> upperDomainSet) {
		HashSet<Integer> markedSet = new HashSet<Integer>();
		HashMap<Integer, HashSet<Integer>> domainMap = pattern.getDomMap();
		coverVerticesMap = (HashMap<Integer, HashSet<Integer>>) domainMap.clone();
		HashMap<Integer, PatternEdges> fullEdgeMap = pattern.getFullEdgeMap();// full edge tables
		// edge info after join operation
		HashMap<Integer, PatternEdges> joinEdgeMap = pattern.getJoinEdgeMap();
		for (Entry<Integer, PatternEdges> entry : joinEdgeMap.entrySet()) {
			int edgeIdex = entry.getKey();
			PatternEdges pe = entry.getValue();
			// other untraversed tuples after join
			InsTable curInsTables = pe.getRemainTuples(qrySize);

			// for each tuple
			for (int i = 0; i < curInsTables.getSize(); i++) {
				InsTable curTuple = new InsTable();
				curTuple.addItem(curInsTables.get(i));
				HashMap<Integer, PatternEdges> midMap = (HashMap<Integer, PatternEdges>) fullEdgeMap.clone();
				midMap.remove(edgeIdex);

//				InsTable RTable = new InsTable();// result table after join
				// join with other full edge tables
				while (midMap != null && midMap.size() != 0) {
					// adjacent table of RTable
					PatternEdges adjEdge = getAdjacentTable(curTuple, midMap);
					if (adjEdge == null)
						break;
					midMap.remove(adjEdge.getEdgeIdx());
					// full join process
					curTuple = oneJoinTables(curTuple, adjEdge);
				}
				// mark all values in R to the corresponding covered vertices
				if (curTuple.getSize() != 0) {
					markNodesDiv(curTuple, pattern);
				}
			}
			// count the cover vertices
			for (Entry<Integer, HashSet<Integer>> e : coverVerticesMap.entrySet()) {
				HashSet<Integer> set = e.getValue();
				markedSet.addAll(set);
			}
			// lower bound checking
			if (divLowerBound(fullCoverSet, markedSet, upperDomainSet) >= Settings.t)
				return true;
		}
		// count the cover vertices
		for (Entry<Integer, HashSet<Integer>> e : coverVerticesMap.entrySet()) {
			HashSet<Integer> set = e.getValue();
			markedSet.addAll(set);
		}
		// store in the pattern
		pattern.setCoverVertices(markedSet);

		// diversity checking
		if (exactDiv(markedSet, fullCoverSet) < Settings.t) {
			return false;
		} else
			return true;
	}
	
	/*
	 *  covered vertices set evaluation
	 */
	public HashSet<Integer> coverVerticeSetFirst(SearchLatticeNode<NodeType, EdgeType> pattern) {
		HashSet<Integer> coverSet = new HashSet<Integer>();
		HashMap<Integer, HashSet<Integer>> domainMap = pattern.getDomMap();
		coverVerticesMap = (HashMap<Integer, HashSet<Integer>>) domainMap.clone();
		HashMap<Integer, PatternEdges> fullEdgeMap = pattern.getFullEdgeMap();// full edge tables
		// edge info after join operation
		HashMap<Integer, PatternEdges> joinEdgeMap = pattern.getJoinEdgeMap();
		for (Entry<Integer, PatternEdges> entry : joinEdgeMap.entrySet()) {
			int edgeIdex = entry.getKey();
			PatternEdges pe = entry.getValue();
			// other untraversed tuples after join
			InsTable RTable = pe.getRemainTuples(qrySize);
			HashMap<Integer, PatternEdges> midMap = (HashMap<Integer, PatternEdges>) fullEdgeMap.clone();
			midMap.remove(edgeIdex);
			while (midMap != null && midMap.size() != 0) {
				// adjacent table of RTable
				PatternEdges adjEdge = getAdjacentTable(RTable, midMap);
				if (adjEdge == null || RTable.getSize() == 0)
					break;
				midMap.remove(adjEdge.getEdgeIdx());
				// join with other full edge tables
				RTable = fullJoinTables(RTable, adjEdge);
//				System.out.println("after join RTable size:" + RTable.getSize());
			}
			// mark all values in R to the corresponding covered vertices
			if (RTable.getSize() != 0) {
				markNodesDiv(RTable, pattern);
			}
		}
		// count the cover vertices
		for (Entry<Integer, HashSet<Integer>> entry : coverVerticesMap.entrySet()) {
			HashSet<Integer> set = entry.getValue();
			coverSet.addAll(set);
		}
		// store in the pattern
		pattern.setCoverVertices(coverSet);
		return coverSet;
	}
	
	/*
	 * mark the valid instances in corresponding domains
	 * 
	 * @param: validTable: contains the valid join results(R)
	 */
	public void markNodesDiv(InsTable validTable, SearchLatticeNode<NodeType, EdgeType> pattern) {
		ArrayList<Instances> instanceList = validTable.getInsList();
		Iterator it = instanceList.iterator();
		while (it.hasNext()) {
			Instances ins = (Instances) it.next();
			for (int i = 0; i < qrySize; i++) {
				if (coverVerticesMap.get(i) == null) {
					HashSet<Integer> dom = new HashSet<Integer>();
					dom.add(ins.getNode(i));
					coverVerticesMap.put(i, dom);
				} else
					coverVerticesMap.get(i).add(ins.getNode(i));
			}
		}
		// need set marked nodes to pattern edges
		HashMap<Integer, PatternEdges> pesMap = pattern.getQuery().getPEMap();
		for (Entry<Integer, PatternEdges> pesEntry : pesMap.entrySet()) {
			PatternEdges pe = pesEntry.getValue();
			int nodeA = pe.getIndexA();
			int nodeB = pe.getIndexB();
			HashSet<Integer> domA = coverVerticesMap.get(nodeA);
			HashSet<Integer> domB = coverVerticesMap.get(nodeB);
			pe.setMarkNodes(nodeA, domA);
			pe.setMarkNodes(nodeB, domB);
		}
	}

	
	/*
	 * get adjacent(neighbor) edge
	 */
	private PatternEdges getAdjacentTable(Instances curIns, HashMap<Integer, PatternEdges> edgesMap) {
		HashSet<Integer> pNodeSet = curIns.getPatNodes();
		int joinNode = -1;
		for (int pNode : pNodeSet) {
			IntIterator edges = qryGraph.getEdgeIndices(pNode);
			while (edges.hasNext()) {
				int edgeIdx = edges.next();
				if (edgesMap.containsKey(edgeIdx)) {
					joinNode = pNode;
					PatternEdges edgeTable = edgesMap.get(edgeIdx);
					edgeTable.setJoinNode(joinNode);
					return edgeTable;
				}
			}
		}
		return null;
	}
	
	/*
	 * get adjacent(neighbor) edge
	 */
	private PatternEdges getAdjacentTable(InsTable curInsTable, HashMap<Integer, PatternEdges> edgesMap) {
		if (curInsTable.getSize() == 0) {
			return null;
		}
		HashSet<Integer> pNodeSet = curInsTable.getPatNodes();
		int joinNode = -1;
		for (int pNode : pNodeSet) {
			IntIterator edges = qryGraph.getEdgeIndices(pNode);
			while (edges.hasNext()) {
				int edgeIdx = edges.next();
				if (edgesMap.containsKey(edgeIdx)) {
					joinNode = pNode;
					PatternEdges edgeTable = edgesMap.get(edgeIdx);
					edgeTable.setJoinNode(joinNode);
					return edgeTable;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private InsTable fullJoinTables(InsTable interTable, PatternEdges adjEdge) {
		ArrayList<int[]> adjNodePairs = adjEdge.getNodePairs();
		int joinNode = adjEdge.getJoinNode();
		int addIdx = -1;
		int pairColumnA = -1;
		int pairColumnB = -1;
		if (adjEdge.getIndexA() == joinNode) {
			addIdx = adjEdge.getIndexB();// the node index need add into
			pairColumnA = 0;
			pairColumnB = 1;
		} else if (adjEdge.getIndexB() == joinNode) {
			addIdx = adjEdge.getIndexA();
			pairColumnA = 1;
			pairColumnB = 0;
		}

		// check if adjEdge has same node label in interTable
		HashSet<Integer> pNodeSet = interTable.getPatNodes();
		int adjLabel = nodeLabelMap.get(addIdx);
		ArrayList<Integer> sameLabelList = new ArrayList<Integer>();
		boolean needCheckSame = false;
		for (int node : pNodeSet) {
			if (nodeLabelMap.get(node) == adjLabel) {// same node label in the pattern
				sameLabelList.add(node);
				needCheckSame = true;
			}
		}

		// sort the instances by join attribute in ascending order
		final int s = pairColumnA;
		Collections.sort(adjNodePairs, new ComparatorPair() {
			@Override
			public int compare(Object o1, Object o2) {
				int[] p1 = (int[]) o1;
				int[] p2 = (int[]) o2;
				return p1[s] - p2[s];
			}
		});
		interTable.sortIns(joinNode);

		InsTable joinedTable = new InsTable();// result table after join
		// check if can construct a cyclic
		if (pNodeSet.contains(adjEdge.getIndexA()) && pNodeSet.contains(adjEdge.getIndexB())) {
			// cyclic
			for (int i = 0; i < interTable.getSize(); i++) {
				Instances interIns = interTable.get(i);
				String curInter = interIns.getNode(adjEdge.getIndexA()) + "_" + interIns.getNode(adjEdge.getIndexB());
				Iterator adjIt = adjNodePairs.iterator();
				while (adjIt.hasNext()) {
					int[] adjArr = (int[]) adjIt.next();
					int adjJoinNode = adjArr[pairColumnA];
					if (interIns.getNode(joinNode) < adjJoinNode)
						break;
					if (interIns.getNode(joinNode) > adjJoinNode)
						continue;
					String adjIntr = adjArr[0] + "_" + adjArr[1];
					if (curInter.equals(adjIntr))
						joinedTable.getInsList().add(interIns);
				}
			}
		} else {// if the intermediate table and its adjacent edge can not construct a cyclic
			for (int i = 0; i < interTable.getSize(); i++) {
				Instances interIns = interTable.get(i);
				int interNode = interIns.getNode(joinNode);
				Iterator it = adjNodePairs.iterator();
				while (it.hasNext()) {
					int[] adjArr = (int[]) it.next();
					int adjJoinNode = adjArr[pairColumnA];
					if (interNode < adjJoinNode)
						break;
					if (interNode > adjJoinNode)
						continue;
					/*
					 * if (interIns.getNode(joinNode) == adjJoinNode &&
					 * !interIns.contains(adjArr[pairColumnB])) joinedTable.joinAssign(interIns,
					 * addIdx, adjArr[pairColumnB]);
					 */

					if (interNode == adjJoinNode) {
						if (needCheckSame) {// has same label
							if (!interIns.sameContains(adjArr[pairColumnB], sameLabelList)) {
								joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
							}
						} else
							joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
					}

				}
			}
		}
		return joinedTable;
	}
	
	
	@SuppressWarnings("unchecked")
	private InsTable oneJoinTables(InsTable interTable, PatternEdges adjEdge) {
		ArrayList<int[]> adjNodePairs = adjEdge.getNodePairs();
		int joinNode = adjEdge.getJoinNode();
		int addIdx = -1;
		int pairColumnA = -1;
		int pairColumnB = -1;
		if (adjEdge.getIndexA() == joinNode) {
			addIdx = adjEdge.getIndexB();// the node index need add into
			pairColumnA = 0;
			pairColumnB = 1;
		} else if (adjEdge.getIndexB() == joinNode) {
			addIdx = adjEdge.getIndexA();
			pairColumnA = 1;
			pairColumnB = 0;
		}

		// check if adjEdge has same node label in interTable
		HashSet<Integer> pNodeSet = interTable.getPatNodes();
		int adjLabel = nodeLabelMap.get(addIdx);
		ArrayList<Integer> sameLabelList = new ArrayList<Integer>();
		boolean needCheckSame = false;
		for (int node : pNodeSet) {
			if (nodeLabelMap.get(node) == adjLabel) {// same node label in the pattern
				sameLabelList.add(node);
				needCheckSame = true;
			}
		}

		// sort the instances by join attribute in ascending order
		final int s = pairColumnA;
		Collections.sort(adjNodePairs, new ComparatorPair() {
			@Override
			public int compare(Object o1, Object o2) {
				int[] p1 = (int[]) o1;
				int[] p2 = (int[]) o2;
				return p1[s] - p2[s];
			}
		});
		interTable.sortIns(joinNode);

		InsTable joinedTable = new InsTable();// result table after join
		// check if can construct a cyclic
		if (pNodeSet.contains(adjEdge.getIndexA()) && pNodeSet.contains(adjEdge.getIndexB())) {
			// cyclic
			for (int i = 0; i < interTable.getSize(); i++) {
				Instances interIns = interTable.get(i);
				String curInter = interIns.getNode(adjEdge.getIndexA()) + "_" + interIns.getNode(adjEdge.getIndexB());
				Iterator adjIt = adjNodePairs.iterator();
				while (adjIt.hasNext()) {
					int[] adjArr = (int[]) adjIt.next();
					int adjJoinNode = adjArr[pairColumnA];
					if (interIns.getNode(joinNode) < adjJoinNode)
						break;
					if (interIns.getNode(joinNode) > adjJoinNode)
						continue;
					String adjIntr = adjArr[0] + "_" + adjArr[1];
					if (curInter.equals(adjIntr)) {
						joinedTable.getInsList().add(interIns);
					}
					return joinedTable;
				}
			}
		} else {// if the intermediate table and its adjacent edge can not construct a cyclic
			for (int i = 0; i < interTable.getSize(); i++) {
				Instances interIns = interTable.get(i);
				int interNode = interIns.getNode(joinNode);
				Iterator it = adjNodePairs.iterator();
				while (it.hasNext()) {
					int[] adjArr = (int[]) it.next();
					int adjJoinNode = adjArr[pairColumnA];
					if (interNode < adjJoinNode)
						break;
					if (interNode > adjJoinNode)
						continue;
					/*
					 * if (interIns.getNode(joinNode) == adjJoinNode &&
					 * !interIns.contains(adjArr[pairColumnB])) joinedTable.joinAssign(interIns,
					 * addIdx, adjArr[pairColumnB]);
					 */

					if (interNode == adjJoinNode) {
						if (needCheckSame) {// has same label
							if (!interIns.sameContains(adjArr[pairColumnB], sameLabelList)) {
								joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
							}
						} else
							joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
					}
					return joinedTable;
				}
			}
		}
		return joinedTable;
	}
	
}
  
