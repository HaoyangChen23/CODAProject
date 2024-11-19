/**  
 * Project Name:frequentPatternsMiningV1.1  
 * File Name:OneEdgeExtension.java  
 * Package Name:mineSteps  
 * Date:Mar 20, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/

package search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import dataStructures.DFSCode;
import dataStructures.Extension;
import dataStructures.GSpanEdge;
import dataStructures.GSpanExtension;
import dataStructures.Graph;
import dataStructures.HPGraph;
import dataStructures.HPListGraph;
import dataStructures.HPMutableGraph;

/**
 * ClassName:OneEdgeExtension Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: Mar 20, 2019
 * 
 * @author JIAN
 * @version
 * @since JDK 1.6
 * @see
 */
public class OneEdgeExtension<NodeType, EdgeType> extends GenerationPartialStep<NodeType, EdgeType> {

	public static int counter = 0;

	private final Map<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>> children;
	private Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;
	private static HashMap<String, Integer> patternEdgeSet;
	private static ArrayList<Integer> sortedNodeLabels;
	private static Graph kGraph;
	private static int[][] maxDegreeMatrix;

	/**
	 * creates a new pruning
	 * 
	 * @param next the next step of the generation chain
	 * @param tenv the environment used for releasing unused objects
	 */
	public OneEdgeExtension(final GenerationPartialStep<NodeType, EdgeType> next,
			final HashMap<String, Integer> patternEdgeSet, int[][] matrix, ArrayList<Integer> nodeLabels, Graph kGraph) {
		super(next);
		this.patternEdgeSet = patternEdgeSet;
		this.maxDegreeMatrix = matrix;//add!!!!!!!!!!!!!!!!!
		this.sortedNodeLabels = nodeLabels;
		this.kGraph = kGraph;
		this.children = new TreeMap<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>>();
		// TODO: evtl schnellere vergleich der gEdges, aber das macht nicht viel
	}
	
//	public OneEdgeExtension(final HashMap<Integer, Map<String, List<Integer>>> map,
//			String n) {
//		super(null,"a");
//
//		this.metaTableMap = map;
//	}
	

	/**
	 * includes the found extension to the corresponding fragment(original)
	 * 
	 * @param gEdge
	 * @param emb
	 * @param code
	 * @param edge
	 * @param nodeB
	 */
	protected void add(final GSpanEdge<NodeType, EdgeType> gEdge, final DFSCode<NodeType, EdgeType> code, int type) {
		// search corresponding extension
		GSpanExtension<NodeType, EdgeType> ext = children.get(gEdge);

		if (ext == null) {
			// create new extension
			HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) code.getHPlistGraph().clone();
			// TODO: avoid clone??
			gEdge.addTo(ng); // reformulate the form of the new extended fragment!!
			ext = new GSpanExtension<NodeType, EdgeType>();
			ext.edge = gEdge;
			ext.frag = new DFSCode<NodeType, EdgeType>(code.getSortedFreqLabels(), code.getSingleGraph(), new HashMap<Integer, HashSet<Integer>>())
					.set((HPListGraph<NodeType, EdgeType>) ng, code.getFirst(), code.getLast(), code.getParents());
			ext.frag = (DFSCode<NodeType, EdgeType>) code.extend(ext); // PUT THE STRING HERE

			children.put(gEdge, ext);
		} else {
			gEdge.release();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 * java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		// just give YOUR extensions to the next step
		extensions.clear();
		extensions.addAll(children.values());
		callNext(node, extensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.
	 * SearchLatticeNode, de.parsemis.graph.Embedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node) {
//		counter++;
//		StopWatch watch = new StopWatch();	
//		watch.start();
		
		extend((DFSCode<NodeType, EdgeType>) node);
//		watch.stop();
//		System.out.println("======================================");
//		System.out.println("extend_elapsedTime:"+watch.getElapsedTime()/1000.0);

		callNext(node); // malhash aii lazma
	}
	
	public static void sendPatternEdge(HashMap<String, Integer> patternEdgeMap){
		patternEdgeSet = patternEdgeMap;
	}
	
	protected final void extend(DFSCode<NodeType, EdgeType> pattern){
		final HPGraph<NodeType, EdgeType> subGraph = pattern.getHPlistGraph();
		final int lastNode = subGraph.getNodeCount() - 1;// 1
		for (int i = 0; i <= lastNode; i++) {// iterate all nodes in this pattern, start at node 0
			int nodeLabeli = (Integer) subGraph.getNodeLabel(i);// A
			int pInDegree = pattern.getHPlistGraph().getInDegree(i);
			int pOutDegree = pattern.getHPlistGraph().getOutDegree(i);
			
			int maxInDegree = maxDegreeMatrix[nodeLabeli][0];//the max in degree of node label
			int maxOutDegree = maxDegreeMatrix[nodeLabeli][1];//the max out degree of node label
			
			// check if the pattern can extend as a cycle
			for (int j = i + 1; j <= lastNode; j++) {
				int hasEdge = subGraph.getEdge(i, j);// if has an edge between node i and j
				// check if can add an edge
				if (hasEdge == -1) {// don't has an edge between this two nodes
					int nodeLabelj = (Integer) subGraph.getNodeLabel(j);
					
					// incoming
					if (pInDegree < maxInDegree) {// in degree is not tight
						for(Entry<String, Integer> entry:patternEdgeSet.entrySet()){
							String oneEdgePatternString = entry.getKey();
							//find the in degree node label 
							int nodeLabelA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int inEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							
							if(nodeLabelB == nodeLabelj && nodeLabelA == nodeLabeli){
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>()
										.set(i, j, sortedNodeLabels.indexOf(nodeLabeli), inEdgeLabel,
												sortedNodeLabels.indexOf(nodeLabelj), -1, nodeLabeli,
												nodeLabelj);
//								if (((pattern.getLast().compareTo(gEdge) < 0))) {
								add(gEdge, pattern, 0);
//								} else {
//									gEdge.release();
//								}
							}
						}
					}else if(pInDegree >= maxInDegree){
//						continue;
					}
					
					// outcoming
					if (pOutDegree < maxOutDegree) {// out degree is not tight
						for(Entry<String, Integer> entry:patternEdgeSet.entrySet()){
							String oneEdgePatternString = entry.getKey();
							//find the in degree node label 
							int nodeLabelA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int outEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							
							if(nodeLabelA == nodeLabeli && nodeLabelB == nodeLabelj){
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>()
										.set(i, j, sortedNodeLabels.indexOf(nodeLabeli), outEdgeLabel,
												sortedNodeLabels.indexOf(nodeLabelj), 1, nodeLabeli,
												nodeLabelj);
//								if (((pattern.getLast().compareTo(gEdge) < 0))) {
								add(gEdge, pattern, 0);
//								} else {
//									gEdge.release();
//								}
							}
						}
					}else if(pOutDegree >= maxOutDegree){
//						continue;
					}

				}else if (hasEdge != -1) {// already has an edge
					continue;
				}

			}
			
			
			// add an new edge extension
			// if node i is not tight
			if (pInDegree < maxInDegree) {// in degree is not tight
				for(Entry<String, Integer> entry:patternEdgeSet.entrySet()){
					String oneEdgePatternString = entry.getKey();
					// find the in degree node label
					int nodeLabelA = Integer.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
					int inEdgeLabel = Integer.parseInt(oneEdgePatternString
							.substring(oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
					int nodeLabelB = Integer.parseInt(oneEdgePatternString.substring(oneEdgePatternString.indexOf("+") + 1,
							oneEdgePatternString.length()));
					
					if (nodeLabelB == nodeLabeli) {
						// add this edge in
						final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(i,
								lastNode + 1, sortedNodeLabels.indexOf(nodeLabeli), inEdgeLabel,
								sortedNodeLabels.indexOf(nodeLabelA), -1, nodeLabeli, nodeLabelA);
//						if (((pattern.getLast().compareTo(gEdge) < 0))) {
						add(gEdge, pattern, 0);
//						} else {
//							gEdge.release();
//						}
					}
				}
			} else if (pInDegree >= maxInDegree) {
//				continue;
			}
			
			
			if (pOutDegree < maxOutDegree) {// out degree is not tight
				for(Entry<String, Integer> entry:patternEdgeSet.entrySet()){
					String oneEdgePatternString = entry.getKey();
					// find the in degree node label
					int nodeLabelA = Integer.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
					int outEdgeLabel = Integer.parseInt(oneEdgePatternString
							.substring(oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
					int nodeLabelB = Integer.parseInt(oneEdgePatternString.substring(oneEdgePatternString.indexOf("+") + 1,
							oneEdgePatternString.length()));
					
					if (nodeLabelA == nodeLabeli) {
						// add this edge in
						final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(i,
								lastNode + 1, sortedNodeLabels.indexOf(nodeLabeli), outEdgeLabel,
								sortedNodeLabels.indexOf(nodeLabelB), 1, nodeLabeli, nodeLabelB);
//						if (((pattern.getLast().compareTo(gEdge) < 0))) {
						add(gEdge, pattern, 0);
//						} else {
//							gEdge.release();
//						}
					}
				}
			} else if (pOutDegree >= maxOutDegree) {
//				continue;
			}
		}
	}

	/*
	 * if has same value between two lists Yes:return this value No:return -1
	 */
	public static int ifSame(List<Integer> a, List<Integer> b) {
		Iterator<Integer> ait = a.iterator();
		Iterator<Integer> bit = b.iterator();
		while (ait.hasNext()) {
			int inEdgeLabel = ait.next();// 3
			while (bit.hasNext()) {
				int outEdgeLabel = bit.next();
				if (inEdgeLabel == outEdgeLabel) {
					return inEdgeLabel;
				} else {
					
				}
			}

		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		children.clear();
		resetNext();
	}

}
