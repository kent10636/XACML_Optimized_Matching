import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;

class requestText{
	String str;
	private static String path = "E:\\XACML_Optimized_Matching\\Experiment\\xEngine\\xEnginePDP_beta0.2\\xEnginePDP\\XACMLPolicies\\requests\\multi22\\";	//请求路径(记得更改！)
	private static String path2 = "E:\\XACML_Optimized_Matching\\Experiment\\xEngine\\xEnginePDP_beta0.2\\xEnginePDP\\XACMLPolicies\\requests\\single\\";	//请求路径(记得更改！)
	public requestText(String sub,String res, String act){
		String str1="<Request>\n<Subject SubjectCategory=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n<Attribute AttributeId=\"subject-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">\n<AttributeValue>";
		String str2="</AttributeValue>\n</Attribute>\n</Subject>\n<Resource>\n<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">\n<AttributeValue>DEFAULT RESOURCE</AttributeValue>\n</Attribute>\n<Attribute AttributeId=\"resource-class\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">\n<AttributeValue>";
		String str3="</AttributeValue>\n</Attribute>\n</Resource>\n<Action>\n<Attribute AttributeId=\"action-type\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">\n<AttributeValue>";
		String str4="</AttributeValue>\n</Attribute>\n</Action>\n</Request>";
		str = str1+sub+str2+res+str3+act+str4;
	}
	public void creatFile(int i) {	
		String filename = path + i + "-req.xml";
		String filename2 = path2 + i + "-req.xml";
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write(this.str);
			fw.close();
			FileWriter fw2 = new FileWriter(filename2);
			fw2.write(this.str);
			fw2.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
public class request {
	final static int N = 3072;	//规则数量(记得更改！)  //3072 6161 9000
	final static String Path = "E:\\XACML_Optimized_Matching\\Experiment\\xEngine\\policy_sets\\lms_record.csv";  //策略集路径(记得更改！)
	public static void generate_request(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);  //输入生成的待匹配策略数量
		int n = sc.nextInt();
		sc.close();
		for(int i = 1; i <= n; i++) {
			int x = (int)(Math.random()*N);
			createRequest(x,i);
		}
	}
	@SuppressWarnings("resource")
	public static void createRequest(int row, int i) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Path));
			String line = null;
			int index = 0;
			while((line=reader.readLine())!=null){
	               String item[] = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
	            if(index==row-1){
	                requestText str = new requestText(item[1],item[2],item[3]);
	                str.creatFile(i);
		            System.out.println(i);
	            }
	            //int value = Integer.parseInt(last);//如果是数值，可以转化为数值
	            index++;
	        } 
			
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
