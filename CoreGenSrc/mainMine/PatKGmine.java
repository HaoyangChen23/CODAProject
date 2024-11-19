package mainMine;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import search.SearchLatticeNode;
import search.Searcher;
import utilities.CommandLineParser;
import utilities.Settings;
import utilities.StopWatch;

/**
 * @author zengjian
 * @date 2020.2.19
 * @version
 * @description 1). Set different optimizations in utilities.Settings 2). Solutions: I) kCP-B:
 *     baseline(tau = minheap); II) kCP-M: meta index III) kCP-U: meta index + UB IV) kCP-A: meta
 *     index + UB + join
 */
public class PatKGmine {

  public static void main(String[] args) {

    // parse the command line arguments
    CommandLineParser.parse(args);
    Searcher<String, String> sr = null;
    StopWatch watch = new StopWatch();
    watch.start();

    String graphFile = Settings.datasetsFolder + Settings.fileName; // knowledge graph file folder
    String coreFile = Settings.datasetsFolder + Settings.coreFileName; // core pattern file folder

    if (Settings.UB && !Settings.HMT) {
      System.out.println("Don't have this solution !!!");
      System.exit(1);
    }
    if (Settings.fileName == null || Settings.coreFileName == null) {
      System.out.println("You have to specify a filename");
      System.exit(1);
    }

    try {
      // Initializations: graph loading
      sr = new Searcher<String, String>(graphFile, coreFile);
      // Optimization I: meta index
      if (!Settings.HMT) {
        sr.initialize();
      }
      // Entrance
      ArrayList<SearchLatticeNode<String, String>> result = sr.search();
      System.out.println("Found " + result.size() + " nodes");

      //			// 使用BaselineProcessor处理结果
      //			BaselineProcessor<String, String> processor =
      //					new BaselineProcessor<>(result, Settings.k, sr.getKGraph());
      //			ArrayList<SearchLatticeNode<String, String>> selectedPatterns = processor.process();
      //
      //			// 输出最终结果
      //			System.out.println("Final Results:");
      //			System.out.println("Selected " + selectedPatterns.size() + " patterns");
      //			System.out.println("Final coverage: " +
      //					String.format("%.2f%%", processor.getFinalCoverage() * 100));
      //
      //			// 输出每个选中的模式
      //			for(int i = 0; i < selectedPatterns.size(); i++) {
      //				System.out.println("Pattern " + (i+1) + ":");
      //				System.out.println(selectedPatterns.get(i));
      //				System.out.println("MNI: " + selectedPatterns.get(i).getMNIfrequency());
      //			}

      watch.stop();
      StopWatch genQueryTime = sr.getQueryTime(); // elapsed time for query generation
      double elapsedTime = watch.getElapsedTime() / 1000.0 - genQueryTime.getElapsedTime() / 1000.0;
      DecimalFormat dtest1 = new DecimalFormat("0.000");
      String runningTime = dtest1.format(elapsedTime);
      System.out.println("elapsedTime of PatKG:" + runningTime); // elapsed time of this algorithm

      // output: write txt files
      FileWriter fw;
      // out put file name
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
        fw.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
