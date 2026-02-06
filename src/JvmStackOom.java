/**
 * 模拟虚拟机栈溢出
 */
public class JvmStackOom {
    //记录递归调用次数
    private static int count = 0;

    public static void main(String[] args) {
        try {
            recursiveCall();
        } catch (Throwable e) {
            System.out.println("递归调用次数：" + count);
            e.printStackTrace();
        }
    }

    /**
     * 递归方法
     */
    private static void recursiveCall() {
        count++;
        recursiveCall();
    }
}
