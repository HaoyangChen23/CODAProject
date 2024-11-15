package BaselineSolution;

import search.SearchLatticeNode;
import search.Searcher;
import utilities.CommandLineParser;
import utilities.Settings;
import utilities.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class baselinemain {
    public static void main(String[] args) {
        // parse the command line arguments
        CommandLineParser.parse(args);

        if(Settings.isALL){
        Searcher<String, String> sr = null;
        StopWatch watch = new StopWatch();
        watch.start();

        String graphFile = Settings.datasetsFolder + Settings.fileName;//knowledge graph file folder
        String coreFile = Settings.datasetsFolder + Settings.coreFileName;//core pattern file folder

        if (Settings.UB && !Settings.HMT) {
            System.out.println("Don't have this solution !!!");
            System.exit(1);
        }
        if (Settings.fileName == null || Settings.coreFileName == null) {
            System.out.println("You have to specify a filename");
            System.exit(1);
        }

        try {
            //Initializations: graph loading
            sr = new Searcher<String, String>(graphFile, coreFile);
            // Optimization I: meta index
            if (!Settings.HMT) {
                sr.initialize();
            }
            // Entrance
            ArrayList<SearchLatticeNode<String, String>> result = sr.search();

            watch.stop();
            StopWatch genQueryTime = sr.getQueryTime();//elapsed time for query generation
            double elapsedTime = watch.getElapsedTime()/1000.0 - genQueryTime.getElapsedTime()/1000.0;
            DecimalFormat dtest1 = new DecimalFormat("0.000");
            String runningTime = dtest1.format(elapsedTime);
            System.out.println("elapsedTime of PatKG:"+runningTime);//elapsed time of this algorithm

            System.out.println("Now Evalute all the frequent core-based pattern by using CODA_baseline_ALLg" );

            BaselineProcessor br = new BaselineProcessor<>();

            ArrayList<SearchLatticeNode<Integer, Integer>> topKPatterns = new ArrayList<>();

            topKPatterns =  br.selectTopKPatterns(result,Settings.k);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }else if(Settings.isFSG){

            Searcher<String, String> sr = null;
            StopWatch watch = new StopWatch();
            watch.start();

            String graphFile = Settings.datasetsFolder + Settings.fileName;//knowledge graph file folder
            String coreFile = Settings.datasetsFolder + Settings.coreFileName;//core pattern file folder

            if (Settings.UB && !Settings.HMT) {
                System.out.println("Don't have this solution !!!");
                System.exit(1);
            }
            if (Settings.fileName == null || Settings.coreFileName == null) {
                System.out.println("You have to specify a filename");
                System.exit(1);
            }

            try {
                //Initializations: graph loading
                sr = new Searcher<String, String>(graphFile, coreFile);
                // Optimization I: meta index
                if (!Settings.HMT) {
                    sr.initialize();
                }
                // Entrance
                ArrayList<SearchLatticeNode<String, String>> result = sr.search();

                watch.stop();
                StopWatch genQueryTime = sr.getQueryTime();//elapsed time for query generation1
                double elapsedTime = watch.getElapsedTime()/1000.0 - genQueryTime.getElapsedTime()/1000.0;
                DecimalFormat dtest1 = new DecimalFormat("0.000");
                String runningTime = dtest1.format(elapsedTime);
                System.out.println("elapsedTime of PatKG:"+runningTime);//elapsed time of this algorithm

                System.out.println("Now Evalute all the frequent core-based pattern by using CODA_baseline_FSGg" );

                BaselineProcessor br = new BaselineProcessor<>();

                ArrayList<SearchLatticeNode<Integer, Integer>> topKPatterns = new ArrayList<>();

                topKPatterns =  br.selectTopKPatterns(result,Settings.k);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }















        }
            // output: write txt files

//            FileWriter fw;
//            //out put file name
//            String outfName = Settings.coreFileName.substring(0, Settings.coreFileName.length() - 3) +"Top"+Settings.k+ ".txt";
//            String corePattern = "";
//            if(sr.getCorePattern().getPatternSize() != 1) {
//                corePattern = sr.getCorePattern().toString();
//            }
//            try {
//                fw = new FileWriter(outfName);
//                Date day = new Date();
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                fw.write("Date:" + df.format(day) + "\n");
//                fw.write("runningTime:" + runningTime + "\n");
//                fw.write("--------------------- Core Pattern ----------------------" + "\n");
//                fw.write(corePattern);
//                fw.write("--------------------- Results ----------------------" + "\n");
//                fw.write("Top-"+Settings.k+" Results:\n"+result+ "\n");
//                fw.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }




    }

}