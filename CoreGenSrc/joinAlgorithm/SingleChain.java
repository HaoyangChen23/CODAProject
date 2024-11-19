package joinAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import dataStructures.HPListGraph;
import dataStructures.IntIterator;
import dataStructures.Query;

public class SingleChain extends InsTable{
	private ArrayList<Integer> edgeList;
	private ArrayList<Integer> patternNodes;
	private static HPListGraph<Integer, Double> qryGraph;
	private static Query qry;
	private int upperBound;
	
	public SingleChain(ArrayList<Integer> sclist, Query qry, HPListGraph<Integer, Double> qryGraph) {
		this.qryGraph = qryGraph;
		this.qry = qry;
		edgeList = new ArrayList<Integer>();
		patternNodes = new ArrayList<Integer>();
		for (int i = 0; i < sclist.size() - 1; i++) {
			int j = i + 1;
			int nodeI = sclist.get(i);
			int nodeJ = sclist.get(j);
			if(!patternNodes.contains(nodeI)){
				patternNodes.add(nodeI);
			}
			if(!patternNodes.contains(nodeJ)){
				patternNodes.add(nodeJ);
			}
			int edgeIdx = qryGraph.getEdge(nodeI, nodeJ);
			if (edgeIdx == -1) {
				edgeIdx = qryGraph.getEdge(nodeJ, nodeI);
			}
			edgeList.add(edgeIdx);
		}
	}
	
	public ArrayList<PatternEdges> getConnectedEdges()
	{
		ArrayList<PatternEdges> cls = new ArrayList<PatternEdges>();
		HashMap<Integer, PatternEdges> peMap = qry.getPEMap();
		Iterator it = edgeList.iterator();
		while(it.hasNext()){
			int edgeIdx = (int) it.next();
			PatternEdges pe = peMap.get(edgeIdx);
			cls.add(pe);
		}
		return cls;
	}
	
	public IntIterator getEdgeIndices(int node){
		return qryGraph.getEdgeIndices(node);
	}
	
	public ArrayList<Integer> getEdgeList(){
		return edgeList;
	}
	
	public ArrayList<Integer> getPatternNodes(){
		return patternNodes;
	}

	public void setUpperBound(int upper) {
		this.upperBound = upper;
	}
	public int getUpper(){
		return upperBound;
	}

}
