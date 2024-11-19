/**  
 * Project Name:PatKG_core_JoinOprV2.0  
 * File Name:RETables.java  
 * Package Name:dataStructures  
 * Date:Dec 28, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package joinAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

/**  
 * ClassName:RETables   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Dec 28, 2019   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class InsTable{
	
	private ArrayList<Instances> instanceList;//(int, node)
	private HashSet<Integer> patternNodes;
	private int frequency;
//	private HashMap<Integer, Integer> nodeLabelMap;//(nodeIdx, nodeLabel)

	
	public InsTable() {
		instanceList = new ArrayList<Instances>();
		patternNodes = new HashSet<Integer>();
//		nodeLabelMap = new HashMap<Integer, Integer>();
	}
	
	public void joinAssign(Instances ins, int addIdx, int addNode) {// (be added pnodeIdx, add graph node)
		Instances inscopy = ins.copy();
		inscopy.assign(addIdx, addNode);
		instanceList.add(inscopy);
	}
	
	public void reJoinAssign(HashSet<Integer> adjPatternIdx, int joinNode, 
			Instances curIns, Instances adjIns) {
		Instances inscopy = null;
		for (int addIdx : adjPatternIdx) {
			if (addIdx != joinNode) {
				int addNode = adjIns.getNode(addIdx);
				if (!curIns.contains(addNode)) {// has not been joined before
					if(inscopy == null)
						inscopy = curIns.copy();
					inscopy.assign(addIdx, addNode);
				}else
					return;
			}
		}
		if(inscopy != null){
			instanceList.add(inscopy);
		}
	}
	
	public void setPatternNodes(){
		if(instanceList != null && instanceList.size() !=0){
			Instances oneIns = instanceList.get(0);
			Integer[] instances = oneIns.getIsoInstances();
			for (int i = 0; i < instances.length; i++) {
				if (instances[i] != -1) {
					patternNodes.add(i);
				}
			}
		}
	}
	
	public void reJoinAssignCycOld(HashSet<Integer> adjPatternIdx, ArrayList<Integer> joinNodes, Instances curIns,
			Instances adjIns) {
		Instances inscopy = null;
		for (int addIdx : adjPatternIdx) {
			if (!joinNodes.contains(addIdx)) {
				int addNode = adjIns.getNode(addIdx);
				if (!curIns.contains(addNode)) {// has not been joined before
					if (inscopy == null)
						inscopy = curIns.copy();
					inscopy.assign(addIdx, addNode);
				} else
					return;
			}
		}
		if (inscopy != null) {
			instanceList.add(inscopy);
		}
	}
	
	public void reJoinAssignCyc(HashSet<Integer> adjPatternIdx, ArrayList<Integer> joinNodes, Instances curIns,
			Instances adjIns) {
		Instances inscopy = null;
		for (int addIdx : adjPatternIdx) {
			if (!joinNodes.contains(addIdx)) {
				int addNode = adjIns.getNode(addIdx);
				if (!curIns.contains(addNode)) {// has not been joined before
					if (inscopy == null)
						inscopy = curIns.copy();
					inscopy.assign(addIdx, addNode);
				} else
					return;
			}
		}
		if (inscopy != null) {
			instanceList.add(inscopy);
		}
	}

	public void addItem(Instances ins) {
		instanceList.add(ins);
	}
	
	public ArrayList<Instances> getInsList(){
		return instanceList;
	}
	
	
	public int getSize() {
		return instanceList.size();
	}

	public Instances get(int index) {
		return instanceList.get(index);
	}
	
	public HashSet<Integer> patternNodes(){
		return patternNodes;
	}
	
	public HashSet<Integer> getPatNodes(){
		Instances oneIns = instanceList.get(0);
		Integer[] instances = oneIns.getIsoInstances();
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] != -1) {
				patternNodes.add(i);
			}
		}
		return patternNodes;
	} 
	
	public int ifrequency(){
		if(frequency != 0){
			return frequency;
		}
		if(instanceList == null || instanceList.size() == 0){
			return 0;
		}
		Instances oneIns = instanceList.get(0);
		HashMap<Integer, HashSet<Integer>> MNIinstanceMap = new HashMap<Integer, HashSet<Integer>>();
		
		Integer[] instances = oneIns.getIsoInstances();
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] != -1) {
				patternNodes.add(i);
			}
		}
		int initialNode = 0;
		Iterator it = instanceList.iterator();
		while(it.hasNext()) {
			Instances midIns = (Instances) it.next();
			for(int i:patternNodes){
				HashSet<Integer> LL = MNIinstanceMap.get(i);
				if(LL == null) {
					LL = new HashSet<Integer>();
					LL.add(midIns.getIsoInstances(i));
					MNIinstanceMap.put(i, LL);
				}else if(LL != null) {
					LL.add(midIns.getIsoInstances(i));
					MNIinstanceMap.put(i, LL);
				}
				initialNode = i;
			}
		}
		frequency = MNIinstanceMap.get(initialNode).size();
		for(Entry<Integer, HashSet<Integer>> entry:MNIinstanceMap.entrySet()) {
			int tmp=entry.getValue().size();
			if(frequency > tmp) {
				frequency = tmp;
			}
		}
		return frequency;
	}
	
	public int efrequency(){
		if(instanceList == null || instanceList.size() == 0){
			return 0;
		}
		HashMap<Integer, HashSet<Integer>> MNIinstanceMap = new HashMap<Integer, HashSet<Integer>>();
		Instances oneIns = instanceList.get(0);
		Integer[] instances = oneIns.getIsoInstances();
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] != -1) {
				patternNodes.add(i);
			}
		}
		int initialNode = 0;
		Iterator it = instanceList.iterator();
		while(it.hasNext()) {
			Instances midIns = (Instances) it.next();//-1,1,-1,-1,6,7
			for(int i:patternNodes){
				HashSet<Integer> LL = MNIinstanceMap.get(i);
				if(LL == null) {
					LL = new HashSet<Integer>();
					LL.add(midIns.getIsoInstances(i));
					MNIinstanceMap.put(i, LL);
				}else if(LL != null) {
					LL.add(midIns.getIsoInstances(i));
					MNIinstanceMap.put(i, LL);
				}
				initialNode = i;
			}
		}
		frequency = MNIinstanceMap.get(initialNode).size();
		for(Entry<Integer, HashSet<Integer>> entry:MNIinstanceMap.entrySet()) {
			int tmp=entry.getValue().size();
			if(frequency > tmp) {
				frequency = tmp;
			}
		}
		return frequency;
	}

	/*
	 * sort the instances in join attribute by increasing order
	 */
	@SuppressWarnings("unchecked")
	public void sortIns(int joinNode){
		Collections.sort(instanceList, new ComparatorPair(){
			@Override
			public int compare(Object o1, Object o2) {
				Instances p1 = (Instances)o1;
				Instances p2 = (Instances)o2;
				return p1.getIsoInstances()[joinNode] - p2.getIsoInstances()[joinNode];
			}
		});
	}

}
  
