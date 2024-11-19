package VF2check;

import java.util.ArrayList;
import java.util.Stack;

import dataStructures.DFSCode;
import dataStructures.DFScodeSerializer;
import joinAlgorithm.ComplexPattern;
import search.SearchLatticeNode;

public class CheckIso <NodeType, EdgeType> {
	
	private static VF2Graph coreGraph;
	private static Stack<Integer> backtrackStack = new Stack<Integer>();

	public CheckIso(VF2Graph coreGraph) {
		this.coreGraph = coreGraph;

	}
	
	public CheckIso(DFSCode<NodeType, EdgeType> query) {
		
		VF2Graph coreGraph = new VF2Graph("coreGraph");
		String codeString = query.toString();
		coreGraph = graphStringrRebuild(codeString, coreGraph);
		this.coreGraph = coreGraph;
	}
	
	
	public static <NodeType, EdgeType> boolean checkIso(SearchLatticeNode<NodeType, EdgeType> pattern) {
		boolean Iso = false;
		
		VF2Graph queryGraph = new VF2Graph("patternGraph");
		String codeString = DFScodeSerializer.serialize(pattern.getHPlistGraph());
		queryGraph = graphStringrRebuild(codeString, queryGraph);
		Iso = checkIsomor(coreGraph, queryGraph);
		return Iso;
	}
	
	public static boolean checkIsomor(VF2Graph targetGraph, VF2Graph queryGraph) {
		VF2State state = new VF2State(targetGraph, queryGraph);

		return matchRecursivePlus(state, targetGraph, queryGraph);
	}
	
	private static boolean matchRecursivePlus(VF2State state, VF2Graph targetGraph, VF2Graph queryGraph) {
		if(state.depth == queryGraph.nodes.size()) {//found a match
			state.matched = true;
			return true;
		}else {
			ArrayList<VF2Pair<Integer,Integer>> candidatePairs = genCandidatePairs(state, targetGraph, queryGraph);
			for (VF2Pair<Integer, Integer> entry : candidatePairs){
				if (checkFeasibility(state, entry.getKey(), entry.getValue())){
					state.extendMatch(entry.getKey(), entry.getValue()); // extend mapping
					if(matchRecursivePlus(state, targetGraph, queryGraph)) {// Found a match
						return true;
					}
					state.backtrack(entry.getKey(), entry.getValue()); // remove the match added before
				}
			}
		}
		return false;
	}
	
	public static VF2Graph graphStringrRebuild(String graphString, VF2Graph graph) {
		String[] strs = graphString.split("\\n");
		if (strs == null || strs.length == 0) {
			System.out.println("GraphString is null !!!!!!!");
		} else {
			for (int i = 0; i < strs.length; i++) {
				String line = strs[i].toString();
				if (line.startsWith("v")) {
					String[] vStrs = line.split(" ");
					int nodeId = Integer.parseInt(vStrs[1]);
					int nodeLabel = Integer.parseInt(vStrs[2]);
					graph.addNode(nodeId, nodeLabel);
				} else if (line.startsWith("e")) {
					String[] eStrs = line.split(" ");
					int sourceId = Integer.parseInt(eStrs[1]);
					int targetId = Integer.parseInt(eStrs[2]);
					int edgeLabel = Integer.parseInt(eStrs[3]);
					graph.addEdge(sourceId, targetId, edgeLabel);
				}
			}
		}
		return graph;
	}
	
	/**
	 * Generate all candidate pairs given current state
	 * @param state			VF2 State
	 * @param targetGraph	Big Graph
	 * @param queryGraph	Small Graph
	 * @return				Candidate Pairs
	 */
	private static ArrayList<VF2Pair<Integer,Integer>> genCandidatePairs(VF2State state, VF2Graph targetGraph, VF2Graph queryGraph) {
		ArrayList<VF2Pair<Integer,Integer>> pairList = new ArrayList<VF2Pair<Integer,Integer>>();
		
		if (!state.T1out.isEmpty() && !state.T2out.isEmpty()){
			// Generate candidates from T1out and T2out if they are not empty
			
			// Faster Version
			// Since every node should be matched in query graph
			// Therefore we can only extend one node of query graph (with biggest id)
			// instead of generate the whole Cartesian product of the target and query 
			int queryNodeIndex = -1;
			for (int i : state.T2out) {
				queryNodeIndex = Math.max(i, queryNodeIndex);
			}
			for (int i : state.T1out) {
				pairList.add(new VF2Pair<Integer,Integer>(i, queryNodeIndex));
			}
			
			// Slow Version
//			for (int i : state.T1out){
//				for (int j : state.T2out){
//					pairList.add(new Pair<Integer,Integer>(i, j));
//				}
//			}
			return pairList;
		} else if (!state.T1in.isEmpty() && !state.T2in.isEmpty()){
			// Generate candidates from T1in and T2in if they are not empty
			
			// Faster Version
			// Since every node should be matched in query graph
			// Therefore we can only extend one node of query graph (with biggest id)
			// instead of generate the whole Cartesian product of the target and query 
			int queryNodeIndex = -1;
			for (int i : state.T2in) {
				queryNodeIndex = Math.max(i, queryNodeIndex);
			}
			for (int i : state.T1in) {
				pairList.add(new VF2Pair<Integer,Integer>(i, queryNodeIndex));
			}
			
			// Slow Version
//			for (int i : state.T1in){
//				for (int j : state.T2in){
//					pairList.add(new Pair<Integer,Integer>(i, j));
//				}
//			}
			return pairList;
		} else {
			// Generate from all unmapped nodes
			
			// Faster Version
			// Since every node should be matched in query graph
			// Therefore we can only extend one node of query graph (with biggest id)
			// instead of generate the whole Cartesian product of the target and query 
			int queryNodeIndex = -1;
			for (int i : state.unmapped2) {
				queryNodeIndex = Math.max(i, queryNodeIndex);
			}
			for (int i : state.unmapped1) {
				pairList.add(new VF2Pair<Integer,Integer>(i, queryNodeIndex));
			}
			
			// Slow Version
//			for (int i : state.unmapped1){
//				for (int j : state.unmapped2){
//					pairList.add(new Pair<Integer,Integer>(i, j));
//				}
//			}
			return pairList;
		}
	}
		
	/**
	 * Check the feasibility of adding this match
	 * @param state				VF2 State
	 * @param targetNodeIndex	Target Graph Node Index
	 * @param queryNodeIndex	Query Graph Node Index
	 * @return					Feasible or not
	 */
	private static Boolean checkFeasibility(VF2State state , int targetNodeIndex , int queryNodeIndex) {
		// Node Label Rule
		// The two nodes must have the same label
//		if (state.targetGraph.nodes.get(targetNodeIndex).label !=
//				state.queryGraph.nodes.get(queryNodeIndex).label){
//			return false;
//		}
		if (state.targetGraph.nodes.get(targetNodeIndex).label != -1) {
			if (state.targetGraph.nodes.get(targetNodeIndex).label != state.queryGraph.nodes
					.get(queryNodeIndex).label) {
				return false;
			}
		} else if (state.targetGraph.nodes.get(targetNodeIndex).label == -1) {

		}		
		// Predecessor Rule and Successor Rule
		if (!checkPredAndSucc(state, targetNodeIndex, queryNodeIndex)){
			return false;
		}
		
		// In Rule and Out Rule
		if (!checkInAndOut(state, targetNodeIndex, queryNodeIndex)){
			return false;
		}

		// New Rule
		if (!checkNew(state, targetNodeIndex, queryNodeIndex)){
			return false;
		}
				
		return true; 
	}
	
	/**
	 * Check the predecessor rule and successor rule
	 * It ensures the consistency of the partial matching
	 * @param state				VF2 State
	 * @param targetNodeIndex	Target Graph Node Index
	 * @param queryNodeIndex	Query Graph Node Index
	 * @return					Feasible or not
	 */
private static Boolean checkPredAndSucc(VF2State state, int targetNodeIndex , int queryNodeIndex) {
		
	VF2Node targetNode = state.targetGraph.nodes.get(targetNodeIndex);
	VF2Node queryNode = state.queryGraph.nodes.get(queryNodeIndex);
		int[][] targetAdjacency = state.targetGraph.getAdjacencyMatrix();
		int[][] queryAdjacency = state.queryGraph.getAdjacencyMatrix();
		
		// Predecessor Rule
		// For all mapped predecessors of the query node, 
		// there must exist corresponding predecessors of target node.
		// Vice Versa
		for (VF2Edge e : targetNode.inEdges) {
			if (state.core_1[e.source.id] > -1) {
				if (queryAdjacency[state.core_1[e.source.id]][queryNodeIndex] == -1){
					return false;	// not such edge in target graph
				} else if (queryAdjacency[state.core_1[e.source.id]][queryNodeIndex] != e.label) {
					if(e.label == -1) {
						return true;
					}
					return false;	// label doesn't match
				}
			}
		}
		
		for (VF2Edge e : queryNode.inEdges) {
			if (state.core_2[e.source.id] > -1) {
				if (targetAdjacency[state.core_2[e.source.id]][targetNodeIndex] == -1){
//					return false;	// not such edge in target graph
					return true;	// skip
				} else if (targetAdjacency[state.core_2[e.source.id]][targetNodeIndex] != e.label){
					return false;	// label doesn't match
				}
			}
		}
		
		// Successsor Rule
		// For all mapped successors of the query node,
		// there must exist corresponding successors of the target node
		// Vice Versa
		for (VF2Edge e : targetNode.outEdges) {
			if (state.core_1[e.target.id] > -1) {
				if (queryAdjacency[queryNodeIndex][state.core_1[e.target.id]] == -1){
					return false;	// not such edge in target graph
				} else if (queryAdjacency[queryNodeIndex][state.core_1[e.target.id]] != e.label) {
					if(e.label == -1) {
						return true;
					}
					return false;	// label doesn't match
				}
			}
		}
		
		for (VF2Edge e : queryNode.outEdges) {
			if (state.core_2[e.target.id] > -1) {
				if (targetAdjacency[targetNodeIndex][state.core_2[e.target.id]] == -1){
//					return false;	// not such edge in target graph
					return true;	// skip
				} else if (targetAdjacency[targetNodeIndex][state.core_2[e.target.id]] != e.label) {
					return false;	// label doesn't match
				}
			}
		}
		
		return true;
	}

/**
 * Check the in rule and out rule
 * This prunes the search tree using 1-look-ahead
 * @param state				VF2 State
 * @param targetNodeIndex	Target Graph Node Index
 * @param queryNodeIndex	Query Graph Node Index
 * @return					Feasible or not
 */
private static boolean checkInAndOut(VF2State state, int targetNodeIndex , int queryNodeIndex) {
	
	VF2Node targetNode = state.targetGraph.nodes.get(targetNodeIndex);
	VF2Node queryNode = state.queryGraph.nodes.get(queryNodeIndex);
	
	int targetPredCnt = 0, targetSucCnt = 0;
	int queryPredCnt = 0, querySucCnt = 0;
	
	// In Rule
	// The number predecessors/successors of the target node that are in T1in 
	// must be larger than or equal to those of the query node that are in T2in
	for (VF2Edge e : targetNode.inEdges){
		if (state.inT1in(e.source.id)){
			targetPredCnt++;
		}
	}
	for (VF2Edge e : targetNode.outEdges){
		if (state.inT1in(e.target.id)){
			targetSucCnt++;
		}
	}
	for (VF2Edge e : queryNode.inEdges){
		if (state.inT2in(e.source.id)){
			queryPredCnt++;
		}
	}
	for (VF2Edge e : queryNode.outEdges){
		if (state.inT2in(e.target.id)){
			querySucCnt++;//modify
		}
	}
	if (targetPredCnt < queryPredCnt || targetSucCnt < querySucCnt){
		return false;
	}

	// Out Rule
	// The number predecessors/successors of the target node that are in T1out 
	// must be larger than or equal to those of the query node that are in T2out
	for (VF2Edge e : targetNode.inEdges){
		if (state.inT1out(e.source.id)){
			targetPredCnt++;
		}
	}
	for (VF2Edge e : targetNode.outEdges){
		if (state.inT1out(e.target.id)){
			targetSucCnt++;
		}
	}
	for (VF2Edge e : queryNode.inEdges){
		if (state.inT2out(e.source.id)){
			queryPredCnt++;
		}
	}
	for (VF2Edge e : queryNode.outEdges){
		if (state.inT2out(e.target.id)){
			querySucCnt++;//modify
		}
	}
	if (targetPredCnt < queryPredCnt || targetSucCnt < querySucCnt){
		return false;
	}		
	
	return true;
}

/**
 * Check the new rule
 * This prunes the search tree using 2-look-ahead
 * @param state				VF2 State
 * @param targetNodeIndex	Target Graph Node Index
 * @param queryNodeIndex	Query Graph Node Index
 * @return					Feasible or not
 */
private static boolean checkNew(VF2State state, int targetNodeIndex , int queryNodeIndex){
	
	VF2Node targetNode = state.targetGraph.nodes.get(targetNodeIndex);
	VF2Node queryNode = state.queryGraph.nodes.get(queryNodeIndex);
	
	int targetPredCnt = 0, targetSucCnt = 0;
	int queryPredCnt = 0, querySucCnt = 0;
	
	// In Rule
	// The number predecessors/successors of the target node that are in T1in 
	// must be larger than or equal to those of the query node that are in T2in
	for (VF2Edge e : targetNode.inEdges){
		if (state.inN1Tilde(e.source.id)){
			targetPredCnt++;
		}
	}
	for (VF2Edge e : targetNode.outEdges){
		if (state.inN1Tilde(e.target.id)){
			targetSucCnt++;
		}
	}
	for (VF2Edge e : queryNode.inEdges){
		if (state.inN2Tilde(e.source.id)){
			queryPredCnt++;
		}
	}
	for (VF2Edge e : queryNode.outEdges){
		if (state.inN2Tilde(e.target.id)){
//			queryPredCnt++;
			querySucCnt++;
		}
	}
	if (targetPredCnt < queryPredCnt || targetSucCnt < querySucCnt){
		return false;
	}
	
	return true;
}


}
