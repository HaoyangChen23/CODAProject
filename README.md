# CODAProject

This is our implementation for the paper:

CODA: Towards Discovering Core-based tOp-𝑘 Edge-Diversified pAtterns in a Single Large Graph

CODA is implemented with Java (JDK23).

# Environments

JDK 23

IntelliJ IDEA 2024

# Dataset

|   Dataset   |    Nodes    |     Edge      | EdgeDistinct nodes label |    Type    |           Reference            |
| :---------: | :---------: | :-----------: | :----------------------: | :--------: | :----------------------------: |
|   Flickr    |   80,513    |   5,899,882   |           195            | undirected | https://renchi.ac.cn/datasets/ |
|    DBLP     |   425,957   |   1,049,866   |           100            | undirected | https://renchi.ac.cn/datasets/ |
| LiveJournal |  3,997,962  |  34,681,189   |           100            | undirected | https://renchi.ac.cn/datasets/ |
|   UK-2007   | 105,896,555 | 3,738,733,648 |            -             |  directed  | https://renchi.ac.cn/datasets/ |



# Example to run the codes

We first illustrate how to run CODA with an exapmle.  Then, examples of its variants () are presented.

Let the dataset be Flickr, inputfile "Flickr", outputfile "Flickr_result.txt".

1. CODA 

Step 1: Parameter Setting in main/Arguments.java. 

        1） hyperparameter setting. 
        
             inFilePath  =  "Flickr";  coreFilePath  = "Flickr_core" outFilePath =  "Flickr_result"; minSup  =  1;  swapcondition  = 
             
             "swap1"; maxNodeNum = 11;numberofpatterns = 5;      numberofgraphs   = 40000;  isPESIndex  = true;
             
        2)  parameter setting for optimization strategies
        
             Since all optimization strategies are used by TED,  we set hasPRM = true;  hasDSS = true;  hasInitialPatternGenerator  = true
             
             In addition, we set isLightVersion = false.

Step 2: Run the main class, main/CODAMain.java. The results can be found in the outputfile.

2. CODA's variants

Step 1:   Unless specified otherwise, the parameter settings are the same as above.

         1) PRM:  Set hasInitialPatternGenerator  = false; 2) DSS:  Set hasInitialPatternGenerator  = false and hasPRM = false; 
         
         3) BASE: Set hasInitialPatternGenerator  = false, hasPRM = false, and hasDSS = false;
         
         4) CODALite:  Set isLightVersion = true.

Step 2:  Run the main class, main/CODAMain.java. The results can be found in the outputfile.  
