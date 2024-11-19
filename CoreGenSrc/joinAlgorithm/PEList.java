package joinAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;

public class PEList {

	private ArrayList<PatternEdges> PEList;//<>
	private boolean selfJoinFlag;//if this set can use self join for tuple reduce 
	private boolean skip;//if this set should skip tuple reduce
	
	public PEList(){
		this.PEList = new ArrayList<PatternEdges>();
		this.selfJoinFlag = false;
		this.skip = false;
	}
	
	public void add(PatternEdges pe){
		this.PEList.add(pe);
	}
	
	public ArrayList<PatternEdges> getPEList(){
		return PEList;
	}
	public int size(){
		return PEList.size();
	}

	public void setSelfJoinFlag() {
		this.selfJoinFlag = true;
	}
	
	public void skipTupleReduce(){
		this.skip = true;
	}
	public boolean skip(){
		return skip;
	}
}
