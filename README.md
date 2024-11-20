## CODA: CODA: Discovering Core-based Top-k Diversified Patterns in a Large Graph

### 项目结构

`src/main`, `src/model` 部分代码来自于 TED<br/>
`CoreGenSrc` 部分代码来自于 FastPat+

### 相关项目

gSpan:<br/>
全称：gSpan: Graph-Based Substructure Pattern Mining<br/>  
代码：https://github.com/TonyZZX/gSpan.Java

GRAMI:<br/>
全称：GRAMI: Frequent Subgraph and Pattern Mining in a Single Large Graph<br/>  
代码：https://github.com/ehab-abdelhamid/GraMi

TED+:<br/>
全称：Towards Discovering Top-𝑘 Edge-Diversified Patterns in a Graph Database<br/>
代码：https://github.com/TechReport2022/TEDProject

FastPat+:<br/>
全称：Fast Core-based Top-[k](tex://k) Frequent Pattern Discovery in Knowledge Graphs<br/>
代码：https://github.com/DBGroup-SUSTech/FastPat-KG

### FastPat+ 使用的数据集

| Dataset   | Nodes   | Distinct node labels | Edges   | Average degree |
|-----------|---------|----------------------|---------|----------------|
| Oscar     | 42K     | 11                   | 63K     | 3.00           |
| WCGoals   | 49K     | 13                   | 158K    | 6.44           |
| MiCo      | 100K    | 29                   | 1.08M   | 21.61          |
| COVID-19  | 103K    | 28                   | 501K    | 9.69           |
| YAGO3     | 1.37M   | 611                  | 2.86M   | 4.15           |

### 格式化 Java 代码 (Linux/Mac)

```bash
find ./ -name "*.java" -print0 | xargs -0 java -jar google-java-format-1.24.0-all-deps.jar --replace
```
