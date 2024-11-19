package VF2check;

import java.awt.Label;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Graph Class, along with some methods to manipulate the graph.
 * @author luo123n
 */
public class VF2Graph {
	
	public String name; // name of the graph
	public ArrayList<VF2Node> nodes = new ArrayList<VF2Node>(); // list of all nodes
	public ArrayList<VF2Edge> edges = new ArrayList<VF2Edge>(); // list of all edges
	
	int[][] adjacencyMatrix; // stores graph structure as adjacency matrix (-1: not adjacent, >=0: the edge label)
	boolean adjacencyMatrixUpdateNeeded = true; // indicates if the adjacency matrix needs an update
	
	public VF2Graph(String name) {
		this.name = name;
	}
	
	public VF2Graph() {
		this.name = new String();
		this.nodes = new ArrayList<VF2Node>();
		this.edges = new ArrayList<VF2Edge>();
		this.adjacencyMatrix = new int[this.nodes.size()][this.nodes.size()];
		this.adjacencyMatrixUpdateNeeded = new Boolean(adjacencyMatrixUpdateNeeded);
//		this.name = graph.name;
//		this.nodes = graph.nodes;
//		this.edges = graph.edges;
//		this.adjacencyMatrix = graph.adjacencyMatrix;
//		this.adjacencyMatrixUpdateNeeded = graph.adjacencyMatrixUpdateNeeded;
	}

	public void addNode(int id, int label) {
		nodes.add(new VF2Node(this, id, label));
		this.adjacencyMatrixUpdateNeeded = true;
	}
	public VF2Node maxNode() {
		VF2Node maxNode = nodes.get(nodes.size()-1);
		return maxNode;
	}
	
	public void addEdge(VF2Node source, VF2Node target, int label) {
		edges.add(new VF2Edge(this, source, target, label));
		this.adjacencyMatrixUpdateNeeded = true;
	}
	
	public void addEdge(int sourceId, int targetId, int label) {
		this.addEdge(this.nodes.get(sourceId), this.nodes.get(targetId), label);
	}
	
	public int getMaxindexNeighbor(int maxIndex, int[][] patternMatrix) {
		for (int i = 0; i < patternMatrix.length - 1; i++) {
			int j = patternMatrix[maxIndex][i];
			if (j != -1) {
				return i;
			}
		}
		for (int m = 0; m < patternMatrix.length - 1; m++) {
			int k = patternMatrix[m][maxIndex];
			if (k != -1) {
				return m;
			}
		}
		return maxIndex;
	}
	
	/**
	 * Get the adjacency matrix
	 * Reconstruct it if it needs an update
	 * @return Adjacency Matrix
	 */
	public int[][] getAdjacencyMatrix() {
		
		if (this.adjacencyMatrixUpdateNeeded) {
			
			int k = this.nodes.size();
			this.adjacencyMatrix = new int[k][k];	// node size may have changed
			for (int i = 0 ; i < k ; i++)			// initialize entries to -1	
				for (int j = 0 ; j < k ; j++)
					this.adjacencyMatrix[i][j] = -1; 
			
			for (VF2Edge e : this.edges) {
				this.adjacencyMatrix[e.source.id][e.target.id] = e.label; // label must bigger than -1
			}
			this.adjacencyMatrixUpdateNeeded = false;
		}
		return this.adjacencyMatrix;
	}
	
	// prints adjacency matrix to console
	public void printGraph() {
		int[][] a = this.getAdjacencyMatrix();
		int k = a.length;
		
		System.out.print(this.name + " - Nodes: ");
		for (VF2Node n : nodes) System.out.print(n.id + " ");
		System.out.println();
		for (int i = 0 ; i < k ; i++) {
			for (int j = 0 ; j < k ; j++) {
				System.out.print(a[i][j] + " ");
			}
			System.out.println();
		}
	}
}
