package VF2check;

import java.util.ArrayList;

public class VF2Node {
	
	public VF2Graph graph; // the graph to which the node belongs
	
	public int id; // a unique id - running number
	public int label; // for semantic feasibility checks
	
	public ArrayList<VF2Edge> outEdges = new ArrayList<VF2Edge>(); // edges of which this node is the origin
	public ArrayList<VF2Edge> inEdges = new ArrayList<VF2Edge>(); // edges of which this node is the destination
	
	public VF2Node(VF2Graph g, int id, int label) {
		this.graph = g;
		this.id = id;
		this.label = label;
	}	
}