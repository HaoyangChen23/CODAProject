// package BaselineSolution;
//
//
// import dataStructures.Frequency;
// import search.*;
// import utilities.CommandLineParser;
// import utilities.Settings;
// import utilities.StopWatch;
//
// import java.io.FileWriter;
// import java.io.IOException;
// import java.text.DecimalFormat;
// import java.text.SimpleDateFormat;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.HashMap;
//
// public class baselinemain {
//    public static void main(String[] args) {
//        // 解析命令行参数
//        //For test
//        String[] hardCodedArgs = {
//
//
//        };
//        CommandLineParser.parse(hardCodedArgs);
////       CommandLineParser.parse(args);
//
//        // 总时间计时
//        StopWatch totalWatch = new StopWatch();
//        totalWatch.start();
//
//        // 设置文件路径
//        String graphFile = Settings.datasetsFolder + Settings.fileName;
//        String coreFile = Settings.datasetsFolder + Settings.coreFileName;
//
//
//        if (Settings.UB && !Settings.HMT) {
//            System.out.println("Don't have this solution !!!");
//            System.exit(1);
//        }
//        if (Settings.fileName == null || Settings.coreFileName == null) {
//            System.out.println("You have to specify a filename");
//            System.exit(1);
//        }
//
//        try {
//            // 加载图和核心模式
//            Searcher<String, String> sr = new Searcher<>(graphFile, coreFile);
//
//            //选择Baseline Solution和设置两种Baseline的minSupport
//            int FSG_FreqSet = sr.getKGraph().getNumberOfNodes();
//
//            double FSG_Ratio = 0.1; // or 0.2-0.5
//            if(Settings.isFSG) Settings.minSupport = (int) (FSG_FreqSet * FSG_Ratio);
//
//            if(Settings.isALL) Settings.minSupport = 1;
//
//            if (!Settings.HMT) {sr.initialize();}
//
//
//
//                //挖掘所有频繁模式计时
//            StopWatch patternMiningWatch = new StopWatch();
//            patternMiningWatch.start();
//            ArrayList<SearchLatticeNode<String, String>> allFrequentPatterns = sr.search();
//            patternMiningWatch.stop();
//
//            //获取大图的总节点数
//            int totalNodes = sr.getKGraph().getNumberOfNodes();
//
//            //选择覆盖率最高的k个模式
//                //贪心挖掘topk计时
//            StopWatch greedySelectionWatch = new StopWatch();
//            greedySelectionWatch.start();
//            BaselineProcessor<String, String> selector =
//                    new BaselineProcessor<>(allFrequentPatterns, Settings.k, sr.getKGraph());
//
////            selector.selectTopKPatterns();
////            System.out.println("Selected Patterns are:" +
////                   selector.getSelectedPatterns());
//            greedySelectionWatch.stop();
//            //计时结束
//            totalWatch.stop();
//            double totalElapsedTime = totalWatch.getElapsedTime() / 1000.0;
//            double patternMiningTime = patternMiningWatch.getElapsedTime() / 1000.0;
//            double greedySelectionTime = greedySelectionWatch.getElapsedTime() / 1000.0;
//
//
//
//            // 输出结果
//            String outfName = Settings.coreFileName.substring(0, Settings.coreFileName.length() -
// 3)
//                    + "Coverage_Top" + Settings.k + ".txt";
//
//            try (FileWriter fw = new FileWriter(outfName)) {
//                // 写入基本信息到指定文件
//                Date day = new Date();
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                fw.write("Date: " + df.format(day) + "\n");
//                fw.write("Running Time: " + totalElapsedTime + "\n");
//                fw.write("Pattern Mining Time: " + patternMiningTime + "\n");
//                fw.write("Greedy Selection Time: " + greedySelectionTime + "\n");
//                fw.write("Total Frequent Patterns Found: " + allFrequentPatterns.size() + "\n");
//
//                // 写入核心模式
//                fw.write("\n--------------------- Core Pattern ----------------------\n");
//                String corePattern = "";
//                if(sr.getCorePattern().getPatternSize() != 1) {
//                    corePattern = sr.getCorePattern().toString();
//                }
//                fw.write(corePattern + "\n");
//
//                // 写入选中的topk模式和覆盖率
//                fw.write("\n--------------------- Selected Patterns ----------------------\n");
//                selector.saveResults(outfName);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // 11. 输出统计信息到console
//            System.out.println("Mining Summary:");
//            System.out.println("Total patterns found: " + allFrequentPatterns.size());
//            System.out.println("Selected patterns: " + selector.getSelectedPatterns().size());
//            System.out.println("Final coverage rate: " +
//                    String.format("%.4f", selector.getCoverageRate()));
//            System.out.println("Results saved to: " + outfName);
//            System.out.println("Total elapsed time: " + totalElapsedTime);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//
//
//    }
//
// }
