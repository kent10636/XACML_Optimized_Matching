import java.util.*;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;

public class test {
    private static final int TIMES=6;

    public static void main(String[] args) {
        try {
            /* 写入Txt文件 */
            File writename = new File("output.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

//            int ls = 12, lr = 4, la = 16, lc = 4;//lms
//            int ls=28,lr=4,la=11,lc=5;//vms
            int ls=1125,lr=2,la=2,lc=2;//asms

            Random rand = new Random();
            Map<String, Integer> s = new HashMap<String, Integer>();
            for (int i = 0; i < ls; i++) {
                s.put("s" + i, i);
            }
            Map<String, Integer> r = new HashMap<String, Integer>();
            for (int i = 0; i < lr; i++) {
                r.put("r" + i, i);
            }
            Map<String, Integer> a = new HashMap<String, Integer>();
            for (int i = 0; i < la; i++) {
                a.put("a" + i, i);
            }
            Map<String, Integer> c = new HashMap<String, Integer>();
            for (int i = 0; i < lc; i++) {
                c.put("c" + i, i);
            }
//		for(int i=1;i<16;i++) {
//			Integer n=m.get("test"+i);
//		}
            java.util.BitSet bitSet = new java.util.BitSet(6144);

            //将指定位的值设为true
            bitSet.set(1, true);

            //输出指定位的值
            //System.out.println(bitSet.get(1));
            //System.out.println(bitSet.get(0));

            int rs, rr, ra, rc, rands, randr, randa, randc, index;
            long TimeSum = 0;
            String strs,strr,stra,strc;
            //for (int temp = 0; temp < TIMES; temp++) {
                for (int n = 1; n <= 10; n++) {
//                    for (int i = 0; i < ls; i++) {
//                        s.get("s" + i);
//                    }
//                    for (int i = 0; i < lr; i++) {
//                        r.get("r" + i);
//                    }
//                    for (int i = 0; i < la; i++) {
//                        a.get("a" + i);
//                    }
//                    for (int i = 0; i < lc; i++) {
//                        c.get("c" + i);
//                    }
                    for (int j = 0, count = n * 1000; j < count; j++) {

                        String res;
                        rands = rand.nextInt(ls);
                        randr = rand.nextInt(lr);
                        randa = rand.nextInt(la);
                        randc = rand.nextInt(lc);

                        strs="s" + rands;
                        strr="r" + randr;
                        stra="a" + randa;
                        strc="c" + randc;

//                        long starTime = System.nanoTime();
                        rs = s.get(strs);
                        rr = r.get(strr);
                        ra = a.get(stra);
                        rc = c.get(strc);

                        long starTime = System.nanoTime();

                        rs = s.get(strs);
                        rr = r.get(strr);
                        ra = a.get(stra);
                        rc = c.get(strc);
                        index = rs * lr * la * lc + rr * la * lc + ra * lc + rc;
                        if (bitSet.get(2 * index) && (!bitSet.get(2 * index + 1))) {
                            res = "P";
                        } else if (bitSet.get(2 * index + 1) && (!bitSet.get(2 * index))) {
                            res = "D";
                        } else res = "N";

                        long endTime = System.nanoTime();

                        long Time = endTime - starTime;
                        TimeSum += Time;
                        //	System.out.printf("%d %d %d %d\n",rs,rr,ra,rc);
                    }

                    String sss = String.valueOf(TimeSum);
                    out.write(sss);
                    out.write(",");
                    System.out.println(TimeSum);
                    TimeSum = 0;
                }

                System.out.println();
                out.write("\r\n");
            //}

            out.write("\r\n"); // \r\n即为换行
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}