package joinAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.HPListGraph;
import utilities.Settings;

public class ComplexPattern {
	private ArrayList vertexList;// store vertex
	private int[][] edges;// adjacent matrix to store edges
	private int numOfEdges;// # of edges
	private ArrayList<ArrayList<Integer>> allSCList;
	private HashMap<Integer, int[]> degreeMap;
	private HPListGraph<Integer, Double> qryGraph;
	
	public ComplexPattern(int n) {
		edges = new int[n][n];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				edges[i][j] = -1;
			}
		}

		vertexList = new ArrayList(n);
		numOfEdges = 0;
	}

	public int getNumOfVertex() {
		return vertexList.size();
	}

	public int getNumOfEdges() {
		return numOfEdges;
	}

	public Object getValueByIndex(int i) {
		return vertexList.get(i);
	}

	public int getWeight(int v1, int v2) {
		return edges[v1][v2];
	}

	public void insertVertex(Object vertex) {
		vertexList.add(vertexList.size(), vertex);
	}

	public void insertEdge(int v1, int v2, int weight) {
		edges[v1][v2] = weight;
		edges[v2][v1] = weight;
		numOfEdges++;
	}

	public void deleteEdge(int v1, int v2) {
		edges[v1][v2] = -1;
		edges[v2][v1] = -1;
		numOfEdges--;
	}

	// get the first neighbor
	public int getFirstNeighborUndirect(int index, ArrayList<Integer> scList) {
		for (int j = 0; j < vertexList.size(); j++) {
			if (edges[index][j] > -1 && !scList.contains(j) && 
					(degreeMap.get(j)[0] != 0 || degreeMap.get(j)[1] != 0)) {
				return j;
			}
		}
		return -1;
	}

	// get the next neighbor by the previous node
	public int getNextNeighborUndirect(int v1, int v2, ArrayList<Integer> scList) {
		for (int j = v2 + 1; j < vertexList.size(); j++) {
			if (edges[v1][j] > -1 && !scList.contains(j)) {
				return j;
			}
		}
		return -1;
	}
	
	// get the first neighbor
		public int getFirstNeighbor(int index) {
			for (int j = 0; j < vertexList.size(); j++) {
				if (edges[index][j] > -1) {
					return j;
				}
			}
			return -1;
		}

		// get the next neighbor by the previous node
		public int getNextNeighbor(int v1, int v2) {
			for (int j = v2 + 1; j < vertexList.size(); j++) {
				if (edges[v1][j] > -1) {
					return j;
				}
			}
			return -1;
		}

	private ArrayList<Integer> depthFirstSearchUndirect(int i, ArrayList<Integer> scList,
			 ArrayList<ArrayList<Integer>> allList) {
		//get neighbor of node i
		int neighbor = getFirstNeighborUndirect(i, scList);
		if(Settings.print){
			System.out.println("i:" + getValueByIndex(i));
			System.out.println("neighbor:"+neighbor);
		}
		
		// update in- and out- degree
		if (neighbor != -1) {// has neighbor
			scList.add(neighbor);
			int edgeIdx = qryGraph.getEdge(i, neighbor);
			if (edgeIdx == -1) {
				edgeIdx = qryGraph.getEdge(neighbor, i);
			}
			deleteEdge(i,neighbor);
			int direction = qryGraph.getDirection(edgeIdx, neighbor);
			if (direction == -1) {// i -> neighbor
				degreeMap.get(i)[1]--;
				degreeMap.get(neighbor)[0]--;// in-
			} else if (direction != -1) {// neighbor -> i
				degreeMap.get(neighbor)[1]--;// out-
				degreeMap.get(i)[0]--;
			}
			if ((degreeMap.get(neighbor)[0] > 0 || degreeMap.get(neighbor)[1] > 0)) {
				scList = depthFirstSearchUndirect(neighbor, scList, allList);
			}
		}else if (neighbor == -1) {//does not have neighbor
			return scList;
		}
		if (scList != null && scList.size() != 0) {
			if(Settings.print){
				System.out.println("scList:" + scList);
			}
			allList.add(scList);
			scList = new ArrayList<Integer>();
		}
		return scList;
	}

	public void printDegreeMap(HashMap<Integer, int[]> degreeMap) {
		System.out.println("print degree --------");
		for (Entry<Integer, int[]> entry : degreeMap.entrySet()) {
			int node = entry.getKey();
			int[] degree = entry.getValue();
			System.out.println(node + " in:" + degree[0] + " out:" + degree[1]);
		}
	}

	public void depthFirstSearchUndirect(HashMap<Integer, int[]> degreeMap, HPListGraph<Integer, Double> qryGraph) {
		this.degreeMap = degreeMap;
		this.qryGraph = qryGraph;
		ArrayList<Integer> scList = new ArrayList<Integer>();//single chain list
//		ArrayList<Integer> edgeList = new ArrayList<Integer>();
		allSCList = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> nodesList = new ArrayList<Integer>(vertexList);//all nodes in the pattern
		while(nodesList != null && nodesList.size() != 0){
			boolean startNode = false;
			Iterator it  = nodesList.iterator();
			while(it.hasNext()){
				int node = (int) it.next();
				int[] degree  = degreeMap.get(node);
				if (degree[0] == 0 && degree[1] != 0) {//start from node that in-degree is 0 and out-degree is not 0
					if (Settings.print) {
						printDegreeMap(degreeMap);
					}
					scList.add(node);
					depthFirstSearchUndirect(node, scList, allSCList);
					if (Settings.print) {
						printDegreeMap(degreeMap);
					}
					scList = new ArrayList<Integer>();
					startNode = true;
				}
				if(degree[0]==0 && degree[1]==0)//in degree and out degree 
					it.remove();
			}
			// did not contain the node in degree is 0, out is not 0
//			if (!startNode) {
//				Iterator its  = nodesList.iterator();
//				while (its.hasNext()) {
//					int node = (int) its.next();
//					int[] degree = degreeMap.get(node);
//					if (Settings.print) {
//						printDegreeMap(degreeMap);
//					}
//					scList.add(node);
//					depthFirstSearchUndirect(node, scList, allSCList);
//					if (Settings.print) {
//						printDegreeMap(degreeMap);
//					}
//					scList = new ArrayList<Integer>();
//					startNode = true;
//					if (degree[0] == 0 && degree[1] == 0)
//						its.remove();
//				}
//			}
			
		}
		
	}
	
	public void depthFirstSearch(HashMap<Integer, int[]> degreeMap) {
		this.degreeMap = degreeMap;
		boolean[] isVisited = new boolean[getNumOfVertex()];
		for (int i = 0; i < getNumOfVertex(); i++) {
			isVisited[i] = false;
		}
		ArrayList<Integer> scList = new ArrayList<Integer>();
		allSCList = new ArrayList<ArrayList<Integer>>();
		boolean firstNode = true;
		ArrayList<Integer> nodesList = new ArrayList<Integer>(vertexList);
		while(nodesList != null && nodesList.size() != 0){
			Iterator it  = nodesList.iterator();
			while(it.hasNext()){
				int node = (int) it.next();
				int[] degree  = degreeMap.get(node);
				if(degree[0]==0 && degree[1]!=0){
					if (!isVisited[node]) {
						if(Settings.print){
							printDegreeMap(degreeMap);
						}
						depthFirstSearch(isVisited, node, scList, allSCList, firstNode);
						if(Settings.print){
							printDegreeMap(degreeMap);
						}
						scList = new ArrayList<Integer>();
						firstNode = true;
					}
				}
				if(degree[0]==0 && degree[1]==0)
					it.remove();
			}
			
		}
	}
	
	private ArrayList<Integer> depthFirstSearch(boolean[] isVisited, int i, ArrayList<Integer> scList,
			ArrayList<ArrayList<Integer>> allList, boolean firstNode) {
		if(Settings.print){
			System.out.println("i:" + getValueByIndex(i));
		}
		isVisited[i] = true;
		scList.add(i);
		// update in- and out- degree
		if (firstNode) {
			degreeMap.get(i)[1]--;
		} else {
			degreeMap.get(i)[0]--;// in-
			degreeMap.get(i)[1]--;// out-
		}
		firstNode = false;
		int w = getFirstNeighbor(i);
		if (w == -1)// no neighbor
			degreeMap.get(i)[1]++;
		while (w != -1) {
			if (!isVisited[w] || (degreeMap.get(w)[0] > 0 || degreeMap.get(w)[1] > 0)) {
				scList = depthFirstSearch(isVisited, w, scList, allList, firstNode);
			}else{
				break;
			}
			w = getNextNeighbor(i, w);
			if (w != -1) {
				// add i with w now
				scList.add(i);
				// degreeMap.get(i)[0] --;
				degreeMap.get(i)[1]--;
			}
		}
		if (scList != null && scList.size() != 0) {
			Iterator it = allList.iterator();
			while(it.hasNext()){
				ArrayList<Integer> tmpList = (ArrayList<Integer>) it.next();
				if(scList.containsAll(tmpList)){
					//remove the previous single chain
					for(int t=0;t<tmpList.size();t++){
						int node = tmpList.get(t);
						if(t==0){
							degreeMap.get(node)[1]++;
						}else if(t==tmpList.size()-1){
							degreeMap.get(node)[0]++;
						}else{
							degreeMap.get(node)[0]++;
							degreeMap.get(node)[1]++;
						}
					}
					it.remove();
				}
			}
			if(Settings.print){
				System.out.println("scList:" + scList);
			}
			allList.add(scList);
			scList = new ArrayList<Integer>();
		}
		return scList;
	}
	
	public ArrayList<ArrayList<Integer>> getSCList(){
		return allSCList;
	}

}
