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

package joinAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.IntFrequency;
import dataStructures.myNode;

public class PatternEdges implements Cloneable
{

	private int indexA;
	private int indexB;
	private int labelA;
	private int labelB;
	private int edgeIdx;
	private double edgeLabel;
	private String pattern;
	private ArrayList<int[]> nodePairs;
	private int joinNode;
	private int MNI;
	private boolean fringeEdge;
	private int selfNum;
	private int selfJoinNode;


	private HashSet<Integer> aSet;
	private HashSet<Integer> bSet;
	private HashSet<Integer> nodeASet;
	private HashSet<Integer> nodeBSet;
	private HashSet<Integer> traverseASet;//has traversed nodes
	private HashSet<Integer> traverseBSet;
	private HashSet<Integer> validASet;//marked nodes that are valid
	private HashSet<Integer> validBSet;
	private HashSet<Integer> unTraverseASet;//not traversed nodes
	private HashSet<Integer> unTraverseBSet;
	private int minMNInode;
	private HashMap<Integer, HashSet<Integer>> pointMap;
	private boolean sortClassify;
	public PatternEdges() {
		
	}
	
	public PatternEdges(int indexA,int labelA,int indexB ,int labelB, double edgeLabel, int edgeIdx) 
	{
		this.labelA=labelA;
		this.labelB= labelB;
		this.indexA=indexA;
		this.indexB=indexB;
		this.edgeLabel=edgeLabel;
		this.edgeIdx=edgeIdx;
		this.joinNode = -1;
		this.setMNI(-1);
		this.minMNInode = -1;
		this.fringeEdge = false;
		this.selfJoinNode = -1;

		aSet = new HashSet<Integer>();
		bSet = new HashSet<Integer>();
		traverseASet = new HashSet<Integer>();
		traverseBSet = new HashSet<Integer>();
		validASet = new HashSet<Integer>();
		validBSet = new HashSet<Integer>();
		unTraverseASet = new HashSet<Integer>();
		unTraverseBSet = new HashSet<Integer>();
		nodePairs = new ArrayList<int[]>();
		this.pointMap = new HashMap<Integer, HashSet<Integer>>();
		this.sortClassify = false;
	}
	
	public PatternEdges copy() {
		PatternEdges pe = new PatternEdges();
		pe.indexA = indexA;
		pe.indexB = indexB;
		pe.joinNode = joinNode;
		pe.edgeIdx = edgeIdx;
		pe.unTraverseASet = new HashSet<>();
		pe.unTraverseBSet = new HashSet<>();
		return pe;
	}
	public int domSize(HashMap<Integer, HashSet<Integer>> domainMap) {
		int domASize = domainMap.get(indexA).size();
		int domBSize = domainMap.get(indexB).size();
		if(domASize < domBSize) {
			return domASize;
		}else
			return domBSize;
	}
	
	public int upperBound(){
		HashSet<Integer> setA = new HashSet<Integer>();
		HashSet<Integer> setB = new HashSet<Integer>();
		Iterator it = nodePairs.iterator();
		while(it.hasNext()){
			int[] arr = (int[]) it.next();
			setA.add(arr[0]);
			setB.add(arr[1]);
		}
		if(setA.size() < setB.size()) {
			return setA.size();
		} else{
			return setB.size();
		}
	}
	
	public int ifrequency(){
		if(getMNI() != -1){
			return getMNI();
		}
		if(nodePairs == null || nodePairs.size() == 0){
			return 0;
		}
		nodeASet = new HashSet<Integer>();
		nodeBSet = new HashSet<Integer>();
		Iterator it = nodePairs.iterator();
		while(it.hasNext()){
			int[] arr = (int[]) it.next();
			nodeASet.add(arr[0]);
			nodeBSet.add(arr[1]);
		}
		setMNI(nodeASet.size());
		int tmp = nodeBSet.size();
		if(getMNI() > tmp) {
			setMNI(tmp);
			this.minMNInode = indexB;
		}else {
			this.minMNInode = indexA;
		}
		return getMNI();
	}
	
	public void setList(ArrayList<int[]> nodePairs){
		this.nodePairs = nodePairs;
		Iterator it = nodePairs.iterator();
		while(it.hasNext()){
			int[] arr = (int[]) it.next();
			unTraverseASet.add(arr[0]);
			unTraverseBSet.add(arr[1]);
		}
	}
	
	public void setListOnly(ArrayList<int[]> nodePairs){
		this.nodePairs = nodePairs;
	}
	
	public void setTraverseList(ArrayList<int[]> nodePairs){
		this.nodePairs = nodePairs;
		Iterator it = nodePairs.iterator();
		while(it.hasNext()){
			int[] arr = (int[]) it.next();
			unTraverseASet.add(arr[0]);
			unTraverseBSet.add(arr[1]);
		}
	}
	
	public HashSet<Integer> nodeASet(){
		return nodeASet;
	}
	public HashSet<Integer> nodeBSet(){
		return nodeBSet;
	}
	
	public void setSelfNode(int selfNode) {
		this.selfJoinNode = selfNode;
	}
	public int getSelfNode(){
		return selfJoinNode;
	}
	
	public void setFringeEdge() {
		this.fringeEdge = true;
	}
	public boolean isFringe() {
		return fringeEdge;
	}
	
	public void setSelfNum(int number){
		this.selfNum = number;
	}

	public int getSelfNum() {
		return selfNum;
	}

	
	public int getLabelA() {
		return labelA;
	}

	public int getLabelB() {
		return labelB;
	}
	
	public int getIndexA()
	{
		return indexA;
	}
	
	public int getIndexB()
	{
		return indexB;
	}
	public int getMinNode() {
		return minMNInode;
	}
	
	public double getEdgeLabel()
	{
		return edgeLabel;
	}
	public int getEdgeIdx() {
		return edgeIdx;
	}
	public String getPattern() {
		pattern = labelA + "_" +(int)edgeLabel +"+"+labelB;
		return pattern;
	}
	
	public HashSet<Integer> getUntraverASet(){
		return unTraverseASet;
	}
	
	public HashSet<Integer> getUntraverBSet(){
		return unTraverseBSet;
	}
	
	public int autoMorphic(PatternEdges edgeT){
		if(labelA == edgeT.getLabelA() && labelB == edgeT.getLabelB()){
			if(indexA == edgeT.indexA){
				return indexA;
			}else{
				return indexB;
			}
		}else{
			return -1;
		}
	}
	
	public void printEdge() {
		System.out.println("--------------------");
		System.out.println("edgeIdx:" + edgeIdx);
		System.out.println("pattern:" + getPattern());
		Iterator it = nodePairs.iterator();
		while (it.hasNext()) {
			int[] arr = (int[]) it.next();
			System.out.println(arr[0] + "-" + arr[1]);
		}
	}

	public ArrayList<int[]> getNodePairs() {
		return nodePairs;
	}

	public void setJoinNode(int node) {
		this.joinNode = node;
	}
	public int getJoinNode(){
		return joinNode;
	}

	public HashSet<Integer> aSet() {
		return aSet;
	}

	public HashSet<Integer> bSet() {
		return bSet;
	}
	
	public void pruneFailJoinNodes(int joinNode, HashSet<Integer> inter){
		ArrayList<int[]> list = nodePairs;
		//prune nodes in curEdge
		if(joinNode == indexA){
			Iterator it = list.iterator();
			while(it.hasNext()){
				int[] arr = (int[]) it.next();
				if(!inter.contains(arr[0])){
					it.remove();
				}
			}
		}else if(joinNode == indexB){
			Iterator it = list.iterator();
			while(it.hasNext()){
				int[] arr = (int[]) it.next();
				if(!inter.contains(arr[1])){
					it.remove();
				}
			}
		}
		
	}
	

	/*
	 * reduce tuples from the edge tables
	 * @param: pe -> pattern edge table
	 */
	public InsTable tupleReduce(int qrySize){
		InsTable reducedTable = new InsTable();
		ArrayList<int[]> reducedList = new ArrayList<int[]>();
		HashSet<Integer> addA = new HashSet<Integer>();
		HashSet<Integer> addB = new HashSet<Integer>();
		
		if(minMNInode == indexA) {//MNI(A) < MNI(B)
			for(int i=0;i<nodePairs.size();i++){
				int[] arr = nodePairs.get(i);
//				if(!addA.contains(arr[0])) {
				if(!addA.contains(arr[0]) && !validASet.contains(arr[0])) {
					reducedList.add(arr);
					addA.add(arr[0]);
					addB.add(arr[1]);
					if(addA.size() == aSet.size())
						break;
				}
			}
			int addAsize = addA.size();
			if(addB.size() < addAsize) {//need add from column B
				for(int i=0;i<nodePairs.size();i++){
					int[] arr = nodePairs.get(i);
//					if(!addB.contains(arr[1])){
					if(!addB.contains(arr[1]) && !validBSet.contains(arr[1])) {
						reducedList.add(arr);
						addB.add(arr[1]);
						if(addB.size() == addAsize)
							break;
					}
				}
			}
		}else if(minMNInode == indexB) {//MNI(A) > MNI(B)
			for(int i=0;i<nodePairs.size();i++){
				int[] arr = nodePairs.get(i);
//				if(!addB.contains(arr[1])) {
				if(!addB.contains(arr[1]) && !validBSet.contains(arr[1])) {
					reducedList.add(arr);
					addA.add(arr[0]);
					addB.add(arr[1]);
					if(addB.size() == bSet.size())
						break;
				}
			}
			int addBsize = addB.size();
			if(addA.size() < addBsize) {//need add from column A
				for(int i=0;i<nodePairs.size();i++){
					int[] arr = nodePairs.get(i);
//					if(!addA.contains(arr[0])){
					if(!addA.contains(arr[0]) && !validASet.contains(arr[0])) {
						reducedList.add(arr);
						addA.add(arr[0]);
						if(addA.size() == addBsize)
							break;
					}
				}
			}
		}
		
		for (int i = 0; i < reducedList.size(); i++) {
			int arr[] = reducedList.get(i);
			// initialize the instance by pattern size
			Instances instance = new Instances(qrySize);
			instance.assign(indexA, arr[0]);// (pattern nodeIdx, node)
			instance.assign(indexB, arr[1]);
			reducedTable.addItem(instance);
		}
		return reducedTable;
	}
	
	public InsTable samplingTuples(int qrySize) {
		InsTable sampleTable = new InsTable();
		ArrayList<int[]> sampledList = new ArrayList<int[]>();
		HashSet<Integer> aSampled = new HashSet<Integer>();
		HashSet<Integer> bSampled = new HashSet<Integer>();
		if(aSet.size() < bSet.size()) {//sampling column b
			Iterator it = nodePairs.iterator();
			while(it.hasNext()){
				int[] arr = (int[]) it.next();
//				if(!aSampled.contains(arr[0])) {
				if(!aSampled.contains(arr[0]) && !validASet.contains(arr[0])) {
					sampledList.add(new int[]{arr[0],arr[1]});
					aSampled.add(arr[0]);
				}
			}
		}else if(aSet.size() > bSet.size()){//sampling column a
			Iterator it = nodePairs.iterator();
			while(it.hasNext()){
				int[] arr = (int[]) it.next();
//				if(!bSampled.contains(arr[1])) {
				if(!bSampled.contains(arr[1]) && !validBSet.contains(arr[1])) {
					sampledList.add(new int[]{arr[0],arr[1]});
					bSampled.add(arr[1]);
				}
			}
		}else if(aSet.size() == bSet.size()){
			Iterator it = nodePairs.iterator();
			while(it.hasNext()){
				int[] arr = (int[]) it.next();
				sampledList.add(new int[]{arr[0],arr[1]});
			}
		}
		
		for (int i = 0; i < sampledList.size(); i++) {
			int arr[] = sampledList.get(i);
			// initialize the instance by pattern size
			Instances instance = new Instances(qrySize);
			instance.assign(indexA, arr[0]);// (pattern nodeIdx, node)
			instance.assign(indexB, arr[1]);
			sampleTable.addItem(instance);
		}
		return sampleTable;
	}

	public HashSet<Integer> getValidSet() {
		if(minMNInode == indexA) {
			return validASet;
		}else {
			return validBSet;
		}
	}
	
	public HashSet<Integer> getValidASet(){
		return validASet;
	}
	
	public HashSet<Integer> getValidBSet(){
		return validBSet;
	}
	
	public HashSet<Integer> unTraversedSet() {
		if(minMNInode == indexA) {
			return unTraverseASet;
		}else {
			return unTraverseBSet;
		}
	}
	
	public void setMarkNodes(int nodeIdx, HashSet<Integer> markedNodes) {
		if(nodeIdx == indexA) {
			validASet = markedNodes;
			traverseASet.addAll(markedNodes);
			unTraverseASet.removeAll(validASet);
		}else if(nodeIdx == indexB){
			validBSet = markedNodes;
			traverseBSet.addAll(markedNodes);
			unTraverseBSet.removeAll(validBSet);
		}
	}

	@SuppressWarnings("unchecked")
	public InsTable getTuples(int needGetCount, int qrySize) {
		ArrayList<int[]> needGetList = new ArrayList<int[]>();
		InsTable needGetTuples = new InsTable();
		if (this.pointMap.size() == 0 && !sortClassify) {
			this.pointMap = sortClassify();
		}
		if (minMNInode == indexA && pointMap.size() != 0) {// has tuples need to get

//				filter(needGetList, traverseASet, unTraverseASet, needGetCount);

			for (Iterator<java.util.Map.Entry<Integer, HashSet<Integer>>> pointIt = pointMap.entrySet()
					.iterator(); pointIt.hasNext();) {
				java.util.Map.Entry<Integer, HashSet<Integer>> map = pointIt.next();
				int joinNode = map.getKey();
				if (!traverseASet.contains(joinNode) && needGetList.size() < needGetCount) {
					HashSet<Integer> points = map.getValue();
					needGetList.add(new int[] { joinNode, points.iterator().next() });
					points.remove(points.iterator().next());
					if (points.size() == 0) {
						// mark these nodes as traversed nodes
						traverseASet.add(joinNode);
						unTraverseASet.remove(joinNode);
						pointIt.remove();
					}
				}
			}
		} else if (minMNInode == indexB && pointMap.size() != 0) {// has tuples need to get
//				filter(needGetList, traverseBSet, unTraverseBSet, needGetCount);

			for (Iterator<java.util.Map.Entry<Integer, HashSet<Integer>>> pointIt = pointMap.entrySet()
					.iterator(); pointIt.hasNext();) {
				java.util.Map.Entry<Integer, HashSet<Integer>> map = pointIt.next();
				int joinNode = map.getKey();
				if (!traverseBSet.contains(joinNode) && needGetList.size() < needGetCount) {
					HashSet<Integer> points = map.getValue();
					needGetList.add(new int[] { points.iterator().next(), joinNode });
					points.remove(points.iterator().next());
					if (points.size() == 0) {
						// mark these nodes as traversed nodes
						traverseBSet.add(joinNode);
						unTraverseBSet.remove(joinNode);
						pointIt.remove();
					}
				}

			}

		}
		// assign
		for (int i = 0; i < needGetList.size(); i++) {
			int arr[] = needGetList.get(i);
			// initialize the instance by pattern size
			Instances instance = new Instances(qrySize);
			instance.assign(indexA, arr[0]);// (pattern nodeIdx, node)
			instance.assign(indexB, arr[1]);
			needGetTuples.addItem(instance);
		}
		return needGetTuples;
	}
	
	public InsTable getRemainTuples(int qrySize) {
		InsTable remainTuples = new InsTable();
		for(int i=0;i<nodePairs.size();i++) {
			int[] arr = nodePairs.get(i);
			if(!traverseASet.contains(arr[0]) || !traverseBSet.contains(arr[1])) {//TODO
				// initialize the instance by pattern size
				Instances instance = new Instances(qrySize);
				instance.assign(indexA, arr[0]);// (pattern nodeIdx, node)
				instance.assign(indexB, arr[1]);
				remainTuples.addItem(instance);
			}
		}
		return remainTuples;
	}
	
	public void filter(ArrayList<int[]> needGetList, HashSet<Integer> traverseSet, HashSet<Integer> untraverseSet, int needGetCount) {
		for (Iterator<java.util.Map.Entry<Integer, HashSet<Integer>>>  pointIt= pointMap.entrySet().iterator();
				pointIt.hasNext();) {
			java.util.Map.Entry< Integer, HashSet<Integer>> map = pointIt.next();
			int joinNode = map.getKey();
			HashSet<Integer> points = map.getValue();
			if(!traverseSet.contains(joinNode) && needGetList.size() < needGetCount) {
				needGetList.add(new int[] {points.iterator().next(), joinNode});
				points.remove(points.iterator().next());
				if(points.size() == 0) {
					//mark these nodes as traversed nodes
					traverseSet.add(joinNode);
					untraverseSet.remove(joinNode);
					pointIt.remove();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer, HashSet<Integer>> sortClassify(){
		final int joinAttribute;
		int negated = -1;
		if(minMNInode == indexA) {
			joinAttribute = 0;
			negated = 1;
		}else {
			joinAttribute = 1;
			negated = 0;
		}
		//need sort ????????
		//sort the table by join attribute in ascending order
		Collections.sort(nodePairs, new ComparatorPair(){
			@Override
			public int compare(Object o1, Object o2) {
				int[] p1 = (int[])o1;
				int[] p2 = (int[])o2;
				return p1[joinAttribute] - p2[joinAttribute];
			}
		});
		HashMap<Integer, HashSet<Integer>> pointMap = new HashMap<>();
		for(int i=0;i<nodePairs.size();i++) {
			int[] arr = nodePairs.get(i);
			HashSet<Integer> pointSet = pointMap.get(arr[joinAttribute]);
			if(pointSet == null) {
				pointSet = new HashSet<>();
				pointSet.add(arr[negated]);
				pointMap.put(arr[joinAttribute], pointSet);
			}else if(!pointSet.contains(arr[negated])){
				pointSet.add(arr[negated]);
			}
		}
		sortClassify = true;
		return pointMap;
	}

	public int getMNI() {
		return MNI;
	}

	public void setMNI(int mNI) {
		MNI = mNI;
	}

	public void changeMinNode() {
		if (validASet.size() > validBSet.size() && minMNInode == indexA) {
			this.minMNInode = indexB;
			this.pointMap = sortClassify();
		} else if (validASet.size() <= validBSet.size() && minMNInode == indexB) {
			this.minMNInode = indexA;
			this.pointMap = sortClassify();
		}
	}

	public int getAnother() {
		if(minMNInode == indexA) {
			return indexB;
		}else
			return indexA;
	}
}
