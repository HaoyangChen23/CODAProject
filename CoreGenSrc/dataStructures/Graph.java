/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package dataStructures;


import java.awt.Point;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

//import Temp.SubsetReference;
import search.OneEdgeExtension;
import topKresults.MaxHeap;
//import utilities.CombinationGenerator;
import utilities.Settings;


public class Graph extends ArrayList<Vertex>
{
	private static final long serialVersionUID = 1L;
	int edge_size = 0;
	boolean directed = false;

	public final static int NO_EDGE = 0;
	private HPListGraph<Integer, Double> m_matrix;
	private int nodeCount=0;
	private ArrayList<myNode> nodes;
	private HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel;
	private HashMap<Integer, HashMap<Integer,myNode>> nodesByLabel;
	private ArrayList<Integer> sortedFreqLabels; //sorted by frequency !!! Descending......
	
	private HashMap<Integer, Integer> nIndexLabelMap;
	private HashMap<Integer, ArrayList<Integer>> nodeIndexMap;
	private ArrayList<Double> freqEdgeLabels;

	private ArrayList<Point> sortedFreqLabelsWithFreq;
	private HashMap<Double, Integer> edgeLabelsWithFreq;
	private HashMap<String, Integer> patternEdgeMap;//for pattern extension: <edge,frequency>
	
	public int[][] getMaxDegreeMatrix(){
		//this matrix stores the max degree of each node label
		int maxDegreeMatrix[][] = new int[nodeIndexMap.size()][2];
		Set<Entry<Integer, ArrayList<Integer>>> entrySet = nodeIndexMap.entrySet();
	    Iterator<Entry<Integer, ArrayList<Integer>>> iter = entrySet.iterator();
	    int index = 0;//node label index, so the data should pre-processing as node label by increasing order one by one
	    while (iter.hasNext()) {
	    	 Entry<Integer, ArrayList<Integer>> entry = iter.next();
	    	 List<Integer> nodeList= entry.getValue();
	    	 int maxInDegree = 0;
	    	 int maxOutDegree = 0;
	    	 for(int i=0;i<nodeList.size();i++) {
	    		 int inDegree = m_matrix.getInDegree(nodeList.get(i));
	    		 int outDegree = m_matrix.getOutDegree(nodeList.get(i));
	    		 if(maxInDegree <inDegree) {
	    			 maxInDegree = inDegree;//in degree stored in first column
	    			 maxDegreeMatrix[index][0] = maxInDegree;
	    		 }
	    		 if(maxOutDegree <outDegree) {
	    			 maxOutDegree = outDegree;//out degree stored in second column
	    			 maxDegreeMatrix[index][1] = maxOutDegree;
	    		 }
	    	 }
	    	 index ++;
	    }
		return maxDegreeMatrix;
	}
	
	private int m_id;

	public Graph() {
	}


	public int getEdgeSize() {
		return edge_size;
	}
	
	public Graph(int ID) 
	{
		sortedFreqLabels= new ArrayList<Integer>();
		sortedFreqLabelsWithFreq = new ArrayList<Point>();
		nIndexLabelMap = new HashMap<Integer, Integer>();
		nodeIndexMap = new HashMap<Integer, ArrayList<Integer>>();
		m_matrix= new HPListGraph<Integer, Double>();
		m_id=ID;
		nodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();

		freqNodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		nodes= new ArrayList<myNode>();
		nodePairMap = new HashMap<String, ArrayList<int[]>>();
		edgeLabelsWithFreq = new HashMap<Double, Integer>();
		freqEdgeLabels = new ArrayList<Double>();
		patternEdgeMap = new HashMap<String, Integer>();
		if(StaticData.hashedEdges!=null)
		{
			StaticData.hashedEdges = null;
			System.out.println(StaticData.hashedEdges.hashCode());//throw exception if more than one graph was created
		}
		StaticData.hashedEdges = new HashMap<String, HashMap<Integer, Integer>[]>();
	}
	
	public ArrayList<Integer> getSortedFreqLabels() {
		return sortedFreqLabels;
	}
	
	public ArrayList<Double> getFreqEdgeLabels() {
		return this.freqEdgeLabels;
	}

	public HashMap<Integer, HashMap<Integer,myNode>> getFreqNodesByLabel()
	{
		return freqNodesByLabel;
	}
	
	public ArrayList<myNode> getNodes(){
		return nodes;
	}
	public HashMap<String, Integer> getPatternEdgeSet(){
		return patternEdgeMap;
	}
	
	public <NodeType, EdgeType> void loadFromFile_Ehab(String fileName) throws Exception
	{		
		final BufferedReader rows = new BufferedReader(new FileReader(new File(fileName)));
		
		// read graph from rows
		// nodes
		int counter = 0;
		int numberOfNodes=0;
		String line;
		String tempLine;
		rows.readLine();
		
		while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
			ArrayList<Integer> nodeIndexList = new ArrayList<Integer>();
			final String[] parts = line.split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			final int label = Integer.parseInt(parts[2]);
			if (index != counter) {
				throw new ParseException("The node list is not sorted", counter);
			}
			nIndexLabelMap.put(index, label);
			//create index for all nodes by node label
			if(!nodeIndexMap.isEmpty() || nodeIndexMap != null) {
				if(nodeIndexMap.containsKey(label)) {
					nodeIndexMap.get(label).add(index);
				}else if(!nodeIndexMap.containsKey(label)) {
					nodeIndexList.add(index);
					nodeIndexMap.put(label, nodeIndexList);
				}
			}else {
				nodeIndexMap.put(label, nodeIndexList);
			}
			
			
			addNode(label);
			myNode n = new myNode(numberOfNodes, label);
			nodes.add(n);
			HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
			if(tmp==null)
			{
				tmp = new HashMap<Integer,myNode>();
				nodesByLabel.put(label, tmp);
			}

			tmp.put(n.getID(), n);
			numberOfNodes++;
			counter++;
		}
		
		nodeCount=numberOfNodes;
		tempLine = line;
		
		
		// edges
		//use the first edge line
		if(tempLine.charAt(0)=='e')
			line = tempLine;
		else
			line = rows.readLine();
		
		if(line!=null)
		{
			do
			{
				final String[] parts = line.split("\\s+");
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				final double label = Double.parseDouble(parts[3]);
				addEdge(index1, index2, label);
//				if (Settings.HMT) {
					//for pattern edges
					int labelA = nIndexLabelMap.get(index1);
					int labelB = nIndexLabelMap.get(index2);
					String pattern = labelA+"_"+(int)label+"+"+labelB;
					//create node pairs for join operation
					createNodePairs(pattern, index1, index2);
//				}
			} while((line = rows.readLine()) !=null && (line.charAt(0) == 'e'));
		}
//		if (Settings.HMT) {
			// create pattern edge map for meta index pattern extension
			createPatternEdgeMap(nodePairMap);
//		}
		nodeIndexMap = sortByKeyIncreasing(nodeIndexMap);
		
//		if(!Settings.HMT){
			edgeLabelsWithFreq = (HashMap<Double, Integer>) sortByValueDescending(edgeLabelsWithFreq);
			//prune infrequent edge labels
			for (Iterator<  java.util.Map.Entry< Double,Integer> >  it= this.edgeLabelsWithFreq.entrySet().iterator(); it.hasNext();) 
			{
				java.util.Map.Entry< Double,Integer > ar =  it.next();			
				if(ar.getValue().doubleValue()>=1)
				{
					this.freqEdgeLabels.add(ar.getKey());
				}
			}
//		}
		
		//now prune the infrequent nodes
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();	
			if(ar.getValue().size()>=1)//size is not the MNI frequency, so can't replace as kthFreq
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());//influent the final time!!!!!!!!!!
			}
		}
		
		
		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) 
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
		}
		
		rows.close();		
	}
	
	private void createPatternEdgeMap(HashMap<String, ArrayList<int[]>> nodePairMap) {
		for(Entry<String, ArrayList<int[]>> entry: nodePairMap.entrySet()){
			int freq = 0;
			String pattern = entry.getKey();
			ArrayList<int[]> list = nodePairMap.get(pattern);
			HashSet<Integer> nodeASet = new HashSet<Integer>();
			HashSet<Integer> nodeBSet = new HashSet<Integer>();
			Iterator it = list.iterator();
			while(it.hasNext()){
				int[] arr = (int[]) it.next();
				nodeASet.add(arr[0]);
				nodeBSet.add(arr[1]);
			}
			freq = nodeASet.size();
			int tmp = nodeBSet.size();
			if(freq > tmp)
				freq = tmp;
			patternEdgeMap.put(pattern, freq);
		}
	}

	private HashMap<String, ArrayList<int[]>> nodePairMap;
	
	public HashMap<String, ArrayList<int[]>> getNodePairs(){
		return nodePairMap;
	}
	
	private void createNodePairs(String pattern, int index1, int index2) {
//		System.out.println("pattern:"+pattern);
		ArrayList<int[]> list = nodePairMap.get(pattern);
		if(list == null){
			list = new ArrayList<int[]>();
			int[] array = new int[]{index1,index2};
			list.add(array);
		}else{
			int[] array = new int[]{index1,index2};
			list.add(array);
		}
		nodePairMap.put(pattern, list);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				int compare = (o1.getValue()).compareTo(o2.getValue());
				return -compare;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public static HashMap<Integer, ArrayList<Integer>> sortByKeyIncreasing(HashMap<Integer, ArrayList<Integer>> nodeIndexMap) {
		//sort by increasing
		 List<Map.Entry<Integer, ArrayList<Integer>>> list = new ArrayList<Map.Entry<Integer, ArrayList<Integer>>>(nodeIndexMap.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry<Integer, ArrayList<Integer>>>() {
	            @Override
				public int compare(Map.Entry<Integer, ArrayList<Integer>> o1, Map.Entry<Integer, ArrayList<Integer>> o2) {
	                return o1.getKey().compareTo(o2.getKey());
	            }
	        });
			return nodeIndexMap;
	}
	
	public void printFreqNodes()
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			System.out.println("Freq Label: "+ar.getKey()+" with size: "+ar.getValue().size());
		}
	}
	
	//1 hop distance for the shortest paths
	public void setShortestPaths_1hop()
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
//			int counter=0;
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
//				System.out.println(counter++);
				node.setReachableNodes_1hop(this, freqNodesByLabel);
			}
		}
	}
	
	public myNode getNode(int ID)
	{
		return nodes.get(ID);
	}
	
	public HPListGraph<Integer, Double> getListGraph()
	{
		return m_matrix;
	}
	public int getID() {
		return m_id;
	}
	
	public int getDegree(int node) {

		return m_matrix.getDegree(node);
	}
		
	public int getNumberOfNodes()
	{
		return nodeCount;
	}
	 
	public int addNode(int nodeLabel) {
		return m_matrix.addNodeIndex(nodeLabel);
	}
	public int addEdge(int nodeA, int nodeB, double edgeLabel) 
	{
		Integer I = edgeLabelsWithFreq.get(edgeLabel); 
		if(I==null)
			edgeLabelsWithFreq.put(edgeLabel, 1);
		else
			edgeLabelsWithFreq.put(edgeLabel, I.intValue()+1);
		
		//add edge frequency
//		int labelA = nodes.get(nodeA).getLabel();
//		int labelB = nodes.get(nodeB).getLabel();
//		
//		String hn;
//		
//		hn = labelA+"_"+edgeLabel+"_"+labelB;
//		
//		HashMap<Integer,Integer>[] hm = StaticData.hashedEdges.get(hn); 
//		if(hm==null)
//		{
//			hm = new HashMap[2];
//			hm[0] = new HashMap();
//			hm[1] = new HashMap();
//					
//			StaticData.hashedEdges.put(hn, hm);
//		}
//		else
//		{}
//		hm[0].put(nodeA, nodeA);
//		hm[1].put(nodeB, nodeB);
		
		return m_matrix.addEdgeIndex(nodeA, nodeB, edgeLabel, 1);
	}


	void buildEdge() {
		String buf;
		NavigableMap<String, Integer> tmp = new TreeMap<>();

		int id = 0;
		for (int from = 0; from < size(); ++from) {
			for (Edge it : this.get(from).edge) {
				if (directed || from <= it.to)
					buf = from + " " + it.to + " " + it.eLabel;
				else
					buf = it.to + " " + from + " " + it.eLabel;

				// Assign unique id's for the edges.
				if (tmp.get(buf) == null) {
					it.id = id;
					tmp.put(buf, id);
					++id;
				} else {
					it.id = tmp.get(buf);
				}
			}
		}

		edge_size = id;
	}

	public BufferedReader read(BufferedReader is) throws IOException {
		ArrayList<String> result = new ArrayList<>();
		String line;

		clear();

		while ((line = is.readLine()) != null) {
			result.clear();
			String[] splitRead = line.split(" ");
			Collections.addAll(result, splitRead);

			if (!result.isEmpty()) {
				if (result.get(0).equals("t")) {
					if (!this.isEmpty()) { // use as delimiter
						break;
					}
				} else if (result.get(0).equals("v") && result.size() >= 3) {
					// int id = Integer.parseInt(result.get(1));
					Vertex vex = new Vertex();
					vex.label = Integer.parseInt(result.get(2));
					this.add(vex);
				} else if (result.get(0).equals("e") && result.size() >= 4) {
					int from = Integer.parseInt(result.get(1));
					int to = Integer.parseInt(result.get(2));
					int eLabel = Integer.parseInt(result.get(3));

					if (this.size() <= from || this.size() <= to) {
						System.out.println("Format Error:  define vertex lists before edges");
						return null;
					}

					this.get(from).push(from, to, eLabel);

					if (!directed) {
						this.get(to).push(to, from, eLabel);
					}
				}
			}
		}

		buildEdge();

		return is;
	}

	public void write(FileWriter os) throws IOException {
		String buf;
		// Sort the result of edges.
		NavigableSet<String> tmp = new TreeSet<>((o1, o2) -> {
			String[] split1 = o1.split(" ");
			String[] split2 = o2.split(" ");
			if (Integer.parseInt(split1[0]) == Integer.parseInt(split2[0])) {
				if (Integer.parseInt(split1[1]) == Integer.parseInt(split2[1])) {
					return Integer.parseInt(split1[2]) - Integer.parseInt(split2[2]);
				} else {
					return Integer.parseInt(split1[1]) - Integer.parseInt(split2[1]);
				}
			} else {
				return Integer.parseInt(split1[0]) - Integer.parseInt(split2[0]);
			}
		});

		for (int from = 0; from < size(); ++from) {
			os.write("v " + from + " " + this.get(from).label + System.getProperty("line.separator"));

			for (Edge it : this.get(from).edge) {
				if (directed || from <= it.to) {
					buf = from + " " + it.to + " " + it.eLabel;
				} else {
					buf = it.to + " " + from + " " + it.eLabel;
				}
				tmp.add(buf);
			}
		}

		for (String it : tmp) {
			os.write("e " + it + System.getProperty("line.separator"));
		}

		os.flush();
	}

	public void check() {
		/*
		 * Check all indices
		 */
		for (int from = 0; from < size(); ++from) {
			System.out.println(
					"check vertex " + from + ", label " + this.get(from).label + System.getProperty("line.separator"));

			for (Edge it : this.get(from).edge) {
				System.out.println("   check edge from " + it.from + " to " + it.to + ", label " + it.eLabel
						+ System.getProperty("line.separator"));
				assert (it.from >= 0 && it.from < size());
				assert (it.to >= 0 && it.to < size());
			}
		}
	}

	void resize(int size) {
		while (this.size() < size) {
			this.add(new Vertex());
		}
	}
}





