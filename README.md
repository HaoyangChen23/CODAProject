# CODA:

### 项目结构

### 格式化 Java 代码 (Linux/Mac)

```bash
find ./ -name "*.java" -print0 | xargs -0 java -jar google-java-format-1.24.0-all-deps.jar --replace
```