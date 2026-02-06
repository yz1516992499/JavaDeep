/**
 * 理解JVM内存模型(栈、堆、方法区)
 */
public class JvmMemoryModel {

    private static String staticVar = "我是静态变量，存在方法区";

    private String memberVar = "我是成员变量，存在堆中";

    public static void main(String[] args) throws InterruptedException {
        //局部变量1：存储在虚拟机栈的main方法栈帧中
        String localVar1 = "局部变量(字符串常量)，存在常量池(方法区)";

        //局部变量2：存储在虚拟机栈中(存储的是堆中对象的地址)
        JvmMemoryModel obj = new  JvmMemoryModel();

        //调用方法创建新的栈帧
        printInfo(obj, localVar1);

        Thread.sleep(100000);
    }

    public static void printInfo(JvmMemoryModel obj, String localVar1){
        //局部变量3：存储在虚拟机栈的printInfo方法栈中
        String localVar2 = "我是printInfo的局部变量，存在栈中";
        System.out.println(staticVar);
        System.out.println(obj.memberVar);
        System.out.println(localVar1);
        System.out.println(localVar2);
    }
}
