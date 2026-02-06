import java.util.HashSet;
import java.util.Set;

/**
 * 方法区和运行时常量池异常
 * 执行参数：-Xms6M -Xmx6M
 */
public class JvmRuntimeConstantPoolOOM {
    public static void main(String[] args) {
        //保持引用，避免GC
        Set<String> set = new HashSet<>();
        //在short范围内足以让6MB的PermSize产生OOM
        short i = 0;
        while (true){
            set.add(String.valueOf(i++).intern());
        }
    }
}
