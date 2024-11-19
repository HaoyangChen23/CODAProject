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

import joinAlgorithm.PatternEdges;

public class Query 
{
	private HPListGraph<Integer, Double> listGraph; 
	private HashMap<Integer, PatternEdges> peMap;
	public Query(Graph g) {
		listGraph=g.getListGraph();
	}
	
	public Query(HPListGraph<Integer, Double> lsGraph) {
		listGraph=lsGraph;
	}
	
	public HPListGraph<Integer, Double> getListGraph() {
		return listGraph;
	}
	public HashMap<Integer, PatternEdges> getPEMap(){
		return peMap;
	}
	
	public HashMap<Integer,Integer> getNodeLabelMap(){
		HashMap<Integer,Integer> nodeLabelMap = new HashMap<Integer,Integer>();
		HPListGraph<Integer, Double> hp = listGraph;
		int qrySize = hp.getNodeCount();
		for(int i=0;i<qrySize;i++) {
			int label = hp.getNodeLabel(i);//node label of the pattern
			nodeLabelMap.put(i, label);
		}
		return nodeLabelMap;
	}

	
	public ArrayList<ConnectedComponent> getConnectedLabels()
	{
		ArrayList<ConnectedComponent> cls = new ArrayList<ConnectedComponent>();
		
		HPListGraph<Integer, Double> hp = listGraph;
		for (int edge = hp.getEdges().nextSetBit(0); edge >= 0; edge = hp.getEdges().nextSetBit(edge + 1)) 
		{
			int nodeA,nodeB,labelA,labelB;
			
			if(hp.getDirection(edge)>=0)
			{
			nodeA= hp.getNodeA(edge); labelA=hp.getNodeLabel(nodeA);
			nodeB = hp.getNodeB(edge);labelB=hp.getNodeLabel(nodeB);
			}
			else
			{
				nodeB= hp.getNodeA(edge); labelB=hp.getNodeLabel(nodeB);
				nodeA = hp.getNodeB(edge);labelA=hp.getNodeLabel(nodeA);
			}
			
			
			ConnectedComponent cl = new ConnectedComponent(nodeA,labelA, nodeB,labelB, Double.parseDouble(listGraph.getEdgeLabel(nodeA, nodeB)+"")); 
			cls.add(cl);
		}
		return cls;
	}
	
	public ArrayList<PatternEdges> getConnectedEdges()
	{
		ArrayList<PatternEdges> cls = new ArrayList<PatternEdges>();
		peMap = new HashMap<Integer, PatternEdges>();
		HPListGraph<Integer, Double> hp = listGraph;
		for (int edge = hp.getEdges().nextSetBit(0); edge >= 0; edge = hp.getEdges().nextSetBit(edge + 1)) 
		{
			int nodeA,nodeB,labelA,labelB;
			
			if(hp.getDirection(edge)>=0)
			{
			nodeA= hp.getNodeA(edge); labelA=hp.getNodeLabel(nodeA);
			nodeB = hp.getNodeB(edge);labelB=hp.getNodeLabel(nodeB);
			}
			else
			{
				nodeB= hp.getNodeA(edge); labelB=hp.getNodeLabel(nodeB);
				nodeA = hp.getNodeB(edge);labelA=hp.getNodeLabel(nodeA);
			}
			
			int edgeIdx = hp.getEdge(nodeA, nodeB);
			PatternEdges cl = new PatternEdges(nodeA,labelA, nodeB,labelB, 
					Double.parseDouble(listGraph.getEdgeLabel(nodeA, nodeB)+""), edgeIdx); 
			cls.add(cl);
			peMap.put(edgeIdx, cl);
		}
		return cls;
	}
	
}
