package joinAlgorithm;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public abstract class ComparatorPair implements Comparator{
	@Override
	public int compare(Object o1, Object o2) {
		int[] p1 = (int[])o1;
		int[] p2 = (int[])o2;
		return p1[0] - p2[1];
	}
}
