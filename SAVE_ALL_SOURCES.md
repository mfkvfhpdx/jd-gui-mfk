# 保存全部源码（Save All Sources）

本文档说明如何在无界面模式下批量导出反编译源码，功能等价于 JD-GUI 图形界面菜单 **文件 → 保存全部源码**。

## 功能概述

- 将 JAR / WAR / EAR / ZIP 等压缩包或目录中的 `.class` 反编译为 `.java`
- 输出为 ZIP 压缩包（默认文件名为 `<输入文件>.src.zip`）
- 支持命令行调用，也可在 Java 代码中作为工具类使用

## 命令行用法

### 基本语法

```bash
java -jar jd-gui.jar --save-all-sources [--no-metadata] <输入文件> [输出文件]
java -jar jd-gui.jar -s [--no-metadata] <输入文件> [输出文件]
```

| 参数 | 说明 |
|------|------|
| `--save-all-sources` / `-s` | 启用无界面保存全部源码模式 |
| `<输入文件>` | 待反编译的 JAR、WAR、EAR、ZIP、KAR、AAR、JMOD 或目录 |
| `[输出文件]` | 可选，输出 ZIP 路径；省略时默认为 `<输入文件>.src.zip` |
| `--no-metadata` | 可选，**不写入**反编译元数据注释（见下文）；默认会写入 |
| `-h` / `--help` | 显示帮助信息 |

### 示例

```bash
# 导出 app.jar，输出为 app.jar.src.zip
java -jar jd-gui.jar --save-all-sources app.jar

# 指定输出路径
java -jar jd-gui.jar -s app.jar output.src.zip

# 不写入反编译元数据注释
java -jar jd-gui.jar -s --no-metadata app.jar clean-output.src.zip
```

成功时终端输出类似：

```text
已保存 1130 个源文件到 'C:\path\to\app.jar.src.zip'
```

## Java API 用法

核心类：

- `org.jd.gui.util.save.SaveAllSourcesUtil` — 执行导出
- `org.jd.gui.util.save.SaveAllSourcesOptions` — 配置选项

### 最简调用

```java
import org.jd.gui.util.save.SaveAllSourcesUtil;
import java.io.File;

// 默认输出：<输入文件>.src.zip
SaveAllSourcesUtil.Result result = SaveAllSourcesUtil.save(new File("app.jar"));
System.out.println(result.getOutputFile());
System.out.println(result.getFileCount());
```

### 指定输出文件

```java
SaveAllSourcesUtil.save(
    new File("app.jar"),
    new File("output.src.zip")
);
```

### 屏蔽反编译元数据注释

默认情况下，每个导出的 `.java` 文件末尾会附加如下块注释：

```java
/* Location:              C:\path\to\app.jar
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */
```

使用 `omitMetadata(true)` 可去掉上述注释：

```java
import org.jd.gui.util.save.SaveAllSourcesOptions;
import org.jd.gui.util.save.SaveAllSourcesUtil;

SaveAllSourcesOptions options = SaveAllSourcesOptions.defaults()
    .omitMetadata(true);

SaveAllSourcesUtil.save(new File("app.jar"), new File("output.src.zip"), options);
```

### 带进度回调

```java
SaveAllSourcesUtil.save(
    new File("app.jar"),
    new File("output.src.zip"),
    SaveAllSourcesOptions.defaults(),
    path -> System.out.println("saved: " + path)
);
```

### 自定义首选项 Map

如需完全控制反编译行为，可传入首选项 Map（会覆盖配置文件中的对应项）：

```java
import java.util.HashMap;
import java.util.Map;

Map<String, String> preferences = new HashMap<>();
preferences.put("ClassFileSaverPreferences.writeMetadata", "false");
preferences.put("ClassFileSaverPreferences.writeLineNumbers", "false");

SaveAllSourcesUtil.save(new File("app.jar"), new File("output.src.zip"), preferences, null);
```

常用首选项键：

| 键 | 默认值 | 说明 |
|----|--------|------|
| `ClassFileSaverPreferences.writeMetadata` | `true` | 是否写入 Location / 编译器版本 / JD-Core 版本注释 |
| `ClassFileSaverPreferences.writeLineNumbers` | `true` | 是否在源码中写入行号注释 |
| `ClassFileDecompilerPreferences.realignLineNumbers` | `true` | 是否重新对齐行号 |
| `ClassFileDecompilerPreferences.escapeUnicodeCharacters` | `false` | 是否转义 Unicode 字符 |

## 支持的输入类型

| 类型 | 扩展名 / 形式 |
|------|----------------|
| Java 归档 | `.jar` |
| Web 归档 | `.war` |
| 企业归档 | `.ear` |
| 压缩包 | `.zip` |
| OSGi 包 | `.kar` |
| Android 归档 | `.aar` |
| Java 模块 | `.jmod` |
| 目录 | 任意目录路径 |

## 输出说明

- 输出为 ZIP 文件，内部目录结构与原始包一致
- `.class` 文件被替换为对应的 `.java` 反编译源码
- 非 class 资源文件（如 `META-INF/*`、配置文件等）会原样复制
- 内部类（`Foo$Bar.class`）会合并到外部类对应的 `.java` 中，不单独生成文件

## 与图形界面的关系

| 方式 | 入口 |
|------|------|
| 图形界面 | 打开文件后，菜单 **文件 → 保存全部源码** |
| 命令行 | `java -jar jd-gui.jar -s <输入文件>` |
| Java API | `SaveAllSourcesUtil.save(...)` |

三种方式使用相同的反编译引擎（JD-Core）和 `SourceSaver` 导出逻辑。

## 构建

```bash
mvn package -pl app -am -DskipTests
```

生成的可执行 JAR 位于：

```text
app/target/jd-gui-1.6.6.jar
```

## 常见问题

**Q: `--no-metadata` 和 GUI 首选项里的「写入元数据」有什么关系？**

A: 效果相同。`--no-metadata` 等价于将 `ClassFileSaverPreferences.writeMetadata` 设为 `false`。命令行未指定时，会读取 `jd-gui.cfg` 中的配置；API 默认使用配置文件，可通过 `SaveAllSourcesOptions` 或自定义 Map 覆盖。

**Q: 默认会写入哪些注释？**

A: 仅元数据块注释（Location、Java compiler version、JD-Core Version）。行号注释由 `writeLineNumbers` 控制，默认开启，与 `--no-metadata` 无关。

**Q: 能否在 Maven 项目中依赖此工具类？**

A: 工具类位于 `jd-gui-app` 模块。可将构建后的 `jd-gui-*.jar` 加入 classpath，或在本仓库中依赖 `org.jd:jd-gui-app:1.6.6`。
