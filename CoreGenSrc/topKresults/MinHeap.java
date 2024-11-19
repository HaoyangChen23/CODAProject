/**  
 * Project Name:frequentPatternsMiningV1.4  
 * File Name:MinHeap.java  
 * Package Name:topKresults  
 * Date:Mar 28, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/

package topKresults;
/**  
 * ClassName:MinHeap   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Mar 28, 2019   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dataStructures.Frequency;
import dataStructures.IntFrequency;
import search.SearchLatticeNode;
import utilities.Settings;

public class MinHeap<NodeType, EdgeType> {
//	static int maxSize = 100;// top-k
	static int maxSize;
	private ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> minHeapList;
	private IntFrequency minFrequency;
	
	public MinHeap() {
		minHeapList = new ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>>();
		minFrequency = new IntFrequency(0);
		this.maxSize = Settings.k;
	}
	
	public int getSize(){
		return minHeapList.size();
	}
	
	public IntFrequency getMinFrequency() {
		int size = minHeapList.size();
		if (size < maxSize) {
			return minFrequency;
		} else {
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry0 : minHeapList.get(0).entrySet()) {
				this.minFrequency = (IntFrequency) entry0.getValue();
			}
			return minFrequency;
		}
	}
	
	public ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> getTopKList(){
		
		return minHeapList;
	}

	/*
	 * 
	 * create heap according to the frequency of each pattern, the root of this heap
	 * is the k-th frequency
	 * true: the root has changed, false: root has not changed
	 */
	public  Boolean addTopk(
			SearchLatticeNode<NodeType, EdgeType> pattern, Frequency frequency) {
		int size = minHeapList.size();
		int maxSize = Settings.k;
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> midMap = new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
		if (size < maxSize) {
			midMap.put(pattern, frequency);// add new value from bottom
			minHeapList.add(midMap);
			heapUp(size,minHeapList);
//			heapDown(0,minHeapList);
			return true;
		} else {
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> rootMap = minHeapList.get(0);
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry : rootMap.entrySet()) {
				IntFrequency root = (IntFrequency) entry.getValue();
				if (frequency.compareTo(root) > 0) {// if the frequency is larger than root
					minHeapList.remove(0);
					HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> addMap = new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
					addMap.put(pattern, frequency);
					minHeapList.add(0, addMap); // replace the root value
					heapDown(0,minHeapList);
					for(Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry0 : minHeapList.get(0).entrySet()) {
						this.minFrequency = (IntFrequency) entry0.getValue();
					}
					return true;
				}else if(frequency.compareTo(root)<=0) {// if the frequency no larger than root
					return false;
				}
			}
		}
		return false;
	}

	// adjust sorting from i up to the heap
	private static <NodeType, EdgeType> void heapUp(int pCount,List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> dataList) {
		if (pCount == 0) {// is the root of heap
			return;
		}
		OUT:
		while (pCount > 0) {// not the root of heap
			int parent = (pCount - 1) / 2;
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> mMap = dataList.get(pCount);
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> pMap = dataList.get(parent);
			// the frequency of father
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry : mMap.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry0 : pMap.entrySet()) {
					if ((entry.getValue()).compareTo((entry0.getValue())) <0 ) {
						if(pCount >0) {
							
							swap(parent, pCount, dataList);// swap the two map //parent = pCount = 0
							pCount = parent;
						}
					}else if((entry.getValue()) .compareTo (entry0.getValue()) >=0){
						break OUT;
					}
				}
			}
		}
	}

	// adjust sorting from root of the heap
	private static <NodeType, EdgeType> void heapDown(int i,List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> dataList) {
		int left = 2 * (i + 1) - 1;
		int right = 2 * (i + 1);
		int smallest = i;// the smallest index of left right and parent
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> smallestMap = dataList.get(smallest);
		
		//if exist left and right child,we choose the small child
		if(left < dataList.size() && right < dataList.size()){
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> leftMap = dataList.get(left);
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> rightMap = dataList.get(right);
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entryl : leftMap.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entryr : rightMap.entrySet()) {
					for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entrys : smallestMap.entrySet()) {
					if(entryl.getValue().compareTo( (entryr.getValue())) <0) {//left smaller than right
						if(entrys.getValue() .compareTo( (entryl.getValue())) <=0) {
							return;
						}else if(entrys.getValue().compareTo( (entryl.getValue())) >0){//choose left child
							smallest = left; 
						}
					}else if(entryl.getValue() .compareTo (entryr.getValue())>=0){//right smaller than left
						if(entrys.getValue() .compareTo (entryr.getValue()) <= 0) {
							return;
						}else if(entrys.getValue() .compareTo (entryr.getValue()) >0){//choose right child
							smallest = right;
						}
					}
					}
				}
			}
			
		}
		//if only has left child 
		if(left < dataList.size() && right >= dataList.size()) {
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> leftMap = dataList.get(left);
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry : leftMap.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry0 : smallestMap.entrySet()) {
					if ((entry.getValue()) .compareTo (entry0.getValue()) < 0) {
						smallest = left;
//						return;
					}
				}
			}
		}
		
			// if father is smaller than left/right child, return directly
			if (smallest == i) {
				return;
			}
			// swap father with the smaller one
			swap(i, smallest, dataList);
			heapDown(smallest, dataList);
	}

	//i<j
	private static <NodeType, EdgeType> void swap(int i, int j,List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> dataList) {
		List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> midList = new ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>>();
		midList.add(dataList.get(i));
		dataList.remove(i);
		dataList.add(i, dataList.get(j-1));//add in specified position
		dataList.remove(j);
		dataList.add(j, midList.get(0));
	}

}
