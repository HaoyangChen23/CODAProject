package joinAlgorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import CSP.DFSSearch;
import CSP.Variable;
import dataStructures.ConnectedComponent;
import dataStructures.DFSCode;
import dataStructures.Frequency;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.IntFrequency;
import dataStructures.IntIterator;
import dataStructures.Query;
import dataStructures.myNode;
import search.SearchLatticeNode;
import utilities.MyPair;
import utilities.Settings;
import utilities.StopWatch;

public class JoinMNI<NodeType, EdgeType> {
	private Query qry;
	private int currentMinFreq;
	private IntFrequency UB;
	private HPListGraph<Integer, Double> qryGraph;
	private HashMap<String, ArrayList<int[]>> nodePairMap;
	private int qrySize;
	private static HashMap<Integer, HashMap<Integer, myNode>> superNodeMap;
	private static HashMap<Integer, HashSet<Integer>> domainMap;
	private DFSCode<NodeType, EdgeType> pDFSCode;
	private HashSet<Integer> hasTraverseNodes;
	private static HashMap<Integer, Integer> nodeLabelMap;// <nodeIdx, nodeLabel>
	private HashMap<Integer, PatternEdges> esMap;
	private HashMap<Integer, PatternEdges> fullEdgeMap;

	public JoinMNI(DFSCode<NodeType, EdgeType> code) {
		this.esMap = new HashMap<Integer, PatternEdges>();
		this.domainMap = new HashMap<Integer, HashSet<Integer>>();
		this.hasTraverseNodes = new HashSet<>();
		this.pDFSCode = (DFSCode<NodeType, EdgeType>) code;// P: DFSCode format
		this.superNodeMap = code.getSuperNodes();
		this.UB = code.getMNIub();
		this.fullEdgeMap = code.getFullEdgeMap();
		this.qry = code.getQuery();// candidate pattern P: graph format
		this.qryGraph = qry.getListGraph();
		this.nodeLabelMap = qry.getNodeLabelMap();
		this.qrySize = qryGraph.getNodeCount();
	}
	
	public void finalizeIt() {
		nodePairMap = null;
	}

	
	/*
	 * the join algorithm in kFPD problem
	 * @param: P*
	 * @return: the next most frequent pattern
	 */
	public void joinMNIPlus(Frequency threshold) {
		IntFrequency frq = new IntFrequency(0);
		//First join pass
		
		/****************** Test *******************/
//		Frequency GramiMNI = pDFSCode.frequency(0);

		// create all edge tables of P
		HashMap<Integer, PatternEdges> allEdgeTables = qry.getPEMap();
		for (Entry<Integer, PatternEdges> pesEntry : allEdgeTables.entrySet()) {
			PatternEdges pe = pesEntry.getValue();
			double edgeLabel = pe.getEdgeLabel();
			int nodeA = pe.getIndexA();// node index in pattern
			int nodeB = pe.getIndexB();
			HashMap<Integer, myNode> nodeAmap = superNodeMap.get(nodeA);
			HashMap<Integer, myNode> nodeBmap = superNodeMap.get(nodeB);
			ArrayList<int[]> nodePairs = getNodepairs(pe, nodeAmap, edgeLabel, nodeBmap);
			pe.setListOnly(nodePairs);
		}
		
		/************** join each two tables ********************/
/*		esMap = (HashMap<Integer, PatternEdges>) qry.getPEMap().clone();
		PatternEdges initialEdge = esMap.get(0);
		esMap.remove(initialEdge.getEdgeIdx());
		UB = tighterUpperBound(initialEdge);*/
		// set list
		for (Entry<Integer, PatternEdges> pesEntry : allEdgeTables.entrySet()) {
			PatternEdges pe = pesEntry.getValue();
			ArrayList<int[]> nodePairs = pe.getNodePairs();
			pe.setTraverseList(nodePairs);
			int freq = pe.ifrequency();// set min MNI node
			if(freq == 0) {
				System.out.println("MNI equals to 0 !!!");
				pDFSCode.store(frq);
				return;
			}
		}
		
		int tau = UB.intValue();// initialize a constant
		if(UB.compareTo(threshold) <= 0) {
			pDFSCode.setMNIub(UB);
			return;
		}
		
		/************** phase I ***************/
		System.out.println("First join pass ...");
//		firstJoinPass();
		easyTupleReduce();

		
		/************** phase II ***************/
		System.out.println("Second join pass ...");
		// edgesMap: <edgeIdx, patternEdges>
		HashMap<Integer, PatternEdges> edgesMap = (HashMap<Integer, PatternEdges>) allEdgeTables.clone();
		HashMap<Integer, PatternEdges> afterJoinEdgeMap = new HashMap<Integer, PatternEdges>();
		while (edgesMap != null && edgesMap.size() != 0) {
			// find the table has minimum MNI
			int m = -1;
			int minEdge = -1;
			for (Entry<Integer, PatternEdges> eEntry : edgesMap.entrySet()) {
				PatternEdges edge = eEntry.getValue();
				if (m == -1) {
					m = edge.getMNI();
					minEdge = edge.getEdgeIdx();
				} else if (m > edge.getMNI()) {
					m = edge.getMNI();
					minEdge = edge.getEdgeIdx();
				}
			}
			PatternEdges table = edgesMap.get(minEdge);// table has minimum MNI
			edgesMap.remove(minEdge);

			// traverse the table
			int domSize = table.getValidSet().size();
			tau = traverseTable(table, tau, domSize, allEdgeTables, afterJoinEdgeMap);
		}
		
		//evaluate the MNI of P by result table
		frq = new IntFrequency(tau);
		//store info
		pDFSCode.setDomMap(domainMap);
//		pDFSCode.setFullEdgeMap(allEdgeTables);
		pDFSCode.setJoinEdgeMap(afterJoinEdgeMap);
		pDFSCode.store(frq);
//		if(GramiMNI.compareTo(frq) != 0) {
//			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		}
//		System.out.println("MNI by Grami: "+ GramiMNI);
		System.out.println("MNI by join: "+ frq);
		return;
	}
	
	public InsTable firstJoinPass() {
		int tau = UB.intValue();// initialize a constant
		// reduce each edge table T in PT
		HashMap<Integer, PatternEdges> reducedTables = new HashMap<Integer, PatternEdges>();
		for (Entry<Integer, PatternEdges> allEntry : qry.getPEMap().entrySet()) {
			PatternEdges edge = allEntry.getValue();
			PatternEdges reducedEdge = null;
			reducedEdge = edge.copy();
			ArrayList<int[]> nodePairs = null;
			nodePairs = tupleReduceEdge(edge);// TODO
			reducedEdge.setList(nodePairs);
			reducedTables.put(reducedEdge.getEdgeIdx(), reducedEdge);
		}
		InsTable ReTable = new InsTable();
		// full join every table T' in PT
		PatternEdges minPe = reducedTables.get(0);// starts at the first edge
		ArrayList<int[]> nodePairs = minPe.getNodePairs();
		for (int i = 0; i < nodePairs.size(); i++) {
			int arr[] = nodePairs.get(i);
			// initialize the instance by pattern size
			Instances instance = new Instances(qrySize);
			instance.assign(minPe.getIndexA(), arr[0]);// (pattern nodeIdx, node)
			instance.assign(minPe.getIndexB(), arr[1]);
			ReTable.addItem(instance);
		}
		reducedTables.remove(0);
		int joinCount = 1;
		while (reducedTables != null && reducedTables.size() != 0) {
			// adjacent table of the edge table
			PatternEdges adjEdge = getAdjacentTable(ReTable, reducedTables);
			if (adjEdge == null || ReTable.getSize() == 0)
				break;
			reducedTables.remove(adjEdge.getEdgeIdx());
			joinCount++;
			ReTable = fullJoinTables(ReTable, adjEdge, joinCount, tau);
		}
		// Mark all values in sample table to the corresponding domains
		if (ReTable.getSize() != 0) {
			markNodes(ReTable);
		}
		
		return ReTable;
	}

	public InsTable easyTupleReduce() {
		InsTable ReTable = new InsTable();
		// reduce all the edges
		for (Entry<Integer, PatternEdges> pesEntry : qry.getPEMap().entrySet()) {
			PatternEdges table = pesEntry.getValue();
			// reduce tuples
			HashMap<Integer, PatternEdges> pesMap = new HashMap<Integer, PatternEdges>();
			pesMap.putAll((HashMap<Integer, PatternEdges>) qry.getPEMap().clone());// <edgeIdx, patternEdges>
			InsTable sampleTable = table.tupleReduce(qrySize);// reduce tuples in the table
			pesMap.remove(table.getEdgeIdx());
			while (pesMap != null && pesMap.size() != 0) {
				// adjacent table of pe
				PatternEdges adjEdge = getAdjacentTable(sampleTable, pesMap);
				if (adjEdge == null || sampleTable.getSize() == 0)
					break;
				pesMap.remove(adjEdge.getEdgeIdx());
				// if join success then next
				sampleTable = priorJoinTables(sampleTable, adjEdge);
			}
			// Mark all values in sample table to the corresponding domains
			if (sampleTable.getSize() != 0) {
				for (int i = 0; i < sampleTable.getSize(); i++) {
					Instances ins = sampleTable.get(i);
					ReTable.addItem(ins);
				}
				markNodes(sampleTable);
			}
		}
		return ReTable;
	}

	public int traverseTable(PatternEdges table, int tau, int domSize, HashMap<Integer, PatternEdges> allEdgeTables,
			HashMap<Integer, PatternEdges> afterJoinEdgeMap) {
		int minNode = table.getMinNode();
		while (domSize < tau && table.unTraversedSet().size() != 0) {
			// get a batch of unmarked tuples
			int needGetCount = tau - domSize;
			InsTable RTable = table.getTuples(needGetCount, qrySize);// TODO //has mark!!!
//			System.out.println("get tuples size:"+RTable.getSize());
			HashMap<Integer, PatternEdges> midMap = (HashMap<Integer, PatternEdges>) allEdgeTables.clone();
			midMap.remove(table.getEdgeIdx());
			int joinCount = 1;
			while (midMap != null && midMap.size() != 0) {
				// adjacent table of RTable
				PatternEdges adjEdge = getAdjacentTable(RTable, midMap);
				if (adjEdge == null || RTable.getSize() == 0)
					break;
				midMap.remove(adjEdge.getEdgeIdx());
//				System.out.println("join tables:"+RTable.getPatNodes()+" - ["+adjEdge.getIndexA()+","+adjEdge.getIndexB()+"]");
//				System.out.println("RTable size:"+RTable.getSize()+" adjEdge size:"+adjEdge.getNodePairs().size());
				// full join
				joinCount++;
				RTable = fullJoinTables(RTable, adjEdge, joinCount, tau);
//				System.out.println("after join RTable size:"+RTable.getSize());
			}
			// mark all values in R to the corresponding domains
			if (RTable.getSize() != 0) {
				pDFSCode.setTupleSize(RTable.getSize());
//				System.out.println("tuple size: "+pDFSCode.getTupleSize());
				markNodes(RTable);
			}
			domSize = table.getValidSet().size();// lower bound of MNI
			hasTraverseNodes.add(table.getMinNode());
		}
		if (domSize < tau)
			tau = domSize;
//		System.out.println("Tau:" + tau);
		// the table has a smaller MNI node ??
		if (domainMap.size() != 0) {
			int minOfTable = 0;
			int domASize = domainMap.get(table.getIndexA()).size();
			int domBSize = domainMap.get(table.getIndexB()).size();
			if (domASize < domBSize) {
				minOfTable = domASize;
			} else {
				minOfTable = domBSize;
			}
			// get another node
			if (minOfTable < domSize) {
				int anotherNode = table.getAnother();
				if (!hasTraverseNodes.contains(anotherNode)) {
					// change minMNInode to another one
					table.changeMinNode();
					tau = traverseTable(table, tau, minOfTable, allEdgeTables, afterJoinEdgeMap);
				} else
					return tau;
			}
		}
		afterJoinEdgeMap.put(table.getEdgeIdx(), table);
		return tau;
	}

	/*
	 * mark the valid instances in corresponding domains
	 * 
	 * @param: validTable: contains the valid join results(R)
	 */
	public void markNodes(InsTable validTable) {
		ArrayList<Instances> instanceList = validTable.getInsList();
		Iterator it = instanceList.iterator();
		while (it.hasNext()) {
			Instances ins = (Instances) it.next();
			for (int i = 0; i < qrySize; i++) {
				if (domainMap.get(i) == null) {
					HashSet<Integer> dom = new HashSet<Integer>();
					dom.add(ins.getNode(i));
					domainMap.put(i, dom);
				} else
					domainMap.get(i).add(ins.getNode(i));
			}
		}
		// need set marked nodes to pattern edges
		HashMap<Integer, PatternEdges> pesMap = qry.getPEMap();
		for (Entry<Integer, PatternEdges> pesEntry : pesMap.entrySet()) {
			PatternEdges pe = pesEntry.getValue();
			int nodeA = pe.getIndexA();
			int nodeB = pe.getIndexB();
			HashSet<Integer> domA = domainMap.get(nodeA);
			HashSet<Integer> domB = domainMap.get(nodeB);
			pe.setMarkNodes(nodeA, domA);
			pe.setMarkNodes(nodeB, domB);
		}
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

	/*
	 * @param interTable: intermediate table adjEdge: the adjacent edge of the
	 * intermediate table
	 */
	@SuppressWarnings("unchecked")
	private InsTable priorJoinTables(InsTable interTable, PatternEdges adjEdge) {
		ArrayList<int[]> adjNodePairs = adjEdge.getNodePairs();
		int joinNode = adjEdge.getJoinNode();
		int addIdx = -1;
		int pairColumnA = -1;
		int pairColumnB = -1;
		if (adjEdge.getIndexA() == joinNode) {
			addIdx = adjEdge.getIndexB();
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

		// sort the instances in join attribute by increasing order
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
				int interNode = interIns.getNode(joinNode);

				String curInter = interIns.getNode(adjEdge.getIndexA()) + "_" + interIns.getNode(adjEdge.getIndexB());
				Iterator adjIt = adjNodePairs.iterator();
				while (adjIt.hasNext()) {
					int[] adjArr = (int[]) adjIt.next();
					if (interNode < adjArr[pairColumnA])
						break;
					if (interNode > adjArr[pairColumnA])
						continue;
					String adjIntr = adjArr[0] + "_" + adjArr[1];
					if (curInter.equals(adjIntr))
//						interIns.setCycValid(true);
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
					 * !interIns.contains(adjArr[pairColumnB])) { joinedTable.joinAssign(interIns,
					 * addIdx, adjArr[pairColumnB]); break;// prior join }
					 */

					if (interNode == adjJoinNode) {
						if (needCheckSame) {// has same label
							if (!interIns.sameContains(adjArr[pairColumnB], sameLabelList)) {
								joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
								break;// prior join
							}
						} else {
							joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
							break;// prior join
						}

					}

				}

			}
		}
		return joinedTable;
	}

	/*
	 * @param interTable: intermediate table 
	 * @param adjEdge: the adjacent edge of the intermediate table
	 */
	@SuppressWarnings("unchecked")
	private InsTable fullJoinTables(InsTable interTable, PatternEdges adjEdge, int joinCount, int tau) {
		ArrayList<int[]> adjNodePairs = adjEdge.getNodePairs();
//		System.out.println("adjNodePairs size: "+adjNodePairs.size());
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
//						interIns.setCycValid(true);
						joinedTable.getInsList().add(interIns);
				}
			}
		} else {// if the intermediate table and its adjacent edge can not construct a cyclic
			if (joinCount != qryGraph.getEdgeCount()) {
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
			} else if (joinCount == qryGraph.getEdgeCount()) {// the last edge of the pattern
//				HashMap<Integer, HashSet<Integer>> needMark = new HashMap<Integer, HashSet<Integer>>();
//				System.out.println("interTable size: "+interTable.getSize());
				OUT: for (int i = 0; i < interTable.getSize(); i++) {
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

						
						  if (interNode == adjJoinNode && !interIns.contains(adjArr[pairColumnB])) { //
						  joinedTable.joinAssign(interIns, addIdx, adjArr[pairColumnB]);
						  
						 /*********** new ***********/
/*							Integer[] interArr = interIns.getIsoInstances();
							Integer[] validArr = interArr.clone();
							validArr[addIdx] = adjArr[pairColumnB]; // mark corresponding domains instead of store the
																	// instances
							boolean goOn = false;
							for (int j = 0; j < qrySize; j++) {
								HashSet<Integer> set = domainMap.get(j);
								if (set != null && set.size() < tau) {
									set.add(validArr[j]);
									goOn = true;
								} else if (set == null) {
									set = new HashSet<Integer>();
									set.add(validArr[j]);
									domainMap.put(j, set);
									goOn = true;
								}
							}
							if (!goOn) {
								System.out.println("================== break !!! ===================");
								break OUT;
							}*/
						}
														 

						if (interNode == adjJoinNode) {
							if (needCheckSame) {// has same label
								if (!interIns.sameContains(adjArr[pairColumnB], sameLabelList)) {
									Integer[] interArr = interIns.getIsoInstances();
									Integer[] validArr = interArr.clone();
									validArr[addIdx] = adjArr[pairColumnB];
									// mark corresponding domains instead of store the instances
									boolean goOn = false;
									for (int j = 0; j < qrySize; j++) {
										HashSet<Integer> set = domainMap.get(j);
										if (set != null && set.size() < tau) {
											set.add(validArr[j]);
											goOn = true;
										} else if (set == null) {
											set = new HashSet<Integer>();
											set.add(validArr[j]);
											domainMap.put(j, set);
											goOn = true;
										}
									}
									if (!goOn) {
										System.out.println("================== break !!! ===================");
										break OUT;
									}
								}
							} else {
								Integer[] interArr = interIns.getIsoInstances();
								Integer[] validArr = interArr.clone();
								validArr[addIdx] = adjArr[pairColumnB];
								// mark corresponding domains instead of store the instances
								boolean goOn = false;
								for (int j = 0; j < qrySize; j++) {
									HashSet<Integer> set = domainMap.get(j);
									if (set != null && set.size() < tau) {
										set.add(validArr[j]);
										goOn = true;
									} else if (set == null) {
										set = new HashSet<Integer>();
										set.add(validArr[j]);
										domainMap.put(j, set);
										goOn = true;
									}
								}
								if (!goOn) {
									System.out.println("================== break !!! ===================");
									break OUT;
								}
							}
						}

					}
				}
				if (domainMap.size() != 0) {
					// need set marked nodes to pattern edges
					HashMap<Integer, PatternEdges> pesMap = qry.getPEMap();
					for (Entry<Integer, PatternEdges> pesEntry : pesMap.entrySet()) {
						PatternEdges pe = pesEntry.getValue();
						int nodeA = pe.getIndexA();
						int nodeB = pe.getIndexB();
						HashSet<Integer> domA = domainMap.get(nodeA);
						HashSet<Integer> domB = domainMap.get(nodeB);
						pe.setMarkNodes(nodeA, domA);
						pe.setMarkNodes(nodeB, domB);
					}
				}

			}

		}
		return joinedTable;
	}
	
	/*
	 * lemma 3
	 * 
	 * @param: pe -> pattern edge table
	 */
	public ArrayList<int[]> tupleReduceEdge(PatternEdges pe) {
		ArrayList<int[]> refinedNodePairs = new ArrayList<int[]>();
		ArrayList<int[]> nodePairs = pe.getNodePairs();
		int MNInodeA = pe.aSet().size();
		int MNInodeB = pe.bSet().size();
		HashSet<Integer> addA = new HashSet<Integer>();
		HashSet<Integer> addB = new HashSet<Integer>();

		if (pe.getMinNode() == pe.getIndexA()) {// MNI(A) < MNI(B)
			for (int i = 0; i < nodePairs.size(); i++) {
				int[] arr = nodePairs.get(i);
//				if(!addA.contains(arr[0])) {
				if (!addA.contains(arr[0]) && !pe.getValidASet().contains(arr[0])) {
					refinedNodePairs.add(arr);
					addA.add(arr[0]);
					addB.add(arr[1]);
					if (addA.size() == MNInodeA)
						break;
				}
			}
			int addAsize = addA.size();
			if (addB.size() < addAsize) {// need add from column B
				for (int i = 0; i < nodePairs.size(); i++) {
					int[] arr = nodePairs.get(i);
//					if(!addB.contains(arr[1])){
					if (!addB.contains(arr[1]) && !pe.getValidBSet().contains(arr[1])) {
						refinedNodePairs.add(arr);
						addB.add(arr[1]);
						if (addB.size() == addAsize)
							break;
					}
				}
			}
		} else if (pe.getMinNode() == pe.getIndexB()) {// MNI(A) > MNI(B)
			for (int i = 0; i < nodePairs.size(); i++) {
				int[] arr = nodePairs.get(i);
//				if(!addB.contains(arr[1])) {
				if (!addB.contains(arr[1]) && !pe.getValidBSet().contains(arr[1])) {
					refinedNodePairs.add(arr);
					addA.add(arr[0]);
					addB.add(arr[1]);
					if (addB.size() == MNInodeB)
						break;
				}
			}
			int addBsize = addB.size();
			if (addA.size() < addBsize) {// need add from column A
				for (int i = 0; i < nodePairs.size(); i++) {
					int[] arr = nodePairs.get(i);
//					if(!addA.contains(arr[0])){
					if (!addA.contains(arr[0]) && !pe.getValidASet().contains(arr[0])) {
						refinedNodePairs.add(arr);
						addA.add(arr[0]);
						if (addA.size() == addBsize)
							break;
					}
				}
			}
		}
		return refinedNodePairs;
	}

	/*
	 * create instance table for this pattern edge by indexes: reachableNodes
	 * 
	 */
	public ArrayList<int[]> getNodepairs(PatternEdges pe, HashMap<Integer, myNode> nodeAmap, double edgeLabel,
			HashMap<Integer, myNode> nodeBmap) {
		ArrayList<int[]> reNodePairsList = new ArrayList<int[]>();
		int labelB = 0;
		for (Entry<Integer, myNode> entry : nodeBmap.entrySet()) {
			labelB = entry.getValue().getLabel();
			break;
		}
		for (Entry<Integer, myNode> entryA : nodeAmap.entrySet()) {
			int nodeAIdx = entryA.getKey();
			myNode nodeA = entryA.getValue();
			// represented by Label~<nodeID,edge_label>, represents the out going nodes
			HashMap<Integer, ArrayList<MyPair<Integer, Double>>> nodeAout = nodeA.getReachableWithNodes();// indexes
			if (nodeAout == null)
				continue;
			ArrayList<MyPair<Integer, Double>> nodeAoutList = nodeAout.get(labelB);
			if (nodeAoutList == null)
				continue;
			Iterator it = nodeAoutList.iterator();
			while (it.hasNext()) {
				MyPair<Integer, Double> pair = (MyPair<Integer, Double>) it.next();
				int nodeBIdx = pair.getA();
				double curLabel = pair.getB();
				if (nodeBmap.containsKey(nodeBIdx) && curLabel == edgeLabel) {// can connect to nodeA
					// A->B
					int[] nPair = new int[] { nodeAIdx, nodeBIdx };
					pe.aSet().add(nodeAIdx);
					pe.bSet().add(nodeBIdx);
					reNodePairsList.add(nPair);
				}
			}
		}
		return reNodePairsList;
	}

	private IntFrequency tighterUpperBound(PatternEdges curEdge) {
		// get all the adjacent edges
		HashMap<Integer, PatternEdges> adjEdges = adjacentEdges(curEdge);
		for (Entry<Integer, PatternEdges> entry : adjEdges.entrySet()) {
			PatternEdges adjEdge = entry.getValue();
			int joinNode = getJoinNode(curEdge, adjEdge);
			HashSet<Integer> curValids = getJoinNodes(curEdge, joinNode);
			HashSet<Integer> adjValids = getJoinNodes(adjEdge, joinNode);

			int curValidSize = curValids.size();
			int adjValidSize = adjValids.size();

			curValids.retainAll(adjValids);
			// intersection is an upper bound
			if (curValids.size() < currentMinFreq) {
				// stop the remaining steps
				UB = new IntFrequency(curValids.size());
				return UB;
			}

			if (curValidSize != curValids.size()) {
				curEdge.pruneFailJoinNodes(joinNode, curValids);
			}
			int UBcur = curEdge.upperBound();
			if (UBcur < currentMinFreq) {
				// stop the remaining steps
				UB = new IntFrequency(UBcur);
				return UB;
			}
			if (adjValidSize != curValids.size()) {
				adjEdge.pruneFailJoinNodes(joinNode, curValids);
			}
			int UBadj = adjEdge.upperBound();
			if (UBadj < currentMinFreq) {
				// stop the remaining steps
				UB = new IntFrequency(UBadj);
				return UB;
			}

			// remove from edges
			esMap.remove(adjEdge.getEdgeIdx());
			if (UB.compareTo(new IntFrequency(0)) > 0) {
				UB = tighterUpperBound(adjEdge);
			} else {
				return UB;
			}
		}
		return UB;
	}

	public void pruneFailJoinNodes(PatternEdges edge, int joinNode, HashSet<Integer> inter) {
		ArrayList<int[]> list = edge.getNodePairs();
		// prune nodes in curEdge
		if (joinNode == edge.getIndexA()) {
			Iterator it = list.iterator();
			while (it.hasNext()) {
				int[] arr = (int[]) it.next();
				if (!inter.contains(arr[0])) {
					it.remove();
				}
			}
		} else if (joinNode == edge.getIndexB()) {
			Iterator it = list.iterator();
			while (it.hasNext()) {
				int[] arr = (int[]) it.next();
				if (!inter.contains(arr[1])) {
					it.remove();
				}
			}
		}

	}

	public HashSet<Integer> getJoinNodes(PatternEdges edge, int joinNode) {
		ArrayList<int[]> list = edge.getNodePairs();
		HashSet<Integer> valids = new HashSet<Integer>();
		if (joinNode == edge.getIndexA()) {
			// nodeA join: first column in node pairs
			Iterator it = list.iterator();
			while (it.hasNext()) {
				int[] arr = (int[]) it.next();
				valids.add(arr[0]);
			}
		} else if (joinNode == edge.getIndexB()) {
			// nodeB join
			Iterator it = list.iterator();
			while (it.hasNext()) {
				int[] arr = (int[]) it.next();
				valids.add(arr[1]);
			}
		}
		return valids;
	}

	// get the adjacent edges of curEdge in pattern
	public HashMap<Integer, PatternEdges> adjacentEdges(PatternEdges curEdge) {
		HashMap<Integer, PatternEdges> adjEdgeTables = new HashMap<Integer, PatternEdges>();
		int nodeA = curEdge.getIndexA();
		int nodeB = curEdge.getIndexB();
		IntIterator nodeANeighbors = qryGraph.getEdgeIndices(nodeA);
		while (nodeANeighbors.hasNext()) {
			int edgeN = nodeANeighbors.next();
			PatternEdges edgeTable = esMap.get(edgeN);
			if (esMap.containsKey(edgeN) && edgeTable.getEdgeIdx() != curEdge.getEdgeIdx()) {
				adjEdgeTables.put(edgeN, edgeTable);
			}
		}
		IntIterator nodeBNeighbors = qryGraph.getEdgeIndices(nodeB);
		while (nodeBNeighbors.hasNext()) {
			int edgeN = nodeBNeighbors.next();
			PatternEdges edgeTable = esMap.get(edgeN);
			if (esMap.containsKey(edgeN) && edgeTable.getEdgeIdx() != curEdge.getEdgeIdx()) {
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

	/*
	 * @param: pattern edge1 and edge2
	 * 
	 * @return: join node of edge1 and edge2
	 */
	public int getJoinNode2(PatternEdges pe1, PatternEdges pe2) {
		if (pe1.getIndexA() == pe2.getIndexA() || pe1.getIndexA() == pe2.getIndexB()) {
			return pe1.getIndexA();
		} else if (pe1.getIndexB() == pe2.getIndexA() || pe1.getIndexB() == pe2.getIndexB()) {
			return pe1.getIndexB();
		}
		return -1;
	}
	
	
	
}
