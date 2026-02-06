import java.util.ArrayList;
import java.util.List;

/**
 * 模拟堆内存溢出
 */
public class JvmHeapOom {
    static class BigObject {
        //每个对象占用1MB
        byte[] data = new byte[1024*1024];
    }

    public static void main(String[] args) {
        //创建一个列表，保存对象引用(防止被GC)
        List<BigObject> objectList = new ArrayList<>();

        //无线循环创建对象，知道内存溢出
        int count = 0;
        while (true) {
            objectList.add(new BigObject());
            count++;
            System.out.println("已创建" + count + "个大对象，占用" + count + "MB 堆内存");
        }
    }
}
