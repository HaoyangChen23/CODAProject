# 如何管理 Java 运行环境

## 使用 SDKMAN!
SDKMAN! 是一个用于管理多种开发工具的命令行工具，尤其适用于Java生态系统，可以轻松安装、更新、切换和管理软件开发工具，包括不同版本的 JDK（Java Development Kit）

### 安装 SDKMAN!
```bash
curl -s "https://get.sdkman.io" | bash
```

### 列出已安装的 JDK 版本
```bash
sdk list java
```

### 切换到其他 JDK 版本
使用 `sdk use` 命令来切换到其他 JDK 版本，例如 `21.0.5-amzn`
```bash
sdk use java <version>
```

### 安装其他

```bash
sdk install java 21.0.5-amzn
```