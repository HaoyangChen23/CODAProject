package BaselineSolution;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import model.*;
import search.SearchLatticeNode;
import search.Searcher;
import utilities.Settings;
import utilities.StopWatch;

public class testMain {
  public static ArrayList<SimpleDFSCode> selectedPatterns;

  public static void main(String[] args) {
    Searcher<Integer, Integer> sr = null;
    StopWatch watch = new StopWatch();
    watch.start();

    String graphFile =
        Settings.datasetsFolder + "graph/" + Settings.fileName; // Knowledge graph file folder
    String coreFile =
        Settings.datasetsFolder
            + "corePattern/"
            + Settings.coreFileName; // Core pattern file folder

    File inFile = new File(graphFile);
    if (Settings.UB && !Settings.HMT) {
      System.out.println("Don't have this solution !!!");
      System.exit(1);
    }
    if (Settings.fileName == null || Settings.coreFileName == null) {
      System.out.println("You have to specify a filename");
      System.exit(1);
    }

    try {
      ////////////////////////////////////////
      // 生成所有满足 MNI 的基于核心模式的图模式 //
      ////////////////////////////////////////
      // Initializations: Graph loading 使用 Searcher 类加载图数据
      sr = new Searcher<Integer, Integer>(graphFile, coreFile);
      // Optimization I: meta index
      if (!Settings.HMT) {
        sr.initialize();
      }
      // Entrance 在图中搜索模式
      ArrayList<SearchLatticeNode<Integer, Integer>> result = sr.search();
      System.out.println("Number of search results: " + result.size());
      // int totalNodes = sr.getKGraph().getNumberOfNodes();

      /////////////////////////////////////////
      // 使用贪心算法选择覆盖率最大的前 K 个图模式 //
      /////////////////////////////////////////
      StopWatch greedySelectionWatch = new StopWatch();
      greedySelectionWatch.start();

      ArrayList<SimpleDFSCode> dfsCodes = DFSCodeConverter.convertResultList(result);

      // 读取输入图
      FileReader reader = new FileReader(inFile);
      Graph TRANS = new Graph();
      BufferedReader br = new BufferedReader(reader);
      TRANS.read(br);

      // 使用贪心选择算法选择前 K 个模式
      GreedySelector gr = new GreedySelector(dfsCodes, TRANS);
      selectedPatterns = gr.selectTopKPatterns(dfsCodes, Settings.k);
      System.out.println("Number of selected patterns: " + selectedPatterns.size());

      // 计算并输出覆盖率
      double coverage = gr.calculateCoverage();

      greedySelectionWatch.start();
      //////////////////////////////
      // 输出结果、统计信息和运行时间 //
      /////////////////////////////
      watch.stop();
      StopWatch genQueryTime = sr.getQueryTime(); // elapsed time for query generation
      double elapsedTime = watch.getElapsedTime() / 1000.0 - genQueryTime.getElapsedTime() / 1000.0;
      DecimalFormat dtest1 = new DecimalFormat("0.000");
      String runningTime = dtest1.format(elapsedTime);
      System.out.println(
          "elapsedTime of baseline solution:" + runningTime); // elapsed time of this algorithm
      // System.out.println("Selected patterns:" + selector.getSelectedPatterns());

      // Output: write txt files 将结果输出到文件
      FileWriter fw;
      // Output file name 输出文件名
      String outfName =
          Settings.coreFileName.substring(0, Settings.coreFileName.length() - 3)
              + "Top"
              + Settings.k
              + ".txt";
      String corePattern = "";
      if (sr.getCorePattern().getPatternSize() != 1) {
        corePattern = sr.getCorePattern().toString();
      }
      try {
        fw = new FileWriter(outfName);
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fw.write("Date:" + df.format(day) + "\n");
        fw.write("runningTime:" + runningTime + "\n");
        fw.write("--------------------- Core Pattern ----------------------" + "\n");
        fw.write(corePattern);
        fw.write("--------------------- Results ----------------------" + "\n");
        fw.write("Top-" + Settings.k + " Results:\n" + result + "\n");
        fw.write("----------------------- Selected Patterns -----------------------\n");
        fw.write("Top-" + Settings.k + " Results:\n");

        if (selectedPatterns.isEmpty()) {
          fw.write("No patterns selected.\n");
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("[");

          for (int i = 0; i < Math.min(selectedPatterns.size(), Settings.k); i++) {
            SimpleDFSCode pattern = selectedPatterns.get(i);
            sb.append(gr.patternToString(pattern));
            if (i < Math.min(selectedPatterns.size(), Settings.k) - 1) {
              sb.append(", ");
            }
          }
          sb.append("]\n");
          fw.write(sb.toString());
        }

        // 写入覆盖率统计信息
        fw.write("\nCoverage Statistics:\n");
        fw.write("Total nodes in graph: " + TRANS.size() + "\n");
        fw.write("Covered nodes: " + gr.getCoveredNodesSize() + "\n");
        fw.write("Coverage ratio: " + String.format("%.2f%%", coverage * 100) + "\n");

        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
