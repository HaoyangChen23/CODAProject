/**  
 * Project Name:kFPD_opt  
 * File Name:UpperBoundOfMNI.java  
 * Package Name:joinAlgorithm  
 * Date:Jul 2, 2021  
 * Copyright (c) 2021, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package joinAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.ConnectedComponent;
import dataStructures.DFSCode;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.IntFrequency;
import dataStructures.Query;
import dataStructures.myNode;

/**  
 * ClassName:UpperBoundOfMNI   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Jul 2, 2021   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class UpperBoundOfMNI <NodeType, EdgeType>{
	
	private Query qry;
	private int currentMinFreq;
	private IntFrequency UB;
	private static Graph KG;
	private static HashMap<String, Integer> patternEdgeMap;
	private static HashMap<Integer, HashMap<Integer, myNode>> superNodeMap;
	private DFSCode<NodeType, EdgeType> pDFSCode;
	
	public UpperBoundOfMNI(Query qry, DFSCode<NodeType, EdgeType> pDFSCode) {
		this.qry = qry;// candidate pattern P: graph format
		this.pDFSCode = (DFSCode<NodeType, EdgeType>) pDFSCode;// P: SimpleDFSCode format
	}

	public UpperBoundOfMNI(Graph singleGraph, HashMap<String, Integer> patternEdgeMap) {
		this.KG = singleGraph;
		this.patternEdgeMap = patternEdgeMap;
	}

	/*
	 * count upper bound of pattern(Query)
	 * 
	 * @param: qry
	 */
	public IntFrequency getUpperBound(int currentMinFreq) {
		this.currentMinFreq = currentMinFreq;
		this.UB = new IntFrequency(0);
		// get the edges from the pattern
		ArrayList<PatternEdges> Edges = qry.getConnectedEdges();
		ArrayList<String> edgeStrings = new ArrayList<String>();
		Iterator es = Edges.iterator();
		while (es.hasNext()) {
			PatternEdges pe = (PatternEdges) es.next();
			String pattern = pe.getPattern();
			String edge = pe.getIndexA() + " " + pe.getIndexB() + " " + pe.getEdgeLabel();
			if (!edgeStrings.contains(edge)) {
				edgeStrings.add(edge);
			} else {
				return new IntFrequency(0);
			}
			// do not contain this pattern edge
			if (!patternEdgeMap.containsKey(pattern)) {
				return new IntFrequency(0);
			}
			// crucial pruning skill
			int edgeFreq = patternEdgeMap.get(pattern);
			if (edgeFreq < currentMinFreq) {
				this.UB = new IntFrequency(edgeFreq);
				UB.setExactFreq();
				return UB;
			}
		}

		this.superNodeMap = new HashMap<Integer, HashMap<Integer, myNode>>();// QueryID -> NodeID->NODE

		ArrayList<ConnectedComponent> cls = qry.getConnectedLabels();

		// refine according to nodeLabels
		for (int i = 0; i < qry.getListGraph().getNodeCount(); i++) {
			int label = qry.getListGraph().getNodeLabel(i);
			superNodeMap.put(i, (HashMap<Integer, myNode>) KG.getFreqNodesByLabel().get(label).clone());
		}

		// refine according to degree !!
		HashMap<Integer, HashMap<Integer, Integer>> nodeOutLabelDegrees = new HashMap<Integer, HashMap<Integer, Integer>>();// nodeID-->(Label,Degree)
		HashMap<Integer, HashMap<Integer, Integer>> nodeInLabelDegrees = new HashMap<Integer, HashMap<Integer, Integer>>();

		for (int i = 0; i < cls.size(); i++) {
			ConnectedComponent c = cls.get(i);
			int nodeA = c.getIndexA();
			int nodeB = c.getIndexB();
			HashMap<Integer, Integer> nodeAmap = nodeOutLabelDegrees.get(nodeA);
			HashMap<Integer, Integer> nodeBmap = nodeInLabelDegrees.get(nodeB);
			if (nodeAmap == null) {
				nodeAmap = new HashMap<Integer, Integer>();
				nodeOutLabelDegrees.put(nodeA, nodeAmap);
			}
			if (nodeBmap == null) {
				nodeBmap = new HashMap<Integer, Integer>();
				nodeInLabelDegrees.put(nodeB, nodeBmap);
			}

			Integer degreeA = nodeAmap.get(c.getLabelB());
			if (degreeA == null)
				degreeA = 0;
			Integer degreeB = nodeBmap.get(c.getLabelA());
			if (degreeB == null)
				degreeB = 0;
			nodeAmap.put(c.getLabelB(), degreeA + 1);
			nodeBmap.put(c.getLabelA(), degreeB + 1);
		}

		for (int i = 0; i < qry.getListGraph().getNodeCount(); i++) {
			HashMap<Integer, Integer> degreeOutCons = nodeOutLabelDegrees.get(i);
			HashMap<Integer, Integer> degreeInCons = nodeInLabelDegrees.get(i);

			HashMap<Integer, myNode> candidates = superNodeMap.get(i);
			boolean isValidNode = true;

			for (Iterator<Entry<Integer, myNode>> it = candidates.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, myNode> nodeEntry = it.next();
				myNode node = nodeEntry.getValue();
				isValidNode = true;
				if (degreeOutCons != null)
					for (Iterator<Entry<Integer, Integer>> iterator = degreeOutCons.entrySet().iterator(); iterator
							.hasNext();) {
						Entry<Integer, Integer> entry = iterator.next();
						int label = entry.getKey();
						int degree = entry.getValue();

						if (node.getOutDegree(label) < degree) {
							isValidNode = false;
							break;
						}
					}
				if (isValidNode && degreeInCons != null) {
					for (Iterator<Entry<Integer, Integer>> iterator = degreeInCons.entrySet().iterator(); iterator
							.hasNext();) {
						Entry<Integer, Integer> entry = iterator.next();
						int label = entry.getKey();
						int degree = entry.getValue();

						if (node.getinDegree(label) < degree) {
							isValidNode = false;
							break;
						}
					}
				}
				if (isValidNode == false)
					it.remove();
			}
		}

		HashSet<Integer> upperDomainSet = new HashSet<Integer>();
		// count upper bound of pattern
		int upper = superNodeMap.get(0).size();
		for (Entry<Integer, HashMap<Integer, myNode>> entry : superNodeMap.entrySet()) {
			HashMap<Integer, myNode> nodeMap = entry.getValue();
			int tmp = nodeMap.size();
			if (tmp < upper) {
				upper = tmp;
			}
			for(Entry<Integer, myNode> nodes:nodeMap.entrySet()) {
				upperDomainSet.add(nodes.getValue().getID());
			}
		}
		UB = new IntFrequency(upper);
		// set cover vertices from upper bound
		pDFSCode.setUpperVertices(upperDomainSet);
		pDFSCode.setQry(qry);
		pDFSCode.setSuperNodes(superNodeMap);
		pDFSCode.setMNIub(UB);
		pDFSCode.setFullEdgeMap(qry.getPEMap());
		return UB;
	}
	
}
  
