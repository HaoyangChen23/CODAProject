
package utilities;

public class Settings {

	// Grami setting
	public static boolean isApproximate = false; // EXACT
	public static double approxEpsilon = 0.0001;
	public static double approxConstant = 100000;
	public static boolean isAutomorphismOn = true;
	public static boolean isDecomposeOn = true;
	public static boolean CACHING = true;
	public static boolean DISTINCTLABELS = true;
	public static boolean LimitedTime = false;
	public static boolean PRINT = false;

	// datasets folder
	public static String datasetsFolder = "D:/Projects/dataSets/";

	// the filename
//	public static String fileName = "Mico.lg";
//	public static String fileName = "Yago.lg";
	public static String fileName = "Oscar.lg";
//	public static String fileName = "Covid.lg";

	// core-pattern file
//	public static String coreFileName = "WCQ3.lg";
//	public static String coreFileName = "OscarQ1.lg";
	public static String coreFileName = "OscarTest.lg";
//	public static String coreFileName = "OscarQ3.lg";
//	public static String coreFileName = "CovidTest.lg";
//	public static String coreFileName = "YagoQ3.lg";

	// parameters
	public static int k = 5;
	public static double t =0.3;
	public static boolean print = false;

	// solution selection
	/*
	 * I) kCP-B: (noHMT+noUB) tau = minheap; 
	 * II) kCP-M: (HMT+noUB) meta index + noUB; 
	 * III) kCP-U: (HMT+UB) meta index + UB; 
	 * IV) kCP-A: (HMT+UBJoin) meta index + UB(+join)
	 */
	public static boolean HMT = true;// meta index
	public static boolean UB = false; // upper for kCP-U
	public static boolean UBJoin = true; // upper for kCP-A

	// Effectiveness I: optimizations in join algorithm
	public static boolean TupleReduce = true;// with lemma 3
	public static boolean greedy = true;// false: select the pattern nodeID:0
	public static boolean earlyTermination = true;

	//For testing two baseline solution of CODA
	public static boolean isALL = false;
	public static boolean isFSG = false;

}
