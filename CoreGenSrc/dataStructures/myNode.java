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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import utilities.MyPair;


public class myNode 
{
	private	int ID;
	private	int label;
	private int[] shortestPaths;
	
	private HashMap<Integer, ArrayList<MyPair<Integer, Double>>> reachableNodes;//represented by Label~<nodeID,edge_label>, represents the outgoing nodes
	
	private HashMap<Integer, ArrayList<MyPair<Integer, Double>>> reachedBYNodes; //represented by Label~<nodeID,edge_label>, represents the ingoing nodes
	
	@Override
	public String toString() {
	
		String format="("+ID+":"+label+")";
		return format;
	}
	
	public myNode(int ID, int label)
	{
		this.ID = ID;
		this.label = label;
	}
	
	public int getOutDegree(int label)
	{
		if(reachableNodes==null)
			return 0;
		if(reachableNodes.get(label)==null)
			return 0;
		return reachableNodes.get(label).size();
	}
	
	
	public List getAEdgeLabelsList(int label){
		if(reachableNodes==null)
			return null;
		if(reachableNodes.get(label)==null)
			return null;
		ArrayList<MyPair<Integer, Double>> reachEdgeLabel = reachableNodes.get(label);
		
		ArrayList<Double> reachableEdgeList = new ArrayList<Double>();
		for(MyPair<Integer, Double> s:reachEdgeLabel){
			Double edgeLabel = s.getB();
			reachableEdgeList.add(edgeLabel);
		}
		
		return reachableEdgeList;
	}
	
	public List getBEdgeLabelsList(int label){
		if(reachedBYNodes==null)
			return null;
		if(reachedBYNodes.get(label)==null)
			return null;
		ArrayList<MyPair<Integer, Double>> reachEdgeLabel = reachedBYNodes.get(label);
		
		ArrayList<Double> reachableEdgeList = new ArrayList<Double>();
		for(MyPair<Integer, Double> s:reachEdgeLabel){
			Double edgeLabel = s.getB();
			reachableEdgeList.add(edgeLabel);
		}
		
		return reachableEdgeList;
	}
	
	
	public int getinDegree(int label)
	{
		if(reachedBYNodes==null)
			return 0;
		if(reachedBYNodes.get(label)==null)
			return 0;
		return reachedBYNodes.get(label).size();
	}
	
	public int getID()
	{
		return ID;
	}
	public int getLabel()
	{
		return label;
	}
	
	public void addreachableNode(myNode node, double edgeLabel)
	{
		if(reachableNodes==null)
			reachableNodes= new HashMap<Integer, ArrayList<MyPair<Integer, Double>>>();
		
		ArrayList<MyPair<Integer, Double>> list=reachableNodes.get(node.getLabel());
		if(list==null)
			{
				list = new ArrayList<MyPair<Integer, Double>>();
				reachableNodes.put(node.getLabel(), list);
			}
		if(!list.contains(node.getID()))
			list.add(new MyPair(node.getID(), edgeLabel));
		node.addreachedBYNodes(this, edgeLabel);
		
	}
	
	
	private void addreachedBYNodes(myNode node, double edgeLabel)
	{
		if(reachedBYNodes==null)
			reachedBYNodes= new HashMap<Integer, ArrayList<MyPair<Integer, Double>>>();
		ArrayList<MyPair<Integer, Double>> list=reachedBYNodes.get(node.getLabel());
		if(list==null)
			{
				list = new ArrayList<MyPair<Integer, Double>>();
				reachedBYNodes.put(node.getLabel(), list);
			}
		if(!list.contains(node.getID()))
			list.add(new MyPair(node.getID(), edgeLabel));
	}
	
	public void printOutReachableNodes()
	{
		if(reachableNodes==null)
			return;
		for (Iterator<ArrayList<MyPair<Integer, Double>>> iterator = reachableNodes.values().iterator(); iterator.hasNext();)
		{
			ArrayList<MyPair<Integer, Double>> arr =  iterator.next();
			for (int i = 0; i < arr.size(); i++) 
			{
				System.out.println("Node: "+ID+" is within reach of Node "+arr.get(i));
			}
			
		}
		
	}
	
	/**
	 * a fast set reachable function
	 * @param graph
	 * @param freqNodesByLabel
	 */
	public void setReachableNodes_1hop(Graph graph,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel)
	{
		//get edge for each node
		IntIterator it= graph.getListGraph().getOutEdgeIndices(getID());
		for (; it.hasNext();) 
		{
			int edge =  it.next();
			myNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, getID()));
			if(freqNodesByLabel.containsKey(otherNode.getLabel()))
					addreachableNode(otherNode, graph.getListGraph().getEdgeLabel(getID(), otherNode.getID()));
		}
	}
	
	public void setReachableNodes_1hopNew(Graph graph,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel, int kthFreq)
	{
		//get edge for each node
		IntIterator it= graph.getListGraph().getOutEdgeIndices(getID());
		for (; it.hasNext();) 
		{
			int edge =  it.next();
			myNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, getID()));
			int nodeLabel = otherNode.getLabel();
			if(freqNodesByLabel.get(nodeLabel).size() <= kthFreq) {
				continue;
			}
			if(freqNodesByLabel.containsKey(nodeLabel))
					addreachableNode(otherNode, graph.getListGraph().getEdgeLabel(getID(), otherNode.getID()));
		}
	}
	
	public boolean hasReachableNodes()
	{
		if(reachableNodes==null)
			return false;
		else
			return true;
	}
	
	public boolean isWithinTheRangeOf(int NodeIndex,int nodeLabel)
	{
		if(reachableNodes.get(nodeLabel)==null)
			return false;
		return reachableNodes.get(nodeLabel).contains(NodeIndex);
	}
	 
	public ArrayList<MyPair<Integer, Double>> getRechableWithNodeIDs(int label, double edgeLabel)
	{
		if(reachableNodes==null) return new ArrayList<MyPair<Integer, Double>>();
		
		ArrayList<MyPair<Integer, Double>> tempArr = new ArrayList<MyPair<Integer, Double>>();
		tempArr.addAll(reachableNodes.get(label));
		for(int j=0;j<tempArr.size();j++)
		{
			MyPair<Integer, Double> mp = tempArr.get(j);
			if(mp.getB().doubleValue()!=edgeLabel)
			{
				tempArr.remove(j);
				j--;
			}
		}
		return tempArr;
	}
	 
	 public HashMap<Integer, ArrayList<MyPair<Integer, Double>>> getReachableWithNodes()
	 {
		 return reachableNodes;
	 }
	 
	 public HashMap<Integer, ArrayList<MyPair<Integer, Double>>> getReachableByNodes()
	 {
		 return reachedBYNodes;
	 }
	
	 public ArrayList<MyPair<Integer, Double>> getRechableByNodeIDs(int label, double edgeLabel)
	{
		if(reachedBYNodes==null) return new ArrayList<MyPair<Integer, Double>>();

		ArrayList<MyPair<Integer, Double>> tempArr = new ArrayList<MyPair<Integer, Double>>();
		tempArr.addAll(reachedBYNodes.get(label));
		for(int j=0;j<tempArr.size();j++)
		{
			MyPair<Integer, Double> mp = tempArr.get(j);
			if(mp.getB().doubleValue()!=edgeLabel)
			{
				tempArr.remove(j);
				j--;
			}
		}
		return tempArr;
	}
}
