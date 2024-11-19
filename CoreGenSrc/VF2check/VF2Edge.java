package VF2check;

public class VF2Edge {
	
	public VF2Graph graph; 	// the graph to which the edge belongs
	
	public VF2Node source; 	// the source / origin of the edge
	public VF2Node target; 	// the target / destination of the edge 
	public int label; 	// the label of this edge
	
	// creates new edge
	public VF2Edge(VF2Graph g, VF2Node source, VF2Node target, int label) {
		this.graph = g;
		this.source = source; // store source
		source.outEdges.add(this); // update edge list at source
		this.target = target; // store target
		target.inEdges.add(this); // update edge list at target
		this.label = label;
	}
	
}
