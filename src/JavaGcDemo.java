/**
 * 步骤3：理解JVM垃圾回收（GC）
 */
public class JavaGcDemo {
    // 定义一个大对象（方便快速占用堆内存，直观看到GC效果）
    static class BigObject {
        // 字节数组：占用1MB内存（1024*1024=1048576字节）
        byte[] data = new  byte[1024 * 1024];
    }

    public static void main(String[] args) throws InterruptedException {
        // 阶段1：创建3个大对象，占用3MB堆内存
        System.out.println("阶段1：创建3个大对象，占用堆内存...");
        BigObject obj1 = new BigObject();
        BigObject obj2 = new BigObject();
        BigObject obj3 = new BigObject();

        // 打印当前内存状态（堆内存使用情况）
        printMemoryInfo();

        //阶段2：将对象引用置为null，变为垃圾对象(无任何引用指向)
        System.out.println("阶段2：将对象引用置为null，变为垃圾对象(无任何引用指向)");
        obj1 = null;
        obj2 = null;
        obj3 = null;

        //阶段3：建议JVM执行GC
        System.out.println("阶段3：建议JVM执行GC...");
        System.gc();

        //休眠1s,执行GC
        System.out.println("休眠1s,执行GC");
        Thread.sleep(100000);

        //阶段4：打印GC后堆内存状态
        System.out.println("阶段4：打印GC后堆内存状态");
        printMemoryInfo();
    }

    /**
     * 打印内存状态
     */
    private static void printMemoryInfo() {
        //获取JVM堆内存总大小
        long totalMemory = Runtime.getRuntime().totalMemory();
        //获取堆内存剩余大小
        long freeMemory = Runtime.getRuntime().freeMemory();
        //获取堆内存已用内存
        long usedMemory = totalMemory - freeMemory;

        System.out.println("堆内存总大小: " + totalMemory/1024/1024 + "MB");
        System.out.println("堆内存已使用: " + usedMemory/1024/1024 + "MB");
        System.out.println("堆内存剩余: " + freeMemory/1024/1024 + "MB");
    }
}
