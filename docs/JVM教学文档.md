# JVM 教学文档

## 目录
1. [JVM 概述](#1-jvm-概述)
2. [JVM 内存模型](#2-jvm-内存模型)
3. [JVM 垃圾回收机制](#3-jvm-垃圾回收机制)
4. [代码示例解析](#4-代码示例解析)
5. [运行指南](#5-运行指南)
6. [常见问题](#6-常见问题)

---

## 1. JVM 概述

### 1.1 什么是 JVM？
JVM（Java Virtual Machine，Java虚拟机）是Java技术的核心和基础，它是一个虚拟出来的计算机，通过在实际的计算机上仿真模拟各种计算机功能来实现的。

### 1.2 JVM 的主要功能
- **类加载**：负责加载.class文件
- **字节码校验**：确保加载的类文件是安全的
- **解释和编译**：将字节码转换为机器码执行
- **内存管理**：自动分配和回收内存
- **垃圾回收**：自动回收不再使用的对象

### 1.3 JVM 的组成结构
```
┌─────────────────────────────────────┐
│           类加载子系统              │
├─────────────────────────────────────┤
│         运行时数据区                │
│  ┌──────────┬──────────┬─────────┐  │
│  │   方法区 │    堆    │   栈    │  │
│  └──────────┴──────────┴─────────┘  │
├─────────────────────────────────────┤
│         执行引擎                    │
│  ┌──────────┬──────────┬─────────┐  │
│  │  解释器  │ JIT编译器│   GC    │  │
│  └──────────┴──────────┴─────────┘  │
├─────────────────────────────────────┤
│         本地接口   (JNI)            │
└─────────────────────────────────────┘
```

---

## 2. JVM 内存模型

### 2.1 运行时数据区概述
JVM在执行Java程序时会把内存划分为若干个不同的数据区域，这些区域各有各自的用途：

```
┌─────────────────────────────────────────┐
│            线程隔离区域                  │
│  ┌────────────┐      ┌────────────┐    │
│  │虚拟机栈栈帧│      │ 程序计数器 │    │
│  │  局部变量  │      │ (PC Register)│   │
│  │  操作数栈  │      └────────────┘    │
│  │  动态链接  │      ┌────────────┐    │
│  │  返回地址  │      │ 本地方法栈 │    │
│  └────────────┘      └────────────┘    │
├─────────────────────────────────────────┤
│            线程共享区域                  │
│  ┌──────────────────┬──────────────┐   │
│  │      堆          │    方法区     │   │
│  │  (Heap)          │ (Method Area) │   │
│  │  - 新生代        │  - 类信息     │   │
│  │  - 老年代        │  - 常量池     │   │
│  │  - 对象实例      │  - 静态变量   │   │
│  └──────────────────┴──────────────┘   │
└─────────────────────────────────────────┘
```

### 2.2 各内存区域详解

#### 2.2.1 程序计数器 (Program Counter Register)
- **作用**：记录当前线程执行的字节码行号
- **特点**：
  - 线程私有
  - 唯一一个不会发生OutOfMemoryError的区域
  - 正在执行Java方法时记录字节码指令地址
  - 执行Native方法时值为空

#### 2.2.2 虚拟机栈 (JVM Stack)
- **作用**：存储方法调用的相关信息
- **存储内容**：
  - **局部变量表**：方法参数和局部变量
  - **操作数栈**：用于计算的栈
  - **动态链接**：指向运行时常量池的引用
  - **返回地址**：方法正常或异常退出的地址

- **栈帧结构**：
```
┌──────────────────┐
│   栈帧 (printInfo) │  ← 当前执行的方法
│  - localVar2     │
├──────────────────┤
│   栈帧 (main)     │
│  - obj           │
│  - localVar1     │
├──────────────────┤
│   栈帧 (方法3)    │
└──────────────────┘
```

#### 2.2.3 本地方法栈 (Native Method Stack)
- **作用**：为Native方法服务
- **特点**：与虚拟机栈类似，但服务于Native方法

#### 2.2.4 堆 (Heap)
- **作用**：存储对象实例
- **特点**：
  - 线程共享
  - GC的主要管理区域
  - 可以物理上不连续

- **堆内存结构**：
```
┌──────────────────────────────┐
│           堆内存              │
├──────────────┬───────────────┤
│    新生代     │    老年代      │
│  ┌────┬────┐ │               │
│  │Eden│S0│S1│ │  Tenured     │
│  │    │  │  │ │  Generation  │
│  └────┴────┘ │               │
│   8:1:1      │               │
└──────────────┴───────────────┘
```

#### 2.2.5 方法区 (Method Area)
- **作用**：存储类信息、常量、静态变量
- **特点**：
  - 线程共享
  - 别名：非堆(Non-Heap)
  - JDK8之后使用元空间(Metaspace)实现

- **存储内容**：
  - 类信息（版本、字段、方法、接口）
  - 常量池（字面量和符号引用）
  - 静态变量
  - 即时编译器编译后的代码

---

## 3. JVM 垃圾回收机制

### 3.1 垃圾回收概述
垃圾回收(Garbage Collection, GC)是JVM自动管理内存的核心机制，用于自动回收不再使用的对象，防止内存泄漏。

### 3.2 如何判断对象可以被回收？

#### 3.2.1 引用计数法
- **原理**：给对象添加一个引用计数器
- **问题**：无法解决循环引用问题

#### 3.2.2 可达性分析法（JVM采用）
- **原理**：从GC Roots向下搜索，不可达的对象即为可回收
- **GC Roots包括**：
  - 虚拟机栈中引用的对象
  - 方法区中静态属性引用的对象
  - 方法区中常量引用的对象
  - 本地方法栈中JNI引用的对象

```
GC Roots → 可达对象 → 可达对象
           ↓
           不可达对象 (可被回收)
```

### 3.3 垃圾回收算法

#### 3.3.1 标记-清除算法 (Mark-Sweep)
```
标记前：  □□■□■□■□□■
标记后：  □□✓□✓□✓□□✓
清除后：  □□□□□□□□□□
```
- **优点**：简单
- **缺点**：产生大量不连续内存碎片

#### 3.3.2 复制算法 (Copying)
```
将存活对象从From复制到To，然后清空From
┌─────────┐         ┌─────────┐
│  From   │  →→→   │   To    │
│ ■□■□■□ │         │ ■■■□□□ │
└─────────┘         └─────────┘
```
- **优点**：没有内存碎片，效率高
- **缺点**：内存利用率低

#### 3.3.3 标记-整理算法 (Mark-Compact)
```
标记后：  □✓□✓□✓□□✓
整理后：  ✓✓✓✓□□□□□□
```
- **优点**：无内存碎片
- **缺点**：效率相对较低

#### 3.3.4 分代收集算法 (Generational)
```
新生代（对象生命周期短）→ 复制算法
老年代（对象生命周期长）→ 标记-整理/标记-清除
```

### 3.4 GC的类型

#### 3.4.1 Minor GC
- **触发时机**：新生代 Eden 区满
- **回收对象**：新生代
- **特点**：频繁，速度快

#### 3.4.2 Major GC
- **触发时机**：老年代满
- **回收对象**：老年代
- **特点**：速度较慢，伴随Major GC

#### 3.4.3 Full GC
- **触发时机**：
  - 老年代满
  - 方法区满
  - 显式调用 System.gc()
- **回收对象**：整个堆和方法区
- **特点**：最慢，应尽量避免

### 3.5 垃圾收集器
| 收集器 | 类型 | 世代 | 特点 |
|--------|------|------|------|
| Serial | 串行 | 新生代 | 单线程，适合客户端 |
| Parallel | 并行 | 新生代 | 多线程，吞吐量高 |
| CMS | 并发 | 老年代 | 低延迟，已废弃 |
| G1 | 分代 | 全堆 | 可预测停顿时间 |
| ZGC | 并发 | 全堆 | 超低延迟(<10ms) |

---

## 4. 代码示例解析

### 4.1 内存模型示例 (JvmMemoryModel.java)

```java
/**
 * 理解JVM内存模型(栈、堆、方法区)
 */
public class JvmMemoryModel {
    // 静态变量 → 方法区
    private static String staticVar = "我是静态变量，存在方法区";

    // 成员变量 → 堆
    private String memberVar = "我是成员变量，存在堆中";

    public static void main(String[] args) throws InterruptedException {
        // 局部变量1 → 虚拟机栈（main栈帧）
        // 字符串常量 → 常量池（方法区）
        String localVar1 = "局部变量(字符串常量)，存在常量池(方法区)";

        // obj引用 → 虚拟机栈（存储堆中对象地址）
        // new JvmMemoryModel() → 堆
        JvmMemoryModel obj = new JvmMemoryModel();

        // 调用方法创建新的栈帧
        printInfo(obj, localVar1);

        Thread.sleep(100000);
    }

    public static void printInfo(JvmMemoryModel obj, String localVar1){
        // 局部变量 → 虚拟机栈（printInfo栈帧）
        String localVar2 = "我是printInfo的局部变量，存在栈中";
        System.out.println(staticVar);   // 方法区
        System.out.println(obj.memberVar); // 堆
        System.out.println(localVar1);    // 栈（main栈帧）
        System.out.println(localVar2);    // 栈（当前栈帧）
    }
}
```

**内存分布图**：
```
┌─────────────────────────────────────┐
│           虚拟机栈                   │
│  ┌─────────────────────────────────┐ │
│  │ 栈帧: printInfo                  │ │
│  │  - localVar2: "..." (栈)         │ │
│  │  - obj: 0x1234 (引用)            │ │
│  │  - localVar1: "..." (引用)       │ │
│  ├─────────────────────────────────┤ │
│  │ 栈帧: main                       │ │
│  │  - localVar1: "..." (引用)       │ │
│  │  - obj: 0x1234 (引用)            │ │
│  │  - args: String[]               │ │
│  └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│              堆                     │
│  ┌─────────────────────────────────┐ │
│  │ 对象: 0x1234                    │ │
│  │  - memberVar: "..."             │ │
│  └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│            方法区                    │
│  - staticVar: "..."                 │
│  - 常量池: 各种字符串常量            │
│  - 类信息: JvmMemoryModel            │
└─────────────────────────────────────┘
```

### 4.2 垃圾回收示例 (JavaGcDemo.java)

```java
/**
 * 步骤3：理解JVM垃圾回收（GC）
 */
public class JavaGcDemo {
    // 大对象：1MB
    static class BigObject {
        byte[] data = new byte[1024 * 1024];
    }

    public static void main(String[] args) throws InterruptedException {
        // 阶段1：创建3个大对象，占用3MB堆内存
        System.out.println("阶段1：创建3个大对象，占用堆内存...");
        BigObject obj1 = new BigObject();
        BigObject obj2 = new BigObject();
        BigObject obj3 = new BigObject();
        printMemoryInfo();

        // 阶段2：将对象引用置为null，变为垃圾对象
        System.out.println("阶段2：将对象引用置为null，变为垃圾对象(无任何引用指向)");
        obj1 = null;
        obj2 = null;
        obj3 = null;

        // 阶段3：建议JVM执行GC
        System.out.println("阶段3：建议JVM执行GC...");
        System.gc();

        // 休眠1s,执行GC
        Thread.sleep(1000);

        // 阶段4：打印GC后堆内存状态
        System.out.println("阶段4：打印GC后堆内存状态");
        printMemoryInfo();
    }

    private static void printMemoryInfo() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("堆内存总大小: " + totalMemory/1024/1024 + "MB");
        System.out.println("堆内存已使用: " + usedMemory/1024/1024 + "MB");
        System.out.println("堆内存剩余: " + freeMemory/1024/1024 + "MB");
    }
}
```

**GC过程图**：
```
阶段1：创建对象
┌─────────────────────┐
│  栈                 │
│  obj1 ──────┐       │
│  obj2 ──────┼──────┼──→ ┌─────────┐
│  obj3 ──────┘       │    │  堆     │
└─────────────────────┘    │ ┌─────┐ │
                          │ │Obj1 │ │
                          │ ├─────┤ │
                          │ │Obj2 │ │
                          │ ├─────┤ │
                          │ │Obj3 │ │
                          │ └─────┘ │
                          └─────────┘

阶段2：引用置null
┌─────────────────────┐
│  栈                 │
│  obj1: null         │    ┌─────────┐
│  obj2: null         │    │  堆     │
│  obj3: null         │    │ ┌─────┐ │
└─────────────────────┘    │ │Obj1 │ │  ← 不可达
                           │ ├─────┤ │    (垃圾对象)
                           │ │Obj2 │ │
                           │ ├─────┤ │
                           │ │Obj3 │ │
                           │ └─────┘ │
                           └─────────┘

阶段3-4：GC后
┌─────────────────────┐
│  栈                 │
│  obj1: null         │    ┌─────────┐
│  obj2: null         │    │  堆     │
│  obj3: null         │    │  (空)   │
└─────────────────────┘    └─────────┘
                           内存已回收
```

---

## 5. 运行指南

### 5.1 编译代码
```bash
# 编译所有Java文件
javac src/jvm/*.java -d src/jvm/
```

### 5.2 运行程序

#### 运行内存模型示例
```bash
java -cp src/jvm jvm.JvmMemoryModel
```

**建议JVM参数**：
```bash
# 设置堆大小，观察更明显
java -Xms10m -Xmx10m -cp src/jvm jvm.JvmMemoryModel
```

#### 运行GC示例
```bash
java -cp src/jvm jvm.JavaGcDemo
```

**建议JVM参数（查看GC详情）**：
```bash
# 打印GC详细信息
java -Xms10m -Xmx10m -XX:+PrintGCDetails -cp src/jvm jvm.JavaGcDemo

# JDK9+使用新的日志参数
java -Xms10m -Xmx10m -Xlog:gc*:file=gc.log -cp src/jvm jvm.JavaGcDemo
```

### 5.3 常用JVM参数

#### 内存相关
| 参数 | 说明 | 示例 |
|------|------|------|
| -Xms | 初始堆大小 | -Xms10m |
| -Xmx | 最大堆大小 | -Xmx10m |
| -Xmn | 新生代大小 | -Xmn5m |
| -Xss | 线程栈大小 | -Xss512k |
| -XX:MetaspaceSize | 元空间初始大小 | -XX:MetaspaceSize=128m |
| -XX:MaxMetaspaceSize | 元空间最大大小 | -XX:MaxMetaspaceSize=256m |

#### GC相关
| 参数 | 说明 |
|------|------|
| -XX:+PrintGC | 打印GC日志 |
| -XX:+PrintGCDetails | 打印GC详细信息 |
| -XX:+PrintGCTimeStamps | 打印GC时间戳 |
| -XX:+PrintGCApplicationStoppedTime | 打印GC停顿时间 |
| -Xlog:gc* | 统一日志(JDK9+) |

#### 分析工具
```bash
# 查看堆转储快照
jmap -dump:format=b,file=heap.bin <pid>

# 查看线程信息
jstack <pid>

# 查看JVM统计信息
jstat -gcutil <pid> 1000 10
```

---

## 6. 常见问题

### Q1: 为什么调用 System.gc() 不一定会执行GC？
**A**: `System.gc()` 只是**建议**JVM执行GC，不是强制。JVM会根据自身策略决定是否执行。

### Q2: 栈溢出(StackOverflowError)是什么原因？
**A**:
- 递归调用过深
- 方法调用层级过多
- 线程栈空间设置过小

**解决**：
- 检查递归逻辑
- 增加 -Xss 参数（如 -Xss2m）

### Q3: 堆溢出(OutOfMemoryError)是什么原因？
**A**:
- 对象无法回收（内存泄漏）
- 对象过多且生命周期长
- 堆内存设置过小

**解决**：
- 检查内存泄漏
- 优化对象生命周期
- 增加 -Xmx 参数

### Q4: 方法区和堆的区别？
**A**:
| 特性 | 方法区 | 堆 |
|------|--------|-----|
| 存储内容 | 类信息、常量、静态变量 | 对象实例 |
| 线程共享 | 是 | 是 |
| GC频率 | 低 | 高 |
| 异常 | OutOfMemoryError | OutOfMemoryError |

### Q5: 字符串常量存在哪里？
**A**:
- **JDK6**：在永久代(PermGen)
- **JDK7**：在堆中
- **JDK8+**：在堆中

### Q6: 如何监控JVM内存？
**A**: 可以使用以下工具：
- **命令行**：jstat、jmap、jstack
- **可视化工具**：JConsole、VisualVM、JProfiler
- **在线工具**：Arthas

---

## 参考资料
- [JVM规范官方文档](https://docs.oracle.com/javase/specs/jvms/se8/html/)
- [Java性能优化权威指南](https://www.oracle.com/java/technologies/javase/codeconv-parameters.html)
- [深入理解Java虚拟机 - 周志明](https://book.douban.com/subject/24722611/)

---

**文档版本**: v1.0
**最后更新**: 2026-02-05
