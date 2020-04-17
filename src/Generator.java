package cn.xxx.xxx.xxx.mapper;

//导入所需的包
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;

/**
 * @文件名称：Generator.java 
 * @创建时间：2020-01-16 15:20:15
 * @创 建 人：zyh
 */
@Slf4j
public class Generator {

	// 数据库连接
	//which db need to be Generatored
	public static final String DB = "xxx";
	
	//jdbc 
	public static final String URL = "jdbc:mysql://127.0.0.1:3306/" + DB;
	
	public static final String NAME = "your name";
	
	public static final String PASS = "your password";
	
	public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

	private static String classname = "news";// 类名

	private static String packageOutPath = "cn.xxx.xxx.xxx.mapper." + classname + "";// 指定实体生成所在包的路径
	private final static String packageOutDir = "X:\\X\\X\\X\\X";// 指定实体生成所在包的路径

	public static String entityPkg = "";
	public static String mapperPkg = "";

	public static void main(String[] args) {

		String targetDir = packageOutDir;

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		Long time = new Long(Calendar.getInstance().getTimeInMillis());
		String d = format.format(time);

		entityPkg = "entity" + d;
		mapperPkg = "mapper" + d;

		File file = new File(targetDir);
		if (!(file.isDirectory() && file.exists())) {
			file.mkdir();
		}

		try {
			Class.forName(Generator.DRIVER);
		} catch (ClassNotFoundException ex) {
			log.error("Driver exception:", ex);
		}

		// 创建连接
		Connection entitycon = null;
		try {
			entitycon = DriverManager.getConnection(Generator.URL, Generator.NAME, Generator.PASS);
		} catch (SQLException ex) {
			log.error("Connection exception:", ex);
		}

		new EntityHelper(targetDir, entitycon);

		Connection mappercon = null;
		try {
			mappercon = DriverManager.getConnection(Generator.URL, Generator.NAME, Generator.PASS);
		} catch (SQLException ex) {
			log.error("Connection exception:", ex);
		}

		new MapperHelper(targetDir, mappercon);
	}
}
