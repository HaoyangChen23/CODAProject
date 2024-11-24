package main;

import java.util.Scanner;

public class Arguments {
	  private static Arguments arguments;
      private String[] args;
      public static String inFilePath  =  "/Users/leonchen/Nutstore Files/CODA_PREPARATION/CODAProject/topKDiversifiedMiner/dataset/AIDS40k";
      public static String labelFilePath  = "/Users/leonchen/Nutstore Files/CODA_PREPARATION/CODAProject/topKDiversifiedMiner/dataset/labels.txt";
      public static String edgeFilePath  = "/Users/leonchen/Nutstore Files/CODA_PREPARATION/CODAProject/topKDiversifiedMiner/dataset/edgelist.txt";
      public static String coreFilePath = "/Users/leonchen/Nutstore Files/CODA_PREPARATION/CODAProject/topKDiversifiedMiner/dataset/AIDS40k_core.txt";
      public static String outFilePath =  "/Users/leonchen/Nutstore Files/CODA_PREPARATION/CODAProject/topKDiversifiedMiner/dataset/result.txt";
     // String inFilePath  =  "pubchem23238"; 
     //  String inFilePath  =  "pubchem1000000clean.txt"; 
     // String outFilePath =  "pubchem23238_result.txt";
     // String inFilePath  =  "emolecul10000"; 
      //  String outFilePath =  "emolecul10000_result.txt";
      public static long minSup        =  1;
      //long minNodeNum = 0;
      //long maxNodeNum = Long.MAX_VALUE;
      
      public static String  swapcondition  = "swap1";  //"swap1", "swap2", "swapalpha"
      public static Double  swapAlpha    = 0.99;
      public static long minNodeNum = 1;
      public static long maxNodeNum = 11;
      String strategy  = "topk";
       // String strategy  = "greedy";
       public static Integer numberofpatterns = 5;
      Integer numberofgraphs   = 40000;

      public static Boolean hasPRM = true;
      public static Boolean isPESIndex  = false;
      public static Boolean isSimpleIndex = !isPESIndex;


      public static boolean hasDSS = true;
      public static Boolean hasInitialPatternGenerator  = true;
      
     // double maintainTime = 0;
      
      Boolean isLightVersion = false;
      Integer ReadNumInEachBatch = 100000;
      Double  AvgE = 43.783167;         ///For Pubchem
      Double  SampleEdgeNum = 500000.0; /// For Pubchem
      

      Boolean isSimplified = false;
      
      
      private Arguments(String[] args) {
          this.args = args;
      }

      static Arguments getInstance(String[] args) {
          arguments = new Arguments(args);
          if (args.length > 0) {
              arguments.initFromCmd();
          } else {
            //arguments.initFromRun();
          }
          
          
          return arguments;
      }
      private void initFromCmd() {

      }
      private void initFromRun() {
          try (Scanner sc = new Scanner(System.in)) {
          	
              System.out.println("Please input the file path of data set: ");
              inFilePath = sc.nextLine();
              
              System.out.println("Please set the minimum support: ");
              minSup = sc.nextLong();
              
              outFilePath = inFilePath + "_result.txt";
          }
      }
}
