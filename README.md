# CODA:

### 项目结构

`src/main`, `src/model` 部分代码来自于 TED
`CodeGenSrc` 部分代码来自于 FastPat+

### 相关项目

gSpan:
全称：gSpan: Graph-Based Substructure Pattern Mining  
代码：https://github.com/TonyZZX/gSpan.Java

GRAMI:
全称：GRAMI: Frequent Subgraph and Pattern Mining in a Single Large Graph  
代码：https://github.com/ehab-abdelhamid/GraMi

TED+:
全称：Towards Discovering Top-𝑘 Edge-Diversified Patterns in a Graph Database
代码：https://github.com/TechReport2022/TEDProject

FastPat+:
全称：Fast Core-based Top-[k](tex://k) Frequent Pattern Discovery in Knowledge Graphs  
代码：https://github.com/DBGroup-SUSTech/FastPat-KG

### 格式化 Java 代码 (Linux/Mac)

```bash
find ./ -name "*.java" -print0 | xargs -0 java -jar google-java-format-1.24.0-all-deps.jar --replace
```