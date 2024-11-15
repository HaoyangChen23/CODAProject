package BaselineSolution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;
import java.util.Map.Entry;

import dataStructures.*;
import main.Arguments;
import main.Common;
import main.Misc;
import search.SearchLatticeNode;

public class BaselineProcessor<NodeType, EdgeType> {
    public ArrayList<SearchLatticeNode<NodeType, EdgeType>> Input;
    private FileWriter os;

    private Graph TRANS;
    private DFSCode DFS_CODE;
    private DFSCode DFS_CODE_IS_MIN;
    private Graph GRAPH_IS_MIN;
    private long ID;
    //private long minSup;
    //private long arg.minNodeNum;
    //private long arg.maxNodeNum;
//    private boolean directed;
    private ArrayList<Graph> allGraphs;
    //    private HashMap<Integer, Set<Integer>> CoveredEdges_OriginalGraphs;
    private HashMap<Integer, Set<Integer>> CoveredEdges_OriginalGraphs;
    private Set<Integer> allCoveredEdges;
    private ArrayList<Set<Integer>> CoveredEdges_EachPattern;
    //// |priv(pattern)|: private edges for each pattern
    private ArrayList<Integer> Priv_pattern;
    ////            rcov:  reverse coverage for each edge
    private HashMap<Integer, Set<Integer>> Rcov_edge;
    ////            |cov|: number of edges covered
    private Integer numberofcovered;
    ////            C_min: minimum pattern
    private Integer minimumpattern_score;
    private Integer minimumpattern_id;
    ////            rpriv:reverse private edges
    private HashMap<Integer, Set<Integer>> Rpriv_i;
    // Single vertex handling stuff [graph][vertexLabel] = count.
    private NavigableMap<Integer, NavigableMap<Integer, Integer>> singleVertex;
    private NavigableMap<Integer, Integer> singleVertexLabel;
    private Arguments arg;

    //////////////// Simple Index////////////////
    ///// [patternid] = set<Integer>, it indicates the edges which are contained by the patternid
    private HashMap<Long, Set<Integer>> CoveredEdges_patterns;
    ///// [edgeid] = set<Integer>, it indicates the patterns that contains the edge with  edgeid
    private HashMap<Integer, Set<Integer>> PatternsID_edges;

    public BaselineProcessor() {
        CoveredEdges_OriginalGraphs = new HashMap<Integer, Set<Integer>>();
        TRANS = new Graph();
        DFS_CODE = new DFSCode();
        DFS_CODE_IS_MIN = new DFSCode();
        GRAPH_IS_MIN = new Graph();
        singleVertex = new TreeMap<>();
        singleVertexLabel = new TreeMap<>();
        /////////////////////////////////////////////////
        //allDFSCodes = new ArrayList<DFSCode>();
        allGraphs = new ArrayList<Graph>();
        allCoveredEdges = new HashSet<Integer>();
        Priv_pattern = new ArrayList<Integer>();
        Rcov_edge    = new HashMap<Integer, Set<Integer>>();
        numberofcovered = 0;
        minimumpattern_score  = -1;
        Rpriv_i = new HashMap<Integer, Set<Integer>>();
        CoveredEdges_EachPattern = new ArrayList<Set<Integer>>();

        CoveredEdges_patterns = new HashMap<Long, Set<Integer>>();
        PatternsID_edges= new HashMap<Integer, Set<Integer>>();
    }

    void run(FileReader reader, FileWriter writers, Arguments arguments) throws IOException {
        os = writers;
        ID = 0;
        arg = arguments;

        read(reader);

        Long Time1 = System.currentTimeMillis();
        if(arg.hasInitialPatternGenerator && !arg.strategy.equals("greedy")) {
            InitialPatternGenerator();
        }
        Long Time2 = System.currentTimeMillis();
        System.out.println("InitialPatternGenerator Time(s) : " + (Time2 - Time1)*1.0 /1000);


        if(!arg.strategy.equals("greedy")) {
            int count = 0;
            for(Graph g : allGraphs) {
                newreport(g, count++);
            }
            System.out.println("TopK, After Swapping, Number of covered edges: " + allCoveredEdges.size());
            int totalegdes = 0;
            TRANS.getEdgeSize();

            System.out.println("totalegdes : " +  totalegdes);
            System.out.println("Coverage rate : " + allCoveredEdges.size()*1.0 / totalegdes);
        }else {
            //int count = 0;
            //for(Graph g : allGraphs) {
            //  	newreport(g, count++);
            //  }

            Set<Integer> coverededges_curall = new HashSet<Integer>();
            Set<Long> selectPatternIndex  	 = new HashSet<Long>();
            int tempcount = 0;
            System.out.println("allGraphs.size():" + allGraphs.size());
            while(tempcount < arg.numberofpatterns) {
                long maxid = -1;
                int maxgain = -1;
                for(int k=0;k<allGraphs.size() ;k++) {
                    long id = (long)k;
                    if(selectPatternIndex.contains(id))  continue;
                    int gain = 0;
                    Set<Integer> converages = CoveredEdges_patterns.get(id);
                    for(Integer a: converages) {
                        if(!coverededges_curall.contains(a)) gain++;
                    }
                    if(gain > maxgain) {
                        maxgain = gain;
                        maxid = id;
                    }

                }

                if(maxgain <0 || maxid < 0) return ;

                selectPatternIndex.add(maxid);

                coverededges_curall.addAll(CoveredEdges_patterns.get(maxid));
                //System.out.println(maxid + " is added, coverededges_curall.size():" + coverededges_curall.size());

                tempcount++;
            }

            int countofpatterns = 0;
            for(long i: selectPatternIndex) {
                Graph g  = allGraphs.get((int) i);
                newreport(g, countofpatterns++);
            }
            int countofcoverededges = 0;
            Set<Integer> temp = new HashSet<Integer>();
            for(long i: selectPatternIndex) {
                temp.addAll(CoveredEdges_patterns.get(i));
            }
            countofcoverededges = temp.size();

            System.out.println("Greedy,  Number of covered edges: " + countofcoverededges);
            int totalegdes = 0;

                totalegdes = TRANS.getEdgeSize();

            System.out.println("totalegdes : " +  totalegdes);
            System.out.println("Coverage rate : " + countofcoverededges*1.0 / totalegdes);
        }
    }

    private void  runIntern() throws IOException {
        // In case 1 node sub-graphs should also be mined for, do this as pre-processing step.
        if ( arg.minNodeNum <= 1) {
            /*
             * Do single node handling, as the normal gSpan DFS code based
             * processing cannot find sub-graphs of size |sub-g|==1. Hence, we
             * find frequent node labels explicitly.
             */
            int id = 0; // 单一大图
                for (int nid = 0; nid < TRANS.size(); ++nid) {
                    int key = TRANS.get(nid).label;
                    //note: if singleVertex.get(id) is null, assign new TreeMap<>() to the key, ie, id
                    singleVertex.computeIfAbsent(id, k -> new TreeMap<>());
                    if (singleVertex.get(id).get(key) == null) {
                        // number of graphs it appears in
                        singleVertexLabel.put(key, Common.getValue(singleVertexLabel.get(key)) + 1);
                    }
                    singleVertex.get(id).put(key, Common.getValue(singleVertex.get(id).get(key)) + 1);
                }

        }
        /*
         * All minimum support node labels are frequent 'sub-graphs'.
         * singleVertexLabel[nodeLabel] gives the number of graphs it appears in.
         */
        for (Entry<Integer, Integer> it : singleVertexLabel.entrySet()) {
            if (it.getValue() < arg.minSup)
                continue;

            int frequent_label = it.getKey();

            // Found a frequent node label, report it.
            Graph g = new Graph();
            Vertex v = new Vertex();
            v.label = frequent_label;
            g.add(v);

            // [graph_id] = count for current substructure
            Vector<Integer> counts = new Vector<>();
            counts.setSize(TRANS.size());
            for (Entry<Integer, NavigableMap<Integer, Integer>> it2 : singleVertex.entrySet()) {
                counts.set(it2.getKey(), it2.getValue().get(frequent_label));
            }

            NavigableMap<Integer, Integer> gyCounts = new TreeMap<>();
            for (int n = 0; n < counts.size(); ++n)
                gyCounts.put(n, counts.get(n));

            reportSingle(g, gyCounts);
        }

        ArrayList<Edge> edges = new ArrayList<>();
        // note: [vertex1.label][eLabel][vertex2.label] = Projected
        NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> root = new TreeMap<>();


            Graph g = TRANS;
            int id = 0;
            for (int from = 0; from < g.size(); ++from) {
                if (Misc.getForwardRoot(g, g.get(from), edges)) {
                    for (Edge it : edges) {
                        int key_1 = g.get(from).label;
                        NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = root.computeIfAbsent(key_1, k -> new TreeMap<>());
                        int key_2 = it.eLabel;
                        NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                        int key_3 = g.get(it.to).label;
                        Projected root_3 = root_2.get(key_3);
                        if (root_3 == null) {
                            root_3 = new Projected();
                            root_2.put(key_3, root_3);
                        }
                        root_3.push(id, it, null);
                    }
                }
            }


        for (Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> fromLabel : root.entrySet()) {
            for (Entry<Integer, NavigableMap<Integer, Projected>> eLabel : fromLabel.getValue().entrySet()) {
                for (Entry<Integer, Projected> toLabel : eLabel.getValue().entrySet()) {
                    // Build the initial two-node graph. It will be grown recursively within project.
                    //note: 0,1, vertex1_label, eLabel, vertex2_label
                    DFS_CODE.push(0, 1, fromLabel.getKey(), eLabel.getKey(), toLabel.getKey());
                    //note: The position of  edge(vertex1_label, eLabel, vertex2_label) occurs in.
                    //It contains Projected object with a set of PDFS where each of them contains an edge in original graph < graph id, the original edge in the graph,  prev = null>
                    project(toLabel.getValue());
                    //System.out.println("allCoveredEdges:" + this.allCoveredEdges.size()+"," + this.allGraphs.size());
                    DFS_CODE.pop();
                }
            }
        }
    }


    public int getLossScore(Set<Integer> dropededges, Long deleteid) {
        int loss_count = 0;

        Set<Integer> set_temp  =  new HashSet<Integer>();
        for (Long key : CoveredEdges_patterns.keySet()) {
            if(key != deleteid)
                set_temp.addAll(CoveredEdges_patterns.get(key));
        }
        Iterator it = dropededges.iterator();
        while(it.hasNext()){
            Integer edgeid  = (Integer)it.next();
            if(!set_temp.contains(edgeid))  {
                loss_count++;
            }
        }
        return loss_count;
    }

    private void read(FileReader is) throws IOException {

        BufferedReader read = new BufferedReader(is);

//      count：计数变量，用于记录读取的图数量。
//	   	BufferedReader read：使用 BufferedReader 包装 FileReader，提高读取效率。

        while (true) {
            Graph g = new Graph();
            read = g.read(read);
            if (g.isEmpty())
                break;
            this.TRANS = g ;
        }
//        创建图对象 g：循环中每次创建一个新的 Graph 对象。
//		g.read(read)：调用 Graph 类的 read 方法，解析文件中的图数据。
//		read 方法返回一个更新后的 BufferedReader，用于读取下一个图。
//		检查图是否为空：如果 g.isEmpty() 返回 true，表示已到文件末尾，停止读取。
//		添加图到 TRANS：将解析后的图对象 g 添加到 TRANS 列表中。
//		更新计数并检查条件：count 自增，如果已达到 arg.numberofgraphs，则跳出循环，停止读取。


        read.close();
//        关闭bufferreader以释放资源
        // System.out.println("total edges: " + num);
    }

    private void  InitialPatternGenerator() throws IOException {

//           初始化 root 结构并遍历图集合
        ArrayList<Edge> edges = new ArrayList<>();
        NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> root = new TreeMap<>();



            Graph g = TRANS;
            int id = 0;
            for (int from = 0; from < g.size(); ++from) {
                if (main.Misc.getForwardRoot(g, g.get(from), edges)) {
                    for (Edge it : edges) {
                        int key_1 = g.get(from).label;
                        NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = root.computeIfAbsent(key_1, k -> new TreeMap<>());
                        int key_2 = it.eLabel;
                        NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                        int key_3 = g.get(it.to).label;
                        Projected root_3 = root_2.get(key_3);
                        if (root_3 == null) {
                            root_3 = new Projected();
                            root_2.put(key_3, root_3);
                        }
                        root_3.push(id, it, null);
                    }
                }
            }

//        root 结构：root 是一个嵌套的映射结构，用于存储每条边的投影信息。
//        遍历图集合：循环 TRANS 中的每个图 g，再遍历图中每个节点 from。
//        Misc.getForwardRoot：获取从当前节点出发的所有边，将结果存储在 edges 中。
//              如果 g 中存在从 from 出发的边，继续处理 edges 中的每条边 it。
//        填充 root 结构：对于每条边 it，根据 from 节点标签、边标签、目标节点标签，将 Projected 对象按层级存储在 root 中。
//               Projected 对象：包含当前图的 ID 和边的相关信息。

        for (Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> fromLabel : root.entrySet()) {
            for (Entry<Integer, NavigableMap<Integer, Projected>> eLabel : fromLabel.getValue().entrySet()) {
                for (Entry<Integer, Projected> toLabel : eLabel.getValue().entrySet()) {
                    DFS_CODE.push(0, 1, fromLabel.getKey(), eLabel.getKey(), toLabel.getKey());
                    project_Initial(toLabel.getValue());
                    DFS_CODE.pop();
                }
            }
        }
//        遍历 root：遍历 root 中的每个三层映射结构，逐层获取 fromLabel（起始节点标签）、eLabel（边标签）、toLabel（目标节点标签）。
//        构建初始 DFS 模式：使用 DFS_CODE.push 方法，将 fromLabel、eLabel 和 toLabel 推入 DFS_CODE，构成初始的模式结构。
//        调用 project_Initial：处理 toLabel 中的投影信息，生成完整模式。
//        DFS_CODE.pop：处理完当前模式后，将该模式从 DFS_CODE 中弹出，准备处理下一个模式。


        System.out.println("After Initial Swapping, Number of covered edges: " + allCoveredEdges.size());
        int totalegdes = 0;

        totalegdes = TRANS.getEdgeSize();

        System.out.println("totalegdes : " +  totalegdes);
        System.out.println("Coverage rate : " + allCoveredEdges.size()*1.0 / totalegdes);
        System.out.println("numberofcovered : " +  numberofcovered);

//        统计覆盖边信息：
//        allCoveredEdges.size()：输出已覆盖的边数量。
//        totalegdes：计算图集合中所有图的总边数。
//        Coverage rate：计算覆盖率，即 allCoveredEdges.size() 除以总边数。
//        numberofcovered：输出被覆盖的独特边数。


    }

    private void newreport(Graph g, int id) throws IOException {
        // Filter to small/too large graphs.
        int sup = 0;
        int ID = id;

//        int sup = 0;：支持度 sup 被初始化为 0，表明在此方法中没有实际计算支持度值。
//        int ID = id;：将传入的 id 赋值给 ID，用于记录模式的标识。
        //  Graph g = new Graph(directed);
        //  code.toGraph(g);
        os.write("Final t # " + ID + " * " + sup + System.getProperty("line.separator"));
//        将模式的基本信息写入输出流 os
        g.write(os);
//        调用 g 的 write 方法，将图的详细信息写入 os 输出流中。
        ++ID;
    }
    private void newreportbeforeswap(Graph g, int id) throws IOException {
        // Filter to small/too large graphs.
        int sup = 0;
        int ID = id;
        //  Graph g = new Graph(directed);
        //  code.toGraph(g);
        os.write("BeforeSwap t # " + ID + " * " + sup + System.getProperty("line.separator"));
        g.write(os);
        ++ID;
    }


    public int getBenefitScore(Set<Integer> coverededges) {
        int unique_count = 0;
        Iterator it = coverededges.iterator();
        while(it.hasNext()){
            if(allCoveredEdges.contains(it.next())) continue;
            unique_count++;
        }
        return unique_count;
    }


    public void Delete(int deleteid) {
        //Long Time1 = System.currentTimeMillis();
        //if(numberofcovered != allCoveredEdges.size())
        //{System.out.println("error!!!!!!!!!!!!"); return;}
        //System.out.println("##########delete##############");

        //allGraphs.remove(deleteid);
        allGraphs.set(deleteid, null);

        Rpriv_i.get(Priv_pattern.get(deleteid)).remove(deleteid);



        Set<Integer> coverededges_pattern = CoveredEdges_EachPattern.get(deleteid);

        for(Integer e: coverededges_pattern) {

            Rcov_edge.get(e).remove(deleteid);

            if(Rcov_edge.get(e).size() == 0) {
                numberofcovered--;

                allCoveredEdges.remove(e);

                //CoveredEdges_OriginalGraphs.get(e/1000).remove(e);

            }else if (Rcov_edge.get(e).size() == 1) {
                int tempid = Rcov_edge.get(e).iterator().next();
                //System.out.println("tempid: " + tempid);
                int temp = Priv_pattern.get(tempid);
                Priv_pattern.set(tempid,  temp+1);
                Rpriv_i.get(temp).remove(tempid);
                if(Rpriv_i.get(temp+1) ==null) Rpriv_i.put(temp+1,  new HashSet<Integer>());
                Rpriv_i.get(temp+1).add(tempid);
            }
        }



        // update  CoveredEdges_OriginalGraphs
        CoveredEdges_OriginalGraphs.clear();
        for(Integer edgeid : allCoveredEdges) {
//            Integer gid = edgeid / 1000;
            Set<Integer>  temp = CoveredEdges_OriginalGraphs.get(0);
            if(temp == null) temp = new  HashSet<Integer>();
            temp.add(edgeid);
            CoveredEdges_OriginalGraphs.put(0, temp);
        }




        //Priv_pattern.remove(deleteid);
        Priv_pattern.set(deleteid, -1);

        //CoveredEdges_EachPattern.remove(deleteid);
        CoveredEdges_EachPattern.set(deleteid, new HashSet<Integer>());

        // System.out.println("**************");
        // System.out.println("Rcov_edge: "+ Rcov_edge.toString());
        // System.out.println("Priv_pattern: "+  Priv_pattern.toString());
        // System.out.println("Rpriv_i: "+   Rpriv_i.toString());
        //  System.out.println("numberofcovered: "+ numberofcovered);
        //  System.out.println("minimumpattern_id: "+ minimumpattern_id);
        //  System.out.println("minimumpattern_score: "+ minimumpattern_score);
        // 	Long Time2 = System.currentTimeMillis();
        //	arg.maintainTime += (Time2 - Time1)*1.0 /1000;
    }


    public void Insert(Projected projected, int insertid) {
        //Long Time1 = System.currentTimeMillis();
        //System.out.println("Insert");
        Graph g = new Graph();
        DFS_CODE.toGraph(g);
        if(allGraphs.size() < arg.numberofpatterns) allGraphs.add(g);
        else allGraphs.set(insertid, g);

        Set<Integer> coverededges_pattern = new HashSet<Integer>();
        if(Priv_pattern.size() < arg.numberofpatterns) Priv_pattern.add(0);
        else Priv_pattern.set(insertid,  0);

        for (PDFS aProjected : projected) {
            int id = aProjected.id;
            //System.out.println("id:" + id);
            Set<Integer> tempedges = new HashSet<Integer>();
            for (PDFS p = aProjected; p != null; p = p.prev) {
                Integer temp = 1000 * id + p.edge.id;
                coverededges_pattern.add(temp);
                tempedges.add(temp);
                allCoveredEdges.add(temp);
            }
            if(CoveredEdges_OriginalGraphs.containsKey(id)) {
                tempedges.addAll(CoveredEdges_OriginalGraphs.get(id));
                CoveredEdges_OriginalGraphs.replace(id, tempedges);
            }else {
                CoveredEdges_OriginalGraphs.put(id, tempedges);
            }

        }

        if(CoveredEdges_EachPattern.size() < arg.numberofpatterns) CoveredEdges_EachPattern.add(coverededges_pattern);
        else CoveredEdges_EachPattern.set(insertid, coverededges_pattern);
        for (int temp : coverededges_pattern ) {
            if(Rcov_edge.get(temp) ==null) Rcov_edge.put(temp, new HashSet<Integer>());
            Rcov_edge.get(temp).add(insertid);
            if(Rcov_edge.get(temp).size() == 1) {
                //edgenum++;
                Priv_pattern.set(insertid,  Priv_pattern.get(insertid)+1);
                numberofcovered++;
            }else if(Rcov_edge.get(temp).size() == 2) {
                int tempid = -1;
                for(int e : Rcov_edge.get(temp)) if(e != insertid)  tempid = e;
                Priv_pattern.set(tempid,  Priv_pattern.get(tempid)-1);

                Rpriv_i.get(Priv_pattern.get(tempid)+1).remove(tempid);
                if(Rpriv_i.get(Priv_pattern.get(tempid)) ==null) Rpriv_i.put(Priv_pattern.get(tempid),  new HashSet<Integer>());
                Rpriv_i.get(Priv_pattern.get(tempid)).add(tempid);
            }
        }
        // System.out.println("%%%%%Priv_pattern: "+  Priv_pattern.toString());
        //System.out.println("%%%%%Rpriv_i: "+   Rpriv_i.toString());
        //Priv_pattern.add(edgenum);
        if(Rpriv_i.get(Priv_pattern.get(insertid)) ==null) Rpriv_i.put(Priv_pattern.get(insertid),  new HashSet<Integer>());
        Rpriv_i.get(Priv_pattern.get(insertid)).add(insertid);
        for(int i=0;i<= Priv_pattern.get(insertid);i++) {
            if(Rpriv_i.get(i) != null && Rpriv_i.get(i).size()>0) {
                //System.out.println(Priv_pattern.get(insertid)+","+i+ "," + Rpriv_i.get(i).size());
                minimumpattern_id = Rpriv_i.get(i).iterator().next();
                minimumpattern_score =  Priv_pattern.get(minimumpattern_id);
                break;
            }
        }
        //System.out.println("############");
        // System.out.println("Rcov_edge: "+ Rcov_edge.toString());
        // System.out.println("Priv_pattern: "+  Priv_pattern.toString());
        // System.out.println("Rpriv_i: "+   Rpriv_i.toString());
        // System.out.println("numberofcovered: "+ numberofcovered);
        // System.out.println("minimumpattern_id: "+ minimumpattern_id);
        // System.out.println("minimumpattern_score: "+ minimumpattern_score);
        //++ID;
        // System.out.println("allCoveredEdges.size(): " + allCoveredEdges.size());

        //Long Time2 = System.currentTimeMillis();
        //arg.maintainTime += (Time2 - Time1)*1.0 /1000;
    }

    public void InsertWithSimpleIndex(Projected projected, int insertid) {
        Graph g = new Graph();
        DFS_CODE.toGraph(g);
        if(allGraphs.size() < arg.numberofpatterns) allGraphs.add(g);
            //else allGraphs.set(insertid, g);
        else if(allGraphs.size() > insertid) allGraphs.set(insertid, g);
        else allGraphs.add(g);
        Set<Integer> coverededges_pattern = new HashSet<Integer>();
        for (PDFS aProjected : projected) {
            int id = aProjected.id;
            Set<Integer> tempedges = new HashSet<Integer>();
            for (PDFS p = aProjected; p != null; p = p.prev) {
               Integer temp = 1000 * id + p.edge.id;

                coverededges_pattern.add(temp);

//                更新 PatternsID_edges：记录每条边被哪些模式覆盖
                if(PatternsID_edges.containsKey(temp)) {
                    Set<Integer> temppatternids = PatternsID_edges.get(temp);
                    temppatternids.add(id);
                    PatternsID_edges.replace(temp, temppatternids);
                }else {
                    Set<Integer> temppatternids =new HashSet<Integer>();
                    temppatternids.add(id);
                    PatternsID_edges.put(temp, temppatternids);
                }
                allCoveredEdges.add(temp);
                tempedges.add(temp);
            }
            if(CoveredEdges_OriginalGraphs.containsKey(id)) {
                tempedges.addAll(CoveredEdges_OriginalGraphs.get(id));
                CoveredEdges_OriginalGraphs.replace(id, tempedges);
            }else {
                CoveredEdges_OriginalGraphs.put(id, tempedges);
            }




        }
        CoveredEdges_patterns.put((long) insertid, coverededges_pattern);

        //System.out.println("allCoveredEdges Size: " + allCoveredEdges.size());
    }

    private void reportSingle(Graph g, NavigableMap<Integer, Integer> nCount) throws IOException {
        int sup = 0;

        // note: total occurrences, [graph] = nCount
        for (Entry<Integer, Integer> it : nCount.entrySet()) {
            sup += Common.getValue(it.getValue());
        }

        if ( arg.maxNodeNum  > arg.minNodeNum && g.size() > arg.maxNodeNum)
            return;
        if (arg.minNodeNum > 0 && g.size() < arg.minNodeNum)
            return;

        os.write("t # " + ID + " * " + sup + System.getProperty("line.separator"));
        g.write(os);
        ID++;
    }

    public  Boolean reportwithCoveredEdges(int sup, Projected projected) throws IOException {
        // Filter to small/too large graphs.
        if (arg.maxNodeNum > arg.minNodeNum && DFS_CODE.countNode() > arg.maxNodeNum)
            return false;
        if (arg.minNodeNum > 0 && DFS_CODE.countNode() < arg.minNodeNum )
            return false;
        //////////////////////////////////
        if( arg.strategy.equals("greedy") || allGraphs.size() <  arg.numberofpatterns) {

            if(arg.isPESIndex) Insert(projected, allGraphs.size());
            else if(arg.isSimpleIndex)  InsertWithSimpleIndex(projected, allGraphs.size()) ;

            if(allGraphs.size()  == arg.numberofpatterns) {
                int count = 0;
                for(Graph tempg : allGraphs) {
                    newreportbeforeswap(tempg, count++);
                }
                System.out.println("Before Swapping, Number of covered edges: " + allCoveredEdges.size());
                int totalegdes = 0;
                for(int i=0;i< TRANS.size();i++) {
                    totalegdes += TRANS.getEdgeSize();
                }
                System.out.println("totalegdes : " +  totalegdes);
                System.out.println("Coverage rate : " + allCoveredEdges.size()*1.0 / totalegdes);

                if(arg.isSimpleIndex && arg.strategy.equals("topk")) {
                    int patternid_min = 0;
                    int loss_score_min = Integer.MAX_VALUE;
                    for (Long key : CoveredEdges_patterns.keySet()) {
                        Set<Integer> dropededges =  CoveredEdges_patterns.get(key);
                        int loss_score = getLossScore(dropededges, key);
                        if(loss_score < loss_score_min) {
                            loss_score_min  =  loss_score;
                            patternid_min = key.intValue();
                        }
                    }
                    minimumpattern_score  =  loss_score_min;
                    minimumpattern_id = patternid_min;

                    //System.out.println("minimumpattern_score: " + minimumpattern_score);
                    //System.out.println("minimumpattern_id: " + minimumpattern_id);
                }
            }
        }
        else {
            /// 1. calculate benefit score
            int benefit_score = 0;
            //// 2. calculate minimum loss score
            int patternid_min = -1;
            int loss_score_min = Integer.MAX_VALUE;

            if(arg.isPESIndex) {
                Set<Integer> coverededges_pattern = new HashSet<Integer>();
                for (PDFS aProjected : projected) {
                    int id = aProjected.id;
                    for (PDFS p = aProjected; p != null; p = p.prev) {
                        Integer temp = 1000 * id + p.edge.id;
                        coverededges_pattern.add(temp);
                    }
                }
                for(Integer e : coverededges_pattern) {
                    if( this.Rcov_edge.get(e) == null || this.Rcov_edge.get(e).size() == 0) {
                        benefit_score++;
                    }
                }
                patternid_min = minimumpattern_id;
                loss_score_min = minimumpattern_score;

                Boolean swapflag = false;
                if(arg.swapcondition.equals("swap1")) {
                    if(benefit_score > 2* loss_score_min ) {
                        swapflag = true;
                    }
                }else  if(arg.swapcondition.equals("swap2")) {
                    if(benefit_score > loss_score_min  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                        swapflag = true;
                    }
                }else {
                    if(benefit_score > (1+arg.swapAlpha)*loss_score_min  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                        swapflag = true;
                    }
                }
                if(swapflag) {
                    os.write(patternid_min + " is swapped out!");
                    os.write("(Swapping Phase) benefit_score: " + benefit_score + ", loss_score_min: " + loss_score_min + "\\n");
                    allGraphs.get(patternid_min).write(os);

                    Delete(patternid_min);
                    Insert(projected, patternid_min);


                }
                return swapflag;
            }else if(arg.isSimpleIndex) {
                Set<Integer> coverededges_pattern = new HashSet<Integer>();
                for (PDFS aProjected : projected) {
                    int id = aProjected.id;
                    for (PDFS p = aProjected; p != null; p = p.prev) {
                        Integer temp = 1000 * id + p.edge.id;
                        coverededges_pattern.add(temp);
                    }
                }
                benefit_score = getBenefitScore(coverededges_pattern);

                loss_score_min  =  minimumpattern_score;
                patternid_min   =  minimumpattern_id ;

                Boolean swapflag = false;
                if(arg.swapcondition.equals("swap1")) {
                    if(benefit_score > 2* loss_score_min ) {
                        swapflag = true;
                    }
                }else  if(arg.swapcondition.equals("swap2")) {
                    if(benefit_score > loss_score_min  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                        swapflag = true;
                    }
                }else {
                    if(benefit_score > (1+arg.swapAlpha)*loss_score_min  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                        swapflag = true;
                    }
                }
                if(swapflag) {
                    os.write(patternid_min + " is swapped out!");
                    os.write("(Swapping Phase) benefit_score: " + benefit_score + ", loss_score_min: " + loss_score_min );
                    allGraphs.get(patternid_min).write(os);

                    // update allAllGraphs
                    Graph g = new Graph();
                    DFS_CODE.toGraph(g);
                    allGraphs.set(patternid_min, g);


                    // update CoveredEdges_patterns
                    CoveredEdges_patterns.replace((long) patternid_min, coverededges_pattern);

                    // update allCoveredEdges
                    allCoveredEdges.clear();
                    for (Long key : CoveredEdges_patterns.keySet()) {
                        Set<Integer> temp = CoveredEdges_patterns.get(key);
                        allCoveredEdges.addAll(temp);
                    }

                    // update  CoveredEdges_OriginalGraphs
                    CoveredEdges_OriginalGraphs.clear();
                    for(Integer edgeid : allCoveredEdges) {
//                        Integer gid = edgeid / 1000;
                        Set<Integer>  temp = CoveredEdges_OriginalGraphs.get(0);
                        if(temp == null) temp = new  HashSet<Integer>();
                        temp.add(edgeid);
                        CoveredEdges_OriginalGraphs.put(0, temp);
                    }


                    // update PatternsID_edges
                    PatternsID_edges.clear();
                    for(Integer edgeid : allCoveredEdges) {
                        Set<Integer> tempedges =  new HashSet<Integer>();
                        for (Long key : CoveredEdges_patterns.keySet()) {
                            Set<Integer> temp = CoveredEdges_patterns.get(key);
                            if(temp.contains(edgeid)) {
                                tempedges.add(key.intValue());
                            }
                        }
                        PatternsID_edges.put(edgeid, tempedges);
                    }

                    //System.out.println("allCoveredEdges Size: " + allCoveredEdges.size());
                    if(true) {
                        patternid_min = 0;
                        loss_score_min = Integer.MAX_VALUE;
                        for (Long key : CoveredEdges_patterns.keySet()) {
                            Set<Integer> dropededges =  CoveredEdges_patterns.get(key);
                            int loss_score =  getLossScore(dropededges, key);
                            if(loss_score < loss_score_min) {
                                loss_score_min  =  loss_score;
                                patternid_min = key.intValue();
                            }
                        }
                        minimumpattern_score  =  loss_score_min;
                        minimumpattern_id = patternid_min;

                        //System.out.println("minimumpattern_score: " + minimumpattern_score);
                        //System.out.println("minimumpattern_id: " + minimumpattern_id);
                    }
                }
                return swapflag;
            }else {

            }
        }
        return false;
    }



    public  void reportwithCoveredEdges_Initial(int sup, Projected projected) throws IOException {
        // Filter to small/too large graphs.
        if (arg.maxNodeNum > arg.minNodeNum && DFS_CODE.countNode() > arg.maxNodeNum)
            return;
        if (arg.minNodeNum > 0 && DFS_CODE.countNode() < arg.minNodeNum)
            return;
//        模式大小过滤：如果当前模式节点数超过 maxNodeNum 或小于 minNodeNum，则忽略该模式。

//      处理初始模式集合
        if(allGraphs.size() <  arg.numberofpatterns) {

            Graph g = new Graph();
            DFS_CODE.toGraph(g);
            os.write("Initial****t # " + allGraphs.size() + " * " + sup + System.getProperty("line.separator"));
            g.write(os);

//		创建图对象 g：将 DFS_CODE 转换为图对象。
//		输出初始模式信息：将模式信息写入 os 输出流，方便记录当前支持度和模式 ID。

//      插入新模式
            if(arg.isPESIndex) Insert(projected, allGraphs.size());
            else if(arg.isSimpleIndex)  InsertWithSimpleIndex(projected, allGraphs.size()) ;
            else ;
            //System.out.println("allCoveredEdges.size(): " + allCoveredEdges.size());

//      检查是否达到最大模式数并报告覆盖信息
            if(allGraphs.size()  == arg.numberofpatterns) {
                int count = 0;
                for(Graph tempg : allGraphs) {
                    newreportbeforeswap(tempg, count++);
                }
                System.out.println("Before Swapping, Number of covered edges: " + allCoveredEdges.size());

//      报告覆盖信息：当模式集合达到 numberofpatterns 阈值时，报告所有模式的初始覆盖情况，并计算覆盖率。

//      在 SimpleIndex 模式下选择损失最小的模式
                int totalegdes = 0;

                totalegdes = TRANS.getEdgeSize();

                System.out.println("totalegdes : " +  totalegdes);
                System.out.println("Coverage rate : " + allCoveredEdges.size()*1.0 / totalegdes);

                if(arg.isSimpleIndex && arg.strategy.equals("topk")) {
                    int patternid_min = 0;
                    int loss_score_min = Integer.MAX_VALUE;
                    for (Long key : CoveredEdges_patterns.keySet()) {
                        Set<Integer> dropededges =  CoveredEdges_patterns.get(key);
                        int loss_score = getLossScore(dropededges, key);
                        if(loss_score < loss_score_min) {
                            loss_score_min  =  loss_score;
                            patternid_min = key.intValue();
                        }
                    }
                    minimumpattern_score  =  loss_score_min;
                    minimumpattern_id = patternid_min;

//                 最小损失分数模式：
//                 在 SimpleIndex 模式下，计算每个模式的损失分数，选择损失分数最小的模式作为候选，用于可能的替换。
                }
            }
        }
        else {
//            计算收益分数并决定是否替换
//            在 PESIndex 和 SimpleIndex 模式下分别处理：
//            PESIndex 模式下的替换逻辑
            /// 1. calculate benefit score
            int benefit_score = 0;
            //// 2. calculate minimum loss score
            int patternid_min = -1;
            int loss_score_min = Integer.MAX_VALUE;

            if(arg.isPESIndex) {
                Set<Integer> coverededges_pattern = new HashSet<Integer>();
                for (PDFS aProjected : projected) {
                    int id = aProjected.id;
                    for (PDFS p = aProjected; p != null; p = p.prev) {
                        Integer temp = 1000 * id + p.edge.id;
                        coverededges_pattern.add(temp);
                    }
                }
                for(Integer e : coverededges_pattern) {
                    if( this.Rcov_edge.get(e) == null || this.Rcov_edge.get(e).size() == 0) {
                        benefit_score++;
                    }
                }
                patternid_min = minimumpattern_id;
                loss_score_min = minimumpattern_score;
//             收益分数计算：遍历 projected 中的覆盖边，计算该模式带来的新增覆盖边数 benefit_score。

//             判断替换条件
                Boolean swapflag = false;
                if(arg.swapcondition.equals("swap1")) {
                    if(benefit_score > 2* loss_score_min ) {
                        swapflag = true;
                    }
                }else  if(arg.swapcondition.equals("swap2")) {
                    if(benefit_score > loss_score_min  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                        swapflag = true;
                    }
                }else {
                    if(benefit_score > (1+arg.swapAlpha)*loss_score_min  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                        swapflag = true;
                    }
                }
//             交换条件：
//             根据 swapcondition 的值，计算不同的替换条件。
//             如果 benefit_score 超过某个条件阈值，则将 swapflag 设置为 true，表示需要执行替换。

                if(swapflag) {
                    os.write(patternid_min + " is swapped out!");
                    os.write("Initial Swapping, benefit_score: " + benefit_score + ", loss_score_min: " + loss_score_min );
                    allGraphs.get(patternid_min).write(os);

                    Delete(patternid_min);
                    Insert(projected, patternid_min);
                }

//             执行替换：如果满足替换条件，写入日志信息，并调用 Delete 和 Insert 方法删除旧模式并插入新模式。


//                 SimpleIndex 模式下的替换逻辑:
//                 与 PESIndex 类似，区别在于 SimpleIndex 使用 getBenefitScore 计算收益分数，并更新覆盖边集合。
            }else if(arg.isSimpleIndex) {
                Set<Integer> coverededges_pattern = new HashSet<Integer>();
                for (PDFS aProjected : projected) {
                    int id = aProjected.id;
                    for (PDFS p = aProjected; p != null; p = p.prev) {
                        Integer temp = 1000 * id + p.edge.id;
                        coverededges_pattern.add(temp);
                    }
                }
                benefit_score = getBenefitScore(coverededges_pattern);

                loss_score_min  = minimumpattern_score ;
                patternid_min   = minimumpattern_id ;

                Boolean swapflag = false;
                if(arg.swapcondition.equals("swap1")) {
                    if(benefit_score > 2* loss_score_min ) {
                        swapflag = true;
                    }
                }else  if(arg.swapcondition.equals("swap2")) {
                    if(benefit_score > loss_score_min  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                        swapflag = true;
                    }
                }else {
                    if(benefit_score > (1+arg.swapAlpha)*loss_score_min  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                        swapflag = true;
                    }
                }
                if(swapflag) {
                    os.write(patternid_min + " is swapped out!");
                    os.write("Initial Swapping, benefit_score: " + benefit_score + ", loss_score_min: " + loss_score_min );
                    allGraphs.get(patternid_min).write(os);

                    // update allAllGraphs
                    Graph g = new Graph();
                    DFS_CODE.toGraph(g);
                    allGraphs.set(patternid_min, g);


                    // update CoveredEdges_patterns
                    CoveredEdges_patterns.replace((long) patternid_min, coverededges_pattern);

                    // update allCoveredEdges
                    allCoveredEdges.clear();
                    for (Long key : CoveredEdges_patterns.keySet()) {
                        Set<Integer> temp = CoveredEdges_patterns.get(key);
                        allCoveredEdges.addAll(temp);
                    }


                    // update  CoveredEdges_OriginalGraphs
                    CoveredEdges_OriginalGraphs.clear();
                    for(Integer edgeid : allCoveredEdges) {
//                        Integer gid = edgeid / 1000;
                        Set<Integer>  temp = CoveredEdges_OriginalGraphs.get(0);
                        if(temp == null) temp = new  HashSet<Integer>();
                        temp.add(edgeid);
                        CoveredEdges_OriginalGraphs.put(0, temp);
                    }


                    // update PatternsID_edges
                    PatternsID_edges.clear();
                    for(Integer edgeid : allCoveredEdges) {
                        Set<Integer> tempedges =  new HashSet<Integer>();
                        for (Long key : CoveredEdges_patterns.keySet()) {
                            Set<Integer> temp = CoveredEdges_patterns.get(key);
                            if(temp.contains(edgeid)) {
                                tempedges.add(key.intValue());
                            }
                        }
                        PatternsID_edges.put(edgeid, tempedges);
                    }


                    if(true) {
                        patternid_min = 0;
                        loss_score_min = Integer.MAX_VALUE;
                        for (Long key : CoveredEdges_patterns.keySet()) {
                            Set<Integer> dropededges =  CoveredEdges_patterns.get(key);
                            int loss_score =  getLossScore(dropededges, key);
                            if(loss_score < loss_score_min) {
                                loss_score_min  =  loss_score;
                                patternid_min = key.intValue();
                            }
                        }
                        minimumpattern_score  =  loss_score_min;
                        minimumpattern_id = patternid_min;
                    }
                }
            }else {

            }
        }
    }



    private Integer getBenefitScore_Initial(Projected projected) {
        //reportwithCoveredEdges_Initial(sup, projected);

//        构建当前模式的覆盖边集合 coverededges_pattern

        Set<Integer> coverededges_pattern = new HashSet<Integer>();
        for (PDFS aProjected : projected) {
            int id = aProjected.id;
            for (PDFS p = aProjected; p != null; p = p.prev) {
               Integer temp = 1000 * id + p.edge.id;
                coverededges_pattern.add(temp);
            }
        }
//        遍历 projected：获取每个 PDFS 对象的 id 和对应的边信息。
//        对于每条边，计算唯一标识符 temp（通过 id * 1000 + p.edge.id 生成），并将其添加到 coverededges_pattern 集合中。
//        结果：coverededges_pattern 包含了当前模式所覆盖的所有边的集合。

        int benefitscore = getBenefitScore(coverededges_pattern);
        return benefitscore;

//        调用 getBenefitScore：传入 coverededges_pattern 集合，计算该模式的收益分数。getBenefitScore 方法会计算 coverededges_pattern 中未被其他模式覆盖的边数。
//        返回收益分数：最终返回 benefitscore，表示当前模式带来的新增边覆盖的贡献。


    }
    private void project(Projected projected) throws IOException {
        //Recursive sub-graph mining function (similar to sub-procedure 1 Sub-graph_Mining in [Yan2002]).
        //Check if the pattern is frequent enough.
        int sup = support(projected);
        if (sup < arg.minSup)
            return;
        if (!isMin()) {
            return;
        }
        // Output the frequent substructure
        //  report(sup);
        Boolean hasupdated = reportwithCoveredEdges(sup, projected);
        //if(hasupdated) System.out.println("out");
        if(arg.maxNodeNum <= 2) return;


        //if(hasupdated) System.out.println("true"); else System.out.println("false");

        if(arg.hasDSS && hasupdated) {
            // global pruning
            arg.minSup = DynamicSupportSetting();
        }

        if (arg.maxNodeNum > arg.minNodeNum && DFS_CODE.countNode() > arg.maxNodeNum)
            return;

        /*
         * We just outputted a frequent sub-graph. As it is frequent enough, so
         * might be its (n+1)-extension-graphs, hence we enumerate them all.
         */
        ArrayList<Integer> rmPath = DFS_CODE.buildRMPath();
        DFS minDFS = (DFS)DFS_CODE.getDFSList().get(0);
        int minLabel = minDFS.fromLabel;
        DFS maxDFS = (DFS)DFS_CODE.getDFSList().get(rmPath.get(0));
        int maxToc = maxDFS.to;


        //note:  1. new_bck_root[to_vertex][eLabel] =  Projected, since from_vertex is fixed as maxToc and labels of two end nodes are known.
        //                 (Reason: for rightmost extension, backward edges can only be inserted when from_vertex is rightmost vertex)
        //            2. new_fwd_root[from_vertex][eLabel][to_vertex_label], since to_vertex is fixed as maxToc+1 and label of from_vertex is known.
        //                 (Reason: for rightmost extension, forward edges can  be inserted with any nodes in the rightmost path)

        NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> new_fwd_root = new TreeMap<>();
        NavigableMap<Integer, NavigableMap<Integer, Projected>> new_bck_root = new TreeMap<>();
        ArrayList<Edge> edges = new ArrayList<>();

        // Enumerate all possible one edge extensions of the current substructure.
        for (PDFS aProjected : projected) {

            int id = aProjected.id;
            History history = new History(TRANS, aProjected);

            // XXX: do we have to change something here for directed edges?


            // backward
            for (int i = rmPath.size() - 1; i >= 1; --i) {

                //note: e1 = history.get(rmPath.get(i)),  e2 = history.get(rmPath.get(0)), check if there is an edge between e2.to and e1.from, and this edge is not already in history
                //           if yes, choose this edge as a backward edge
                Edge e = Misc.getBackward(TRANS, history.get(rmPath.get(i)), history.get(rmPath.get(0)),history);
                if (e != null) {
                    DFS key1 = (DFS)DFS_CODE.getDFSList().get(rmPath.get(i));
                    int key_1 = key1.from;
                    NavigableMap<Integer, Projected> root_1 = new_bck_root.computeIfAbsent(key_1, k -> new TreeMap<>());
                    int key_2 = e.eLabel;
                    Projected root_2 = root_1.get(key_2);
                    if (root_2 == null) {
                        root_2 = new Projected();
                        root_1.put(key_2, root_2);
                    }
                    //note:  the new Projected root_2 has a pointer to the previous Projected, aProjected
                    root_2.push(id, e, aProjected);
                }
            }

            // pure forward
            // FIXME: here we pass a too large e.to (== history[rmPath[0]].to
            // into getForwardPure, such that the assertion fails.
            //
            // The problem is:
            // history[rmPath[0]].to > TRANS[id].size()
            if (Misc.getForwardPure(TRANS, history.get(rmPath.get(0)), minLabel, history, edges))
                for (Edge it : edges) {
                    NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = new_fwd_root.computeIfAbsent(maxToc, k -> new TreeMap<>());
                    int key_2 = it.eLabel;
                    NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                    int key_3 = TRANS.get(it.to).label;
                    Projected root_3 = root_2.get(key_3);
                    if (root_3 == null) {
                        root_3 = new Projected();
                        root_2.put(key_3, root_3);
                    }
                    root_3.push(id, it, aProjected);
                }
            // backtracked forward
            for (Integer aRmPath : rmPath)
                if (Misc.getForwardRmPath(TRANS, history.get(aRmPath), minLabel, history, edges))
                    for (Edge it : edges) {
                        DFS key1 = (DFS)DFS_CODE.getDFSList().get(aRmPath);
                        int key_1 = key1.from;
                        NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = new_fwd_root.computeIfAbsent(key_1, k -> new TreeMap<>());
                        int key_2 = it.eLabel;
                        NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                        int key_3 = TRANS.get(it.to).label;
                        Projected root_3 = root_2.get(key_3);
                        if (root_3 == null) {
                            root_3 = new Projected();
                            root_2.put(key_3, root_3);
                        }
                        root_3.push(id, it, aProjected);
                    }
        }

        // Test all extended substructures.
        // backward
        for (Entry<Integer, NavigableMap<Integer, Projected>> to : new_bck_root.entrySet()) {
            for (Entry<Integer, Projected> eLabel : to.getValue().entrySet()) {
                if(arg.hasPRM  && this.allGraphs.size() == arg.numberofpatterns)
                {
                    if(BranchAndBound(projected, eLabel.getValue(),hasupdated)) {
                        continue;
                    }
                }
                DFS_CODE.push(maxToc, to.getKey(), -1, eLabel.getKey(), -1);
                project(eLabel.getValue());
                DFS_CODE.pop();
            }
        }

        //note:  There are many forward edges, so they should be visited descendingly resp.  from_vertex.
        //            e.g., let rightmost path be (1,2,4), then forward edges should be, (4,5), (2,5) and (1,5)
        // forward
        for (Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> from : new_fwd_root.descendingMap().entrySet()) {
            for (Entry<Integer, NavigableMap<Integer, Projected>> eLabel : from.getValue().entrySet()) {
                for (Entry<Integer, Projected> toLabel : eLabel.getValue().entrySet()) {
                    if(arg.hasPRM  && this.allGraphs.size() == arg.numberofpatterns)
                    {
                        if(BranchAndBound(projected, toLabel.getValue(),hasupdated)) {
                            continue;
                        }
                    }
                    DFS_CODE.push(from.getKey(), maxToc + 1, -1, eLabel.getKey(), toLabel.getKey());
                    project(toLabel.getValue());
                    DFS_CODE.pop();
                }
            }
        }
    }





    private void project_Initial(Projected projected) throws IOException {
        //int sup = support(projected);

        //if(allDFSCodes.size() >=  numberofpatterns) {
        //	return ;
        //}

        //if (!isMin()) {
        //System.out.println("not minimum, number of nodes" +DFS_CODE.countNode()) ;
        // return;
        //}

        //reportwithCoveredEdges_Initial(sup, projected);

//        计算当前模式的收益分数
        int CurrentBenefitScore = getBenefitScore_Initial(projected);

        if(arg.maxNodeNum <= 2) return;
        if (arg.maxNodeNum > arg.minNodeNum && DFS_CODE.countNode() > arg.maxNodeNum)  return;
//        收益分数：调用 getBenefitScore_Initial 方法，计算当前模式的覆盖收益分数 CurrentBenefitScore。
//        模式大小限制：如果 maxNodeNum <= 2，则不进行扩展。如果当前模式节点数超过 maxNodeNum，也会提前返回。


        ArrayList<Integer> rmPath = DFS_CODE.buildRMPath();
        DFS minDFS = (DFS) DFS_CODE.getDFSList().get(0);
        int minLabel = minDFS.fromLabel;

        DFS maxDFS = (DFS) DFS_CODE.getDFSList().get(rmPath.get(0));
        int maxToc = maxDFS.to;
        NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> new_fwd_root = new TreeMap<>();
        NavigableMap<Integer, NavigableMap<Integer, Projected>> new_bck_root = new TreeMap<>();
        ArrayList<Edge> edges = new ArrayList<>();

//        构建右侧路径和初始化扩展结构
//              rmPath：生成当前模式的右侧路径，用于定义可能的扩展。
//              new_fwd_root 和 new_bck_root：用于存储前向扩展和后向扩展的投影信息。
//              edges：用于临时存储每次扩展产生的边集合。

        // Enumerate all possible one edge extensions of the current substructure.
//        遍历 projected 中的投影，生成前向和后向扩展
        for (PDFS aProjected : projected) {
            int id = aProjected.id;
            History history = new History(TRANS, aProjected);
            // backward
            for (int i = rmPath.size() - 1; i >= 1; --i) {
                //note: e1 = history.get(rmPath.get(i)),  e2 = history.get(rmPath.get(0)), check if there is an edge between e2.to and e1.from, and this edge is not already in history
                //           if yes, choose this edge as a backward edge
                Edge e = main.Misc.getBackward(TRANS,history.get(rmPath.get(i)), history.get(rmPath.get(0)),history);
                if (e != null) {
                    DFS key1 = (DFS)DFS_CODE.getDFSList().get(i);
                    int key_1 = key1.from;

                    NavigableMap<Integer, Projected> root_1 = new_bck_root.computeIfAbsent(key_1, k -> new TreeMap<>());
                    int key_2 = e.eLabel;
                    Projected root_2 = root_1.get(key_2);
                    if (root_2 == null) {
                        root_2 = new Projected();
                        root_1.put(key_2, root_2);
                    }
                    //note:  the new Projected root_2 has a pointer to the previous Projected, aProjected
                    root_2.push(id, e, aProjected);
                }
            }
            // pure forward
            // FIXME: here we pass a too large e.to (== history[rmPath[0]].to
            // into getForwardPure, such that the assertion fails.
            //
            // The problem is:
            // history[rmPath[0]].to > TRANS[id].size()
            if (main.Misc.getForwardPure(TRANS, history.get(rmPath.get(0)), minLabel, history, edges))
                for (Edge it : edges) {
                    NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = new_fwd_root.computeIfAbsent(maxToc, k -> new TreeMap<>());
                    int key_2 = it.eLabel;
                    NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                    int key_3 = TRANS.get(it.to).label;
                    Projected root_3 = root_2.get(key_3);
                    if (root_3 == null) {
                        root_3 = new Projected();
                        root_2.put(key_3, root_3);
                    }
                    root_3.push(id, it, aProjected);
                }
            // backtracked forward
            for (Integer aRmPath : rmPath)
                if (main.Misc.getForwardRmPath(TRANS, history.get(aRmPath), minLabel, history, edges))
                    for (Edge it : edges) {

                        DFS key1 = (DFS)DFS_CODE.getDFSList().get(aRmPath);
                        int key_1 = key1.from;

                        NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = new_fwd_root.computeIfAbsent(key_1, k -> new TreeMap<>());
                        int key_2 = it.eLabel;
                        NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                        int key_3 = TRANS.get(it.to).label;
                        Projected root_3 = root_2.get(key_3);
                        if (root_3 == null) {
                            root_3 = new Projected();
                            root_2.put(key_3, root_3);
                        }
                        root_3.push(id, it, aProjected);
                    }
        }

//        后向扩展：对于右侧路径中的每个节点，使用 Misc.getBackward 方法找到合适的后向边。
//        前向扩展：调用 Misc.getForwardPure 方法获取右侧路径的扩展边，并存储到 new_fwd_root 中。
//        回溯前向扩展：调用 Misc.getForwardRmPath 获取可与右侧路径任意节点连接的前向边，生成新的投影。


//        计算每个扩展的收益分数并选择最佳扩展
        int Benefitscore_max_backward = -1;
        Integer maxToc_max_backward = 0;
        Integer index1_max_backward = 0;
        Integer eLabel_max_backward = 0;
        Projected projected_max_backward = null;


        // Test all extended substructures.
        // backward
        for (Entry<Integer, NavigableMap<Integer, Projected>> to : new_bck_root.entrySet()) {
            for (Entry<Integer, Projected> eLabel : to.getValue().entrySet()) {

                int benefitscore =  getBenefitScore_Initial(eLabel.getValue());
                //System.out.println("benefitscore: " +benefitscore);
                if(benefitscore >= Benefitscore_max_backward) {
                    Benefitscore_max_backward = benefitscore;
                    maxToc_max_backward = maxToc;
                    index1_max_backward = to.getKey();
                    eLabel_max_backward = eLabel.getKey();
                    projected_max_backward = eLabel.getValue();
                }
                //DFS_CODE.push(maxToc, to.getKey(), -1, eLabel.getKey(), -1);
                //project_Initial(eLabel.getValue());
                //DFS_CODE.pop();
            }
        }


        int Benefitscore_max_forward = -1;
        Integer from_max_forward = 0;
        Integer maxToc_max_forward = 0;
        Integer index1_max_forward = 0;
        Integer eLabel_max_forward = 0;
        Projected projected_max_forward = null;
        //note:  There are many forward edges, so they should be visited descendingly resp.  from_vertex.
        //            e.g., let rightmost path be (1,2,4), then forward edges should be, (4,5), (2,5) and (1,5)
        // forward
        for (Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> from : new_fwd_root.descendingMap().entrySet()) {
            for (Entry<Integer, NavigableMap<Integer, Projected>> eLabel : from.getValue().entrySet()) {
                for (Entry<Integer, Projected> toLabel : eLabel.getValue().entrySet()) {

                    int benefitscore =  getBenefitScore_Initial(toLabel.getValue());
                    if(benefitscore>=Benefitscore_max_forward) {
                        Benefitscore_max_forward = benefitscore;
                        from_max_forward   = from.getKey();
                        maxToc_max_forward = maxToc + 1;
                        index1_max_forward = eLabel.getKey();
                        eLabel_max_forward = toLabel.getKey();
                        projected_max_forward = toLabel.getValue();
                    }
                    //DFS_CODE.push(from.getKey(), maxToc + 1, -1, eLabel.getKey(), toLabel.getKey());
                    // project(toLabel.getValue());
                    // DFS_CODE.pop();
                }
            }
        }
        //System.out.println("CurrentBenefitScore, Benefitscore_max_forward, Benefitscore_max_backward: "+ CurrentBenefitScore + "," + Benefitscore_max_forward + "," + Benefitscore_max_backward);


//        比较当前模式与最佳扩展模式的收益，决定是否扩展或报告
        if(Benefitscore_max_forward >= CurrentBenefitScore && Benefitscore_max_forward>= Benefitscore_max_backward) {
            //System.out.println("here1");
            DFS_CODE.push(from_max_forward, maxToc_max_forward, -1, index1_max_forward, eLabel_max_forward);
            project_Initial(projected_max_forward);
            DFS_CODE.pop();
        }else if(Benefitscore_max_backward >= CurrentBenefitScore &&  Benefitscore_max_backward >= Benefitscore_max_forward) {
            //System.out.println("here2");
            DFS_CODE.push(maxToc_max_backward, index1_max_backward, -1, eLabel_max_backward, -1);
            project_Initial(projected_max_backward);
            DFS_CODE.pop();
        }else {
            //System.out.println("here3");
            //System.out.println("CurrentBenefitScore, Benefitscore_max_forward, Benefitscore_max_backward: "+ CurrentBenefitScore + "," + Benefitscore_max_forward + "," + Benefitscore_max_backward);
            reportwithCoveredEdges_Initial(0, projected);
            return ;
        }
//        扩展策略：如果最佳的前向或后向扩展的收益分数高于当前模式的 CurrentBenefitScore，则将对应扩展推入 DFS_CODE，并递归调用 project_Initial 进行进一步扩展。
//        报告模式：如果没有扩展的收益分数高于当前模式，则调用 reportwithCoveredEdges_Initial 方法记录当前模式。


    }
    private int support(Projected projected) {
        int oid = 0xffffffff;
        int size = 0;

        for (PDFS cur : projected) {
            if (oid != cur.id) {
                ++size;
            }
            oid = cur.id;
        }

        return size;
    }
    private boolean isMin() {

        if (DFS_CODE.getDFSList().size() == 1)
            return (true);

        DFS_CODE.toGraph(GRAPH_IS_MIN);
        DFS_CODE_IS_MIN.getDFSList().clear();

        // note: [vertex1.label][eLabel][vertex2.label] = Projected
        NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> root = new TreeMap<>();
        ArrayList<Edge> edges = new ArrayList<>();

        for (int from = 0; from < GRAPH_IS_MIN.size(); ++from)
            if (Misc.getForwardRoot(GRAPH_IS_MIN, GRAPH_IS_MIN.get(from), edges))
                for (Edge it : edges) {
                    int key_1 = GRAPH_IS_MIN.get(from).label;
                    NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = root.computeIfAbsent(key_1, k -> new TreeMap<>());
                    int key_2 = it.eLabel;
                    NavigableMap<Integer, Projected> root_2 = root_1.computeIfAbsent(key_2, k -> new TreeMap<>());
                    int key_3 = GRAPH_IS_MIN.get(it.to).label;
                    Projected root_3 = root_2.get(key_3);
                    if (root_3 == null) {
                        root_3 = new Projected();
                        root_2.put(key_3, root_3);
                    }
                    // note: [vertex1.label][eLabel][vertex2.label] = Projected, but here the graph id is fixed as 0, since Projected is only for GRAPH_IS_MIN
                    root_3.push(0, it, null);
                }

        Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> fromLabel = root.firstEntry();
        Entry<Integer, NavigableMap<Integer, Projected>> eLabel = fromLabel.getValue().firstEntry();
        Entry<Integer, Projected> toLabel = eLabel.getValue().firstEntry();
        // note: select the minimum edge as the as first DFS of DFS_CODE_IS_MIN
        DFS_CODE_IS_MIN.push(0, 1, fromLabel.getKey(), eLabel.getKey(), toLabel.getKey());

        return isMinProject(toLabel.getValue());
    }

    ///// note: similar to the project function
    private boolean isMinProject(Projected projected) {
        //note: rmPath contains the edges (DFS) in the rightmost path. [i] = i-th DFS.  It is generated from the last DFS to the first DFS
        ArrayList<Integer> rmPath = DFS_CODE_IS_MIN.buildRMPath();

        //note: fromlabel of first DFS in  DFS_CODE_IS_MIN
        DFS minDFS = (DFS)DFS_CODE_IS_MIN.getDFSList().get(0);
        int minLabel = minDFS.fromLabel;

        //note: rightmost vertex in  DFS_CODE_IS_MIN
        DFS maxDFS = (DFS)DFS_CODE_IS_MIN.getDFSList().get(rmPath.get(0));
        int maxToc = maxDFS.to;

        {
            NavigableMap<Integer, Projected> root = new TreeMap<>();
            boolean flg = false;
            int newTo = 0;

            for (int i = rmPath.size() - 1; !flg && i >= 1; --i) {
                for (PDFS cur : projected) {
                    History history = new History(GRAPH_IS_MIN, cur);
                    Edge e = Misc.getBackward(GRAPH_IS_MIN, history.get(rmPath.get(i)), history.get(rmPath.get(0)),
                            history);
                    if (e != null) {
                        int key_1 = e.eLabel;
                        Projected root_1 = root.get(key_1);
                        if (root_1 == null) {
                            root_1 = new Projected();
                            root.put(key_1, root_1);
                        }
                        root_1.push(0, e, cur);
                        DFS newTo_DFS = (DFS)DFS_CODE_IS_MIN.getDFSList().get(rmPath.get(i));
                        newTo = newTo_DFS.from;
                        flg = true;
                    }
                }
            }

            if (flg) {
                Entry<Integer, Projected> eLabel = root.firstEntry();
//                Change here
                DFS_CODE_IS_MIN.push(maxToc, newTo, -1, eLabel.getKey(), -1);
                DFS GenCode = (DFS)DFS_CODE.getDFSList().get(DFS_CODE_IS_MIN.getDFSList().size() - 1);
                DFS CurrentCode = (DFS)DFS_CODE_IS_MIN.getDFSList().get(DFS_CODE_IS_MIN.getDFSList().size() - 1);
                if (GenCode.notEqual(CurrentCode))
                    return false;
                return isMinProject(eLabel.getValue());
            }
        }

        {
            boolean flg = false;
            int newFrom = 0;
            NavigableMap<Integer, NavigableMap<Integer, Projected>> root = new TreeMap<>();
            ArrayList<Edge> edges = new ArrayList<>();

            for (PDFS cur : projected) {
                History history = new History(GRAPH_IS_MIN, cur);
                if (Misc.getForwardPure(GRAPH_IS_MIN, history.get(rmPath.get(0)), minLabel, history, edges)) {
                    flg = true;
                    newFrom = maxToc;
                    for (Edge it : edges) {
                        int key_1 = it.eLabel;
                        NavigableMap<Integer, Projected> root_1 = root.computeIfAbsent(key_1, k -> new TreeMap<>());
                        int key_2 = GRAPH_IS_MIN.get(it.to).label;
                        Projected root_2 = root_1.get(key_2);
                        if (root_2 == null) {
                            root_2 = new Projected();
                            root_1.put(key_2, root_2);
                        }
                        root_2.push(0, it, cur);
                    }
                }
            }

            for (int i = 0; !flg && i < rmPath.size(); ++i) {
                for (PDFS cur : projected) {
                    History history = new History(GRAPH_IS_MIN, cur);
                    if (Misc.getForwardRmPath(GRAPH_IS_MIN, history.get(rmPath.get(i)), minLabel, history, edges)) {
                        flg = true;
                        DFS newFrom_DFS = (DFS)DFS_CODE_IS_MIN.getDFSList().get(rmPath.get(i));
                        newFrom = newFrom_DFS.from;
                        for (Edge it : edges) {
                            int key_1 = it.eLabel;
                            NavigableMap<Integer, Projected> root_1 = root.computeIfAbsent(key_1, k -> new TreeMap<>());
                            int key_2 = GRAPH_IS_MIN.get(it.to).label;
                            Projected root_2 = root_1.get(key_2);
                            if (root_2 == null) {
                                root_2 = new Projected();
                                root_1.put(key_2, root_2);
                            }
                            root_2.push(0, it, cur);
                        }
                    }
                }
            }

            if (flg) {
                Entry<Integer, NavigableMap<Integer, Projected>> eLabel = root.firstEntry();
                Entry<Integer, Projected> toLabel = eLabel.getValue().firstEntry();
                DFS_CODE_IS_MIN.push(newFrom, maxToc + 1, -1, eLabel.getKey(), toLabel.getKey());

                DFS GenCode = (DFS)DFS_CODE.getDFSList().get(DFS_CODE_IS_MIN.getDFSList().size() - 1);
                DFS CurrentCode = (DFS)DFS_CODE_IS_MIN.getDFSList().get(DFS_CODE_IS_MIN.getDFSList().size() - 1);

                if (GenCode.notEqual(CurrentCode))
                    return false;
                return isMinProject(toLabel.getValue());
            }
        }

        return true;
    }

    int DynamicSupportSetting() {
        //// calculate minimum loss score
        int loss_score_min =   this.minimumpattern_score;
        List<Integer> list = new ArrayList<Integer>();

//      loss_score_min：将 minimumpattern_score 的值赋予 loss_score_min，作为后续计算的基准。
//		list：初始化一个整数列表，用于存储每个图中满足条件的边数量。

            int id = 0;
            int count = 0;
            for (int nid = 0; nid < TRANS.size(); ++nid) {
                for(Edge e : TRANS.get(nid).edge) {
                    Integer edgeid = e.id;
                    if(this.Rcov_edge.get(edgeid)==null || this.Rcov_edge.get(edgeid).size()==0) continue;
                    count++;
                }
            }
            list.add(count);


//        外层循环：遍历 TRANS 中的每个图。
//		count 变量：用于统计当前图中满足条件的边数量。
//		判断条件：对于每条边 e，检查 Rcov_edge 中是否存在边 edgeid 的信息。如果 Rcov_edge.get(edgeid) 为空，说明该边没有被任何模式覆盖，跳过计数；否则，增加 count。
//		将计数结果添加到 list：完成当前图的边统计后，将 count 添加到 list。


        Collections.sort(list);
        int sum = 0;
        int index = list.size() -1;
        while(sum < 2 * loss_score_min) {
            sum += list.get(index);
            index--;
        }
//     排序并计算累积和，以动态确定支持度:
//        排序：将 list 按照边数量从小到大排序。
//		累积和：从 list 的最大值开始向前累加，直到累积和 sum 大于或等于 2 * loss_score_min。这种方式确保选取的是满足一定覆盖率的最大边集合。


        int ans = 1;

        if((list.size() - 1 - index) > ans)
            ans = list.size() - 1 - index;
        if(ans <= (int)arg.minSup)  { ans = (int)arg.minSup; }
        //else {  System.out.println("DSS got better ans( > minSup): "+ ans) ; }
        //System.out.println("DSS: "+ ans + ", loss_score_min: " + loss_score_min);
        return ans;
    }

//    根据累积和结果动态设置 minSup:
//      ans：默认设置为 1，用于存储动态支持度。
//		更新 ans：如果 (list.size() - 1 - index) 的值大于 ans，则更新 ans，确保覆盖更多边的图数量。
//		确保支持度下限：如果 ans 小于 arg.minSup，则将 ans 设为 minSup，防止动态调整后的支持度低于初始支持度。
//		返回结果：最终返回 ans 作为动态调整后的最小支持度。

    Boolean BranchAndBound(Projected projected_g, Projected  projected_g2, Boolean hasupdated) {
        //System.out.println("test");
        //if(true) return false;
        if(hasupdated) {
            int maximum_benefit = 0;
            Set<Integer> temp  = new HashSet<Integer>();
            for (PDFS aProjected : projected_g2) {
                int id = aProjected.id;
                if(temp.contains(id)==false) {
                    // E_i
                    int size = this.TRANS.getEdgeSize();
                    // Cov_i
                    int count = 0;
                    for(int i=0;i<size;i++) {
                        Integer edgeid = 1000*id + i;
                        //System.out.println("ss: " + id);
                       if(CoveredEdges_OriginalGraphs.get(id) != null && CoveredEdges_OriginalGraphs.get(id).contains(edgeid)) count++;
                    }
                    maximum_benefit = maximum_benefit + size - count;

                    // if(maximum_benefit > 2*this.minimumpattern_score) {
                    //	   return false;
                    //   }
                    if(arg.swapcondition.equals("swap1")) {
                        if(maximum_benefit > 2* minimumpattern_score ) {
                            return false;
                        }
                    }else  if(arg.swapcondition.equals("swap2")) {
                        if(maximum_benefit > minimumpattern_score  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                            return false;
                        }
                    }else {
                        if(maximum_benefit > (1+arg.swapAlpha)*minimumpattern_score  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                            return false;
                        }
                    }

                    temp.add(id);
                }
            }
            //System.out.println("test11111");
            return true;
        }
        else {
            //System.out.println("test");
            if(true) {
                int maximum_benefit = 0;
                Set<Integer> temp  = new HashSet<Integer>();
                for (PDFS aProjected : projected_g2) {
                    int id = aProjected.id;
                    if(temp.contains(id)==false) {
                        // E_i
                        int size = this.TRANS.getEdgeSize();
                        // Cov_i
                        int count = 0;
                        if(CoveredEdges_OriginalGraphs.get(id) !=null) {
                            for(int i=0;i<size;i++) {
                                Integer edgeid = 1000*id + i;
                                if(CoveredEdges_OriginalGraphs.get(id).contains(edgeid)) count++;
                            }
                        }
                        maximum_benefit = maximum_benefit + size - count;
                        temp.add(id);
                    }
                }
                //if(maximum_benefit <= 2*this.minimumpattern_score)  {
                //	return true;
                //}

                if(arg.swapcondition.equals("swap1")) {
                    if(maximum_benefit <= 2* minimumpattern_score ) {
                        return true;
                    }
                }else  if(arg.swapcondition.equals("swap2")) {
                    if(maximum_benefit <= minimumpattern_score  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                        return true;
                    }
                }else {
                    if(maximum_benefit <= (1+arg.swapAlpha)*minimumpattern_score  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                        return true;
                    }
                }
            }


            int maximum_benefit = 0;
            int totaledges = 0;
//            Set<Integer> graphIDs = new HashSet<Integer>();
            Set<Integer> Cov_g  = new HashSet<Integer>();
            Set<Integer> Cov_g2  = new HashSet<Integer>();
            Set<Integer> Cov_i  = new HashSet<Integer>();

            //calculate Cov_g2
            for (PDFS aProjected : projected_g2) {
                int id = aProjected.id;
                for (PDFS p = aProjected; p != null; p = p.prev) {
                    Integer temp = 1000 * id + p.edge.id;
                    Cov_g2.add(temp);
                }
//                graphIDs.add(id);
            }

            //calculate Cov_g
            for (PDFS aProjected : projected_g) {
                int id = aProjected.id;
//                if(graphIDs.contains(id) == false) continue;
                for (PDFS p = aProjected; p != null; p = p.prev) {
                    Integer temp = 1000 * id + p.edge.id;
                    Cov_g.add(temp);
                }
            }

            //calculate Cov_i
                int id = 0;
                totaledges += this.TRANS.getEdgeSize();
                //for(int e: allCoveredEdges) if(e >= 1000*id && e < 1000*(id+1)) Cov_i.add(e);
//                if(CoveredEdges_OriginalGraphs.get(id) == null) continue;
                Cov_i.addAll(CoveredEdges_OriginalGraphs.get(id));


            // Cov_diff = Cov(g) \ (Cov(g2) U Cov(g))
            Set<Integer> Cov_diff = new HashSet<Integer>();
            for(Integer e: Cov_g) {
                if(Cov_g2.contains(e) == false && Cov_i.contains(e) == false) {
                    Cov_diff.add(e);
                }
            }

            // Cov_i U Cov_diff
            Set<Integer> Cov_union = Cov_i;
            Cov_union.addAll(Cov_diff);

            maximum_benefit =  totaledges - Cov_union.size();

            //  if(maximum_benefit > 2*this.minimumpattern_score) {
            //	   return false;
            //  }

            if(arg.swapcondition.equals("swap1")) {
                if(maximum_benefit > 2* minimumpattern_score ) {
                    return false;
                }
            }else  if(arg.swapcondition.equals("swap2")) {
                if(maximum_benefit > minimumpattern_score  + this.allCoveredEdges.size()*1.0/arg.numberofpatterns) {
                    return false;
                }
            }else {
                if(maximum_benefit > (1+arg.swapAlpha)*minimumpattern_score  + (1-arg.swapAlpha)*(this.allCoveredEdges.size()*1.0/arg.numberofpatterns)) {
                    return false;
                }
            }

            return true;
        }
    }
    public ArrayList<SearchLatticeNode<NodeType, EdgeType>> selectTopKPatterns(
            ArrayList<SearchLatticeNode<NodeType, EdgeType>> patterns, int k) {

        // 用于存储最终的前 k 个模式
        ArrayList<SearchLatticeNode<NodeType, EdgeType>> topKPatterns = new ArrayList<>();

        for (SearchLatticeNode<NodeType, EdgeType> pattern : patterns) {
            Set<Integer> coveredEdges = getCoveredEdges(pattern);

            // 计算收益分数
            int benefitScore = getBenefitScore(coveredEdges);

            if (topKPatterns.size() < k) {
                // 插入模式，未超过 k 个时不需要替换
                insertPatternWithScore(topKPatterns, pattern, coveredEdges);
            } else {
                // 超过 k 个模式时，计算替换条件
                int minLossScore = Integer.MAX_VALUE;
                int replaceIndex = -1;

                for (int i = 0; i < topKPatterns.size(); i++) {
                    Set<Integer> currentCoveredEdges = getCoveredEdges(topKPatterns.get(i));
                    int lossScore = getLossScore(currentCoveredEdges, (long) i);  // 模拟替换效果

                    // 找出当前模式集中损失最小的模式进行替换
                    if (lossScore < minLossScore) {
                        minLossScore = lossScore;
                        replaceIndex = i;
                    }
                }

                // 判断是否替换当前的模式
                if (benefitScore > minLossScore) {
                    deletePattern(topKPatterns, replaceIndex);
                    insertPatternWithScore(topKPatterns, pattern, coveredEdges);
                }
            }
        }
        return topKPatterns;

    }
    private void insertPatternWithScore(ArrayList<SearchLatticeNode<NodeType, EdgeType>> patterns,
                                        SearchLatticeNode<NodeType, EdgeType> pattern,
                                        Set<Integer> coveredEdges) {

        Projected projected = new Projected();
        for (Integer edgeId : coveredEdges) {

            Edge<NodeType, EdgeType> edge = new Edge<>();
            edge.id = edgeId;

            PDFS pdfs = new PDFS();
            pdfs.edge = edge;
            projected.add(pdfs);
        }

        InsertWithSimpleIndex(projected, patterns.size());
        patterns.add(pattern);
    }

    private void deletePattern(ArrayList<SearchLatticeNode<NodeType, EdgeType>> patterns, int index) {
        // 删除模式并更新覆盖边
        Delete(index);  // 使用 Delete 方法
        patterns.set(index, null);
    }

    private Set<Integer> getCoveredEdges(SearchLatticeNode<NodeType, EdgeType> pattern) {
        Set<Integer> coveredEdges = new HashSet<>();
        // 遍历模式并获取每条边，加入到 coveredEdges
        HPListGraph<NodeType, EdgeType> graph = pattern.getHPlistGraph();

        // 使用 edgeIndexIterator 遍历所有有效边的索引
        IntIterator edgeIterator = graph.edgeIndexIterator();
        while (edgeIterator.hasNext()) {
            int edgeIdx = edgeIterator.next();
            coveredEdges.add(edgeIdx);
        }
        return coveredEdges;
    }







}
