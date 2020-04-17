package cn.dm89.app.edm.mapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * 从数据库表反射出实体类，自动生成实体类
 *
 * 
 *
 */
@Slf4j
public class MapperHelper {
    //基本数据配置
    private String authorName = "zyh";// 作者名字
    private String[] colNames; // 列名数组

    private String version = "V0.01"; // 版本

    private String srcPath = "\\src\\test\\java";
    private String packagePath = "xxxx";


	public MapperHelper(String packageOutPath, Connection con) {

		// 查要生成实体类的表
		PreparedStatement pStemt = null;
		ResultSet rs = null;
		try {
			pStemt = con.prepareStatement("SHOW TABLES");
			rs = pStemt.executeQuery();
			while (rs.next()) {
				String tableName = rs.getString("Tables_in_" + Generator.DB);
				generateEntityOneTable(packageOutPath, con, tableName, initcap(underlineToHump(tableName)));
			}
			rs.close();
			pStemt.close();
			con.close();
		} catch (SQLException ex) {
			log.error("Fetching tables exception:", ex);
		} finally {
		}
	}
	
	@Deprecated
    public void generateEntityOneTable(String packageOutPath, Connection con, String tableName, String className) {
    	
        // 查要生成实体类的表
        PreparedStatement pStemt = null;
        try {
            pStemt = con.prepareStatement("SELECT * FROM " + tableName);
            ResultSetMetaData rsmd = pStemt.getMetaData();
            
            int size = rsmd.getColumnCount(); // 统计列
            colNames = new String[size];
            for (int i = 0; i < size; i++) {

                colNames[i] = rsmd.getColumnName(i + 1);
            }

            String subPackagePath = packagePath + (Generator.mapperPkg == "" ? "." : "." + Generator.mapperPkg);
            
        	String classDir = packageOutPath + srcPath + "\\" + subPackagePath.replace(".", "\\");
            String classFile = classDir + "\\" + className + ".java";

            File file = new File(classFile);
    		if (!(file.getParentFile().exists())) {
    			file.getParentFile().mkdirs();
    		}

            String mapperContent = parse(subPackagePath,className);

            try {
                File directory = new File("");
                String outputPath = directory.getAbsolutePath() + this.srcPath
                        + "\\" + subPackagePath.replace(".", "\\") + "\\" +initcap(className) + "Mapper.java";

                log.info("Mapper: {}", outputPath);

                FileWriter mfw = new FileWriter(outputPath);
                PrintWriter mpw = new PrintWriter(mfw);
                mpw.println(mapperContent);
                mpw.flush();
                mpw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (SQLException ex) {
        	log.error("Generate Java file exception:", ex);
        } finally {
        }
    }

    /**
     * 功能：生成MAPPER类主体代码
     *
     * @param colnames
     * @param colTypes
     * @param colSizes
     * @return
     */
    private String parse(String packageOutPath,String mappername) {
        StringBuffer sb = new StringBuffer();
        // 生成package包路径
        sb.append("package " + packageOutPath + ";\r\n\n");
        // 判断是否导入工具包
        sb.append("import cn.dm89.app.edm.mapper."+ Generator.entityPkg+"."+initcap(mappername) +";\r\n");
        sb.append("import cn.dm89.app.common.mapper.AutoMapper;\r\n");
        sb.append("import tk.mybatis.mapper.common.Mapper;\r\n");
        sb.append("\r\n");
        // 注释部分
        sb.append("   /**\r\n");
        sb.append("    * @文件名称：" + initcap(mappername) + "Mapper.java\r\n");
        sb.append("    * @创建时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\r\n");
        sb.append("    * @创  建  人：" + this.authorName + " \r\n");
        sb.append("    * @文件描述：" + initcap(mappername) + " Mapper\r\n");
        sb.append("    * @文件版本：" + this.version + " \r\n");
        sb.append("    */ \r\n");
        sb.append("\n@org.apache.ibatis.annotations.Mapper");

        // 实体部分
        sb.append("\npublic interface " + initcap(mappername)+"Mapper extends AutoMapper<" + initcap(mappername)+">{\r\n\n");

        
//        processAllMethod(sb);// get set方法
        sb.append("}\r\n");

        // System.out.println(sb.toString());
        return sb.toString();
    }
    
    /**
     * 功能：将输入字符串的首字母改成大写
     *
     * @param str
     * @return
     */
    private String initcap(String str) {

        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    /**
     * 下划线转驼峰
     *
     * @String para
     */
    private  String underlineToHump(String para){
        StringBuilder result=new StringBuilder();
        String a[]=para.split("_");
        for(String s:a){
            if(result.length()==0){
                result.append(s.toLowerCase());
            }else{
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
}
