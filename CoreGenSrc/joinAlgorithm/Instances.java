package joinAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Instances {
	private Integer[] instances;
	private int qrySize;

	public Instances(int size){
		instances = new Integer[size];
		this.qrySize = size;
		for (int i = 0; i < instances.length; i++) 
		{
			instances[i]=-1;
		}
	}
	
	public void assign(int pIndex, int node) {//(pattern nodeIdx, node instance)
		instances[pIndex]=node;
	}

	public int getNode(int index) {
		return instances[index];
	}
	
	public Instances copy() {
		Instances inscopy = new Instances(qrySize);
		for(int i=0;i<qrySize;i++) {
			inscopy.getIsoInstances()[i]=instances[i];
		}
//		Integer[] arr = instances.clone();
//		inscopy.instances = arr;
		return inscopy;
	}
	
	public boolean contains(int nodeIdx) {
		for(int i=0;i<instances.length;i++ ) {
//			if(instances[i] != -1) {
				if(instances[i] == nodeIdx) {
					return true;
				}
//			}
		}
		return false;
	}
	
	public boolean sameContains(int nodeIdx, ArrayList<Integer> sameLabelList) {
		for(int i=0;i<sameLabelList.size();i++) {
			int sameLabelIdx = sameLabelList.get(i);
			if(instances[sameLabelIdx] == nodeIdx) {
				return true;
			}
		}
		return false;
	}

	public Integer[] getIsoInstances() {
		return instances;
	}
	public int getIsoInstances(int index) {//return node instance
		return instances[index];
	}
	
	public String toString() {
		String s = "";
		for(int i=0;i<instances.length;i++) {
			s += instances[i] + " ";
		}
		return s;
	}

	public int insSize() {
		int counter = 0;
		for (int i = 0; i < instances.length; i++) 
		{
			if(instances[i]!=-1) {
				counter ++;
			}
		}
		return counter;
	}

	public HashSet<Integer> getPatNodes() {
		HashSet<Integer> patternNodes = new HashSet<>();  
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] != -1) {
				patternNodes.add(i);
			}
		}
		return patternNodes;
	}

}
