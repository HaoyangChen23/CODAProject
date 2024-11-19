package topKresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dataStructures.Frequency;
import dataStructures.IntFrequency;
import search.SearchLatticeNode;

public class MaxHeap <NodeType, EdgeType> {
	private List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> heap;
	 
    private List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> orginList;
	private IntFrequency maxFrequency;

    public void initOriginList(List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> orginList) {
        this.orginList = orginList;
    }
 
	public List<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> getHeap() {
        return heap;
    }
	
	public Integer getSize(){
		return heap.size();
	}
	
	public IntFrequency getMaxFrequency() {
		for(Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry0 : heap.get(0).entrySet()) {
			this.maxFrequency = (IntFrequency) entry0.getValue();
		}
		return maxFrequency;
	}
	
	public MaxHeap(){
		this.heap = new ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>>();
	}
 
    /**
     * insert up
     *
     * @param start
     */
	protected void adjustUp(int start) {
		int currentIndex = start;
		int parentIndex = (currentIndex - 1) / 2;

		HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> currentIndexMap = heap.get(currentIndex);
		OUT:
		while (currentIndex > 0) {
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> parentIndexMap = heap.get(parentIndex);
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> currentIndexentry : currentIndexMap.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> parentIndexentry : parentIndexMap.entrySet()) {
//					int cmp = entryp.getValue().compareTo(entrytmp.getValue());
					if (parentIndexentry.getValue().compareTo(currentIndexentry.getValue())>=0) {
						break OUT;
					} else {
						heap.set(currentIndex, heap.get(parentIndex));
						currentIndex = parentIndex;
						parentIndex = (parentIndex - 1) / 2;
					}
				}
			}
		}
		heap.set(currentIndex, currentIndexMap);
	}
 
    public void insert(HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> data) {
        int size = heap.size();
        heap.add(data);    //add in the end
        adjustUp(size);        
    }
 
    public void remove(int index) {
        int size = heap.size();
        heap.set(index, heap.get(size - 1));
        heap.remove(size - 1);
        if(heap.size() == 0){
        	return;
        }
        adjustDown(index);
    }
 
    /**
     * delete down
     *
     * @param index
     */
	private void adjustDown(int index) {
		int currentIndex = index;
		int leftChildIndex = index * 2 + 1;
		int rightChildIndex = index * 2 + 2;
		int size = heap.size();
		OUT:
		while (leftChildIndex < size) {

			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> currentIndexMap = heap.get(currentIndex);
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> left = null;
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> right = null;
			if (leftChildIndex < size) {
				left = heap.get(leftChildIndex);
			}
			if (rightChildIndex < size) {
				right = heap.get(rightChildIndex);
			}

			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entryl : left.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entryt : currentIndexMap.entrySet()) {
					int maxIndex= 0;
					if(right != null){
						for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entryr : right.entrySet()) {
							maxIndex = (entryl.getValue().compareTo(entryr.getValue()) >= 0 ? leftChildIndex: rightChildIndex);
						}
						
					}else{
						maxIndex = leftChildIndex;
					}
//						int maxIndex = right == null ? leftChildIndex
//								: (entryl.getValue().compareTo(entryr.getValue()) >= 0 ? leftChildIndex
//										: rightChildIndex);
						HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> max = heap.get(maxIndex);
						for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entryMax : max.entrySet()) {

							if (entryt.getValue().compareTo(entryMax.getValue()) >= 0) {
								break OUT;
							} else {
								heap.set(currentIndex, max);
								heap.set(maxIndex, currentIndexMap);
                                currentIndex = maxIndex;//add
								leftChildIndex = maxIndex * 2 + 1;
								rightChildIndex = leftChildIndex + 1;
							}
						}
					}

				}

		}
	}
 
	public void makeHeap(int first, int last) {
        for (int i = first; i < last; i++) {
            insert(orginList.get(i));
        }
    }
	public HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> popHeap(int first) {
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> reMap = heap.get(0);
		remove(first);
        return reMap;
    }
	public HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> mostFrequent(int first) {
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> reMap = heap.get(0);
        return reMap;
    }
 
	public void pushHeap(int first, int last) {
        adjustUp(last - 1);
    }
 
	public void display() {
        System.out.println(heap);
    }

}
