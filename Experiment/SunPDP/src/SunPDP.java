import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SunPDP {
    private static final int TIMES = 6;  //第1次实验明显比后几次实验用时长，原因未知？暂时猜想是Java虚拟机的缓存机制，建议做6次实验取后5次结果
    private static final int MAXNUM = 1000;

    private static boolean match(String p, String c) {
        Pattern pattern = Pattern.compile(c);
        Matcher matcher = pattern.matcher(p);
        return matcher.find();
    }

    public static void main(String[] args) {
        for (int t = 0; t < TIMES; t++) {  //进行TIMES次实验
            long total_time = 0;  //匹配多条请求的总时间
            long time;  //匹配一条请求的时间
            long start_time;  //开始匹配一条请求的时间节点
            long end_time;  //结束匹配一条请求的时间节点

            List<String> list = new ArrayList<String>();

            try {
                //String policy_path = "E:\XACML_Optimized_Matching\Experiment\SunPDP\src\\lms_extraction.csv";
                //String policy_path = "E:\XACML_Optimized_Matching\Experiment\SunPDP\src\\vms_extraction.csv";
                String policy_path = "E:\\XACML_Optimized_Matching\\Experiment\\SunPDP\\src\\asms_extraction.csv";

                File file = new File(policy_path);
                FileInputStream stream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader buffer = new BufferedReader(reader);

                String line = null;
                while ((line = buffer.readLine()) != null) {
                    list.add(line);
                    //System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            for (int i = 0; i < list.size(); i++) {  //lms的list.size()为3072，list的下标范围是0~3071
                System.out.println(list.get(i));
            }
            //System.out.println(i);
            */

            for (int temp = 1; temp <= MAXNUM; temp++) {
                //产生0~3071的随机整数，随机得到一条请求
                Random rand = new Random();
                int rand_num = rand.nextInt(list.size());
                //System.out.println(rand_num);
                String request = list.get(rand_num).replace("Permit,", "").replace("Deny,", "");
                //System.out.println(request);

                start_time = System.currentTimeMillis();
                for (int index = 0; index < list.size(); index++) {
                    String str = list.get(index);
                    if (match(str, request)) {
                        //System.out.println(str);
                        break;
                    }
                }
                end_time = System.currentTimeMillis();
                time = end_time - start_time;
                //System.out.println(time);
                total_time += time;
            }
            System.out.println(total_time);  //单位ms
        }
    }
}
