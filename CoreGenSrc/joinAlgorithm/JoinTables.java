package joinAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.HPListGraph;
import dataStructures.IntFrequency;
import dataStructures.IntIterator;
import dataStructures.Query;
import utilities.Settings;

public class JoinTables {
	private HashMap<Integer, PatternEdges> edgesMap;
	private ArrayList<Instances> reTables;
	private SingleChain scPattern;
	private int currentMinFreq;
	
	public JoinTables(HashMap<Integer, PatternEdges> edgesMap, SingleChain sc, int currentMinFreq) {
		this.edgesMap = edgesMap;
		this.reTables = new ArrayList<Instances>();
		this.scPattern = sc;
		this.currentMinFreq = currentMinFreq;
	}
	
	
	@SuppressWarnings("unchecked")
	public InsTable joinTable(PatternEdges curEdge, InsTable scTables) {
		//next join table of Tp in P'T
		HashMap<Integer, PatternEdges> adjEdges = adjacentEdges(curEdge);
		for (Entry<Integer, PatternEdges> entry : adjEdges.entrySet()) {
			PatternEdges adjEdge = entry.getValue();
			ArrayList<int[]> adjNodePairs = adjEdge.getNodePairs();
			int joinNode = -1;
			int addIdx = -1;
			int pairColloumnA = -1;
			int pairColloumnB = -1;
			if (adjEdge.getIndexA() == curEdge.getIndexA() || adjEdge.getIndexA() == curEdge.getIndexB()) {
				joinNode = adjEdge.getIndexA();
				addIdx = adjEdge.getIndexB();
				pairColloumnA = 0;
				pairColloumnB = 1;
			} else if (adjEdge.getIndexB() == curEdge.getIndexA() || adjEdge.getIndexB() == curEdge.getIndexB()) {
				joinNode = adjEdge.getIndexB();
				addIdx = adjEdge.getIndexA();
				pairColloumnA = 1;
				pairColloumnB = 0;
			}
			
			//sort the instances in join attribute by increasing order
			final int s = pairColloumnA;
			Collections.sort(adjNodePairs, new ComparatorPair(){
				@Override
				public int compare(Object o1, Object o2) {
					int[] p1 = (int[])o1;
					int[] p2 = (int[])o2;
					return p1[s] - p2[s];
				}
			});
			
			scTables.sortIns(joinNode);
			InsTable insTable = new InsTable();
			for(int i = 0; i<scTables.getSize(); i++){
				Instances ins = scTables.get(i);
				Iterator it = adjNodePairs.iterator();
				while(it.hasNext()){
					int[] adjArr = (int[]) it.next();
					if(ins.getNode(joinNode) < adjArr[pairColloumnA]){
						break;
					}
					if(ins.getNode(joinNode) > adjArr[pairColloumnA]){
						it.remove();
					}
					if(ins.getNode(joinNode) == adjArr[pairColloumnA]){
						insTable.joinAssign(ins, addIdx, adjArr[pairColloumnB]);
					}
				}
			}
			
			int freq = insTable.ifrequency();
			if(Settings.earlyTermination){
				if(freq < currentMinFreq){
					return null;
				}
			}
			
			// remove from edges
			edgesMap.remove(adjEdge.getEdgeIdx());
			scTables = joinTable(adjEdge, insTable);
			if(scTables == null)
				return null;
		}
		return scTables;
	}

	// get the adjacent edges of curEdge in pattern
	public HashMap<Integer, PatternEdges> adjacentEdges(PatternEdges curEdge) {
		HashMap<Integer, PatternEdges> adjEdgeTables = new HashMap<Integer, PatternEdges>();
		int nodeA = curEdge.getIndexA();
		int nodeB = curEdge.getIndexB();
		IntIterator nodeANeighbors = scPattern.getEdgeIndices(nodeA);
		while (nodeANeighbors.hasNext()) {
			int edgeN = nodeANeighbors.next();
			PatternEdges edgeTable = edgesMap.get(edgeN);
			if (edgesMap.containsKey(edgeN) && edgeTable.getEdgeIdx() != curEdge.getEdgeIdx()) {
				adjEdgeTables.put(edgeN, edgeTable);
			}
		}
		IntIterator nodeBNeighbors = scPattern.getEdgeIndices(nodeB);
		while (nodeBNeighbors.hasNext()) {
			int edgeN = nodeBNeighbors.next();
			PatternEdges edgeTable = edgesMap.get(edgeN);
			if (edgesMap.containsKey(edgeN) && edgeTable.getEdgeIdx() != curEdge.getEdgeIdx()) {
				adjEdgeTables.put(edgeN, edgeTable);
			}
		}
		return adjEdgeTables;
	}

	public int getJoinNode(PatternEdges rEdge, PatternEdges curEdge) {
		if (rEdge.getIndexA() == curEdge.getIndexA() || rEdge.getIndexA() == curEdge.getIndexB()) {
			return rEdge.getIndexA();
		} else if (rEdge.getIndexB() == curEdge.getIndexA() || rEdge.getIndexB() == curEdge.getIndexB()) {
			return rEdge.getIndexB();
		}
		return -1;
	}

}
