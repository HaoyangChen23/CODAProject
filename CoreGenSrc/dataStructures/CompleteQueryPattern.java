package dataStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
 * convert the query pattern into DFScode format
 */
public class CompleteQueryPattern<NodeType, EdgeType> {
	private int nodeNum;
	private int edgeNum;
	private ArrayList<Integer> sortedNodeLabels;
	private HashMap<Integer, Integer> nIndexLabelMap;
	private Graph kGraph;
	private HashMap<String, Integer> corePatternEdgeMap;

	public CompleteQueryPattern(Graph kGraph) {
		nIndexLabelMap = new HashMap<Integer, Integer>();
		corePatternEdgeMap = new HashMap<String, Integer>();
		this.kGraph = kGraph;
		nodeNum = 0;
		edgeNum = 0;
		sortedNodeLabels = kGraph.getSortedFreqLabels();
		
	}
	
	public int getNodeNum(){
		return nodeNum;
	}
	
	public HashMap<String, Integer> getCorePatternMap(){
		return corePatternEdgeMap;
	}

	public DFSCode<NodeType, EdgeType> loadFromFile_Core(String fileName) throws Exception {
		final BufferedReader rows = new BufferedReader(new FileReader(new File(fileName)));

		// read graph from rows
		// nodes
		String line;
		String tempLine;
		rows.readLine();

		// new pattern object
		DFSCode<NodeType, EdgeType> pattern = new DFSCode();
		while ((line = rows.readLine()) != null && (line.charAt(0) == 'v')) {
			final String[] parts = line.split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			final int label = Integer.parseInt(parts[2]);
			pattern.setNodeType(label);
			// corePatternString += "v " + index + " " + label + "\n";
			if (index != nodeNum) {
				throw new ParseException("The node list is not sorted", nodeNum);
			}
			nIndexLabelMap.put(index, label);
			nodeNum++;
		}

		tempLine = line;


		// edges
		// use the first edge line
		if (tempLine.charAt(0) == 'e')
			line = tempLine;
		else {
			line = rows.readLine();
//			return pattern;
		}

		if (line != null) {
			do {
				final String[] parts = line.split("\\s+");
				if (parts.length < 3) {// only has one node
					pattern.setSize(1);
					return pattern;
				}
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				final int edgeLabel = Integer.parseInt(parts[3]);
				// corePatternString += "e " + index1 + " " + index2 + " " +
				// edgeLabel + "\n";
				int firstLabel = nIndexLabelMap.get(index1);
				int secondLabel = nIndexLabelMap.get(index2);
				int labelA = sortedNodeLabels.indexOf(firstLabel);
				int labelB = sortedNodeLabels.indexOf(secondLabel);
				String patternEdge = firstLabel+"_"+edgeLabel +"+"+ secondLabel;
				corePatternEdgeMap.put(patternEdge, 0);
				// final GSpanEdge<NodeType, EdgeType> gEdge = new
				// GSpanEdge<NodeType, EdgeType>().set(0, 1, labelA,
				// edgeLabel, labelB, 1, firstLabel, secondLabel);
				final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(index1, index2,
						labelA, edgeLabel, labelB, 1, firstLabel, secondLabel);
				// first line
				if (edgeNum == 0) {
					final ArrayList<GSpanEdge<NodeType, EdgeType>> parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(
							2);
					parents.add(gEdge);
					parents.add(gEdge);

					HPListGraph<NodeType, EdgeType> lg = new HPListGraph<NodeType, EdgeType>();
					gEdge.addTo(lg);
					pattern = new DFSCode<NodeType, EdgeType>(sortedNodeLabels, kGraph, null).set(lg, gEdge, gEdge,
							parents);

				} else if (edgeNum > 0) {
					// final GSpanEdge<NodeType, EdgeType> gEdge = new
					// GSpanEdge<NodeType, EdgeType>()
					// .set(index1, index2, labelA, edgeLabel, labelB, 1,
					// firstLabel, secondLabel);
					pattern = add(gEdge, pattern, 0);
				}
				edgeNum++;
			} while ((line = rows.readLine()) != null && (line.charAt(0) == 'e'));
		}
		return pattern;
	}

	/**
	 * includes the found extension to the corresponding fragment(original)
	 * 
	 * @param gEdge
	 * @param emb
	 * @param code
	 * @param edge
	 * @param nodeB
	 * @return
	 */
	protected DFSCode add(final GSpanEdge<NodeType, EdgeType> gEdge, final DFSCode<NodeType, EdgeType> code, int type) {
		// search corresponding extension
		GSpanExtension<NodeType, EdgeType> ext = new GSpanExtension<NodeType, EdgeType>();

		// create new extension
		HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) code.getHPlistGraph().clone();
		// TODO: avoid clone??
		gEdge.addTo(ng); // reformulate the form of the new extended fragment!!
		ext = new GSpanExtension<NodeType, EdgeType>();
		ext.edge = gEdge;
		ext.frag = new DFSCode<NodeType, EdgeType>(code.getSortedFreqLabels(), code.getSingleGraph(),
				new HashMap<Integer, HashSet<Integer>>()).set((HPListGraph<NodeType, EdgeType>) ng, code.getFirst(),
						code.getLast(), code.getParents());
		ext.frag = (DFSCode<NodeType, EdgeType>) code.extend(ext);
		// children.put(gEdge, ext);
		return ext.frag;
	}

}
