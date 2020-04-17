package cn.xxx.xxx.xxx.mapper;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 从数据库表反射出实体类，自动生成实体类
 *
 * 
 *
 */
@Slf4j
public class EntityHelper {
	// 基本数据配置
	private String authorName = "zyh";// 作者名字
	private String[] colNames; // 列名数组
	private String[] colTypes; // 列名类型数组
	private String[] colComments; // 列名类型数组

	private String version = "V0.01"; // 版本
	private int[] colSizes; // 列名大小数组
	private boolean f_sql = false; // 是否需要导入包java.sql.*
	private boolean f_lang = false; // 是否需要导入包java.sql.*

	private String srcPath = "\\src\\test\\java";
	private String packagePath = "xxxx";

	public EntityHelper(String packageOutPath, Connection con) {

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

	public void generateEntityOneTable(String packageOutPath, Connection con, String tableName, String className) {

		// 查要生成实体类的表
		PreparedStatement pStemt = null;
		try {
			pStemt = con.prepareStatement("SELECT * FROM " + tableName);
			ResultSetMetaData rsmd = pStemt.getMetaData();

			ResultSet rs = null;
			rs = pStemt.executeQuery("SHOW FULL columns FROM " + tableName);
			List<String> colnComments = new ArrayList<>();// 列名注释集合
			while (rs.next()) {
				colnComments.add(rs.getString("Comment"));
			}

			colComments = colnComments.toArray(new String[colnComments.size()]);

			int size = rsmd.getColumnCount(); // 统计列
			colNames = new String[size];
			colTypes = new String[size];
			colSizes = new int[size];
			for (int i = 0; i < size; i++) {

				colNames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);

				// 自动生成包配置
				// if (colTypes[i].equalsIgnoreCase("datetime")) {
				// f_util = true;
				// }
				if (colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")
						|| colTypes[i].equalsIgnoreCase("datetime") || colTypes[i].equalsIgnoreCase("time")
						|| colTypes[i].equalsIgnoreCase("date") || colTypes[i].equalsIgnoreCase("datetime2")) {
					f_sql = true;
				}
				// if (colTypes[i].equalsIgnoreCase("int")) {
				// f_lang = true;
				// }
				colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
			}

			String subPackagePath = packagePath + (Generator.entityPkg == "" ? "." : "." + Generator.entityPkg);
			String classDir = packageOutPath + srcPath + "\\" + subPackagePath.replace(".", "\\");
			String classFile = classDir + "\\" + className + ".java";
			File file = new File(classFile);
			if (!(file.getParentFile().exists())) {
				file.getParentFile().mkdirs();
			}
			log.info("Entity: {}", classFile);

			String content = parse(colNames, colTypes, colSizes, colComments, subPackagePath, tableName, className);

			FileWriter fw = new FileWriter(classFile);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(content);

			pw.flush();
			pw.close();

		} catch (SQLException ex) {
			log.error("Generate Java file exception:", ex);
		} catch (IOException ex) {
			log.error("Generate Java file exception:", ex);
		} finally {
		}
	}

	/**
	 * 功能：生成实体类主体代码
	 *
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes, int[] colSizes, String[] colnComments,
			String packageName, String tableName, String className) {
		StringBuffer sb = new StringBuffer();
		// 生成package包路径
		sb.append("package " + packageName + ";\r\n");
		// 判断是否导入工具包
		if (f_sql) {
			sb.append("import java.util.*;\r\n");
		}
		if (f_lang) {
			sb.append("import java.lang.*;\r\n");
		}

		sb.append("import java.io.Serializable;\r\n");
		sb.append("import javax.persistence.Table;\r\n");
		sb.append("import javax.persistence.Entity;\r\n");
		sb.append("import javax.persistence.Id;\r\n");
		sb.append("import javax.persistence.Column;\r\n");
		sb.append("import javax.persistence.GeneratedValue;\r\n");
		sb.append("import cn.dm89.app.util.JsonUtil;\r\n");
		sb.append("import lombok.Data;\r\n");
		// sb.append("import lombok.AllArgsConstructor;\r\n");
		sb.append("import lombok.NoArgsConstructor;\r\n");

		sb.append("\r\n");
		// 注释部分
		sb.append("/**\r\n");
		sb.append(" * @文件名称：" + className + ".java\r\n");
		sb.append(" * @创建时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\r\n");
		sb.append(" * @创  建  人：" + this.authorName + " \r\n");
		sb.append(" * @文件描述：" + tableName + " 实体类\r\n");
		sb.append(" * @文件版本：" + this.version + " \r\n");
		sb.append(" */ \r\n");
		sb.append("\r\n");
		sb.append("@Data").append("\r\n");
		sb.append("@Entity").append("\r\n");
		// sb.append("\n@AllArgsConstructor");
		sb.append("@NoArgsConstructor").append("\r\n");
		sb.append("@Table(name = \"" + tableName + "\")").append("\r\n");
		sb.append("@Accessors(chain = true)").append("\r\n");

		// 实体部分
		sb.append("public class " + className + " implements Serializable{").append("\r\n");
		;
		sb.append("\r\n");
		sb.append("    private static final long serialVersionUID = 1L;").append("\r\n");
		;
		sb.append("    @Id").append("\r\n");
		sb.append("    @Column(name = \"id\")").append("\r\n");
		sb.append("    @GeneratedValue(generator = \"JDBC\") // MySQL自增主键").append("\r\n");
		;
		sb.append("    private Integer id;").append("\r\n").append("\r\n");

		processAllAttrs(sb);// 属性

		sb.append("    @Override\r\n");
		sb.append("    public String toString() {\r\n");
		sb.append("        return JsonUtil.toString(this);\r\n");
		sb.append("    }\r\n");
		sb.append("}\r\n");

		// System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * 功能：生成所有属性
	 *
	 * @param sb
	 */
	private void processAllAttrs(StringBuffer sb) {

		for (int i = 1; i < colNames.length; i++) {
			sb.append("\t/**\r\t* " + colComments[i] + "\r\t*/\r\n");
			sb.append("\t@Column(name = \"" + colNames[i] + "\")\r\n");
			sb.append(
					"\tprivate " + sqlType2JavaType(colTypes[i]) + " " + this.underlineToHump(colNames[i]) + ";\r\n\n");
		}

	}

	/**
	 * 功能：生成所有方法
	 *
	 * @param sb
	 */
	// private void processAllMethod(StringBuffer sb) {
	//
	// for (int i = 0; i < colnames.length; i++) {
	// sb.append("\tpublic void set" + initcap(colnames[i]) + "(" +
	// sqlType2JavaType(colTypes[i]) + " "
	// + colnames[i] + "){\r\n");
	// sb.append("\tthis." + colnames[i] + "=" + colnames[i] + ";\r\n");
	// sb.append("\t}\r\n");
	// sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" +
	// initcap(colnames[i]) + "(){\r\n");
	// sb.append("\t\treturn " + colnames[i] + ";\r\n");
	// sb.append("\t}\r\n");
	// }
	//
	// }

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
	 * 功能：获得列的数据类型
	 *
	 * @param sqlType
	 * @return
	 */
	private String sqlType2JavaType(String sqlType) {

		if (sqlType.equalsIgnoreCase("bit")) {
			return "Boolean";
		} else if (sqlType.equalsIgnoreCase("smallmoney") || sqlType.equalsIgnoreCase("numeric")
				|| sqlType.equalsIgnoreCase("bigint")) {
			return "Long";
		} else if (sqlType.equalsIgnoreCase("money") || sqlType.equalsIgnoreCase("decimal")
				|| sqlType.equalsIgnoreCase("float")) {
			return "Double";
		} else if (sqlType.equalsIgnoreCase("int") || sqlType.equalsIgnoreCase("int identity")
				|| sqlType.equalsIgnoreCase("int unsigned")) {
			return "Integer";
		} else if (sqlType.equalsIgnoreCase("image") || sqlType.equalsIgnoreCase("varbinary(max)")
				|| sqlType.equalsIgnoreCase("varbinary") || sqlType.equalsIgnoreCase("udt")
				|| sqlType.equalsIgnoreCase("binary")) {
			return "Byte[]";
		} else if (sqlType.equalsIgnoreCase("nchar") || sqlType.equalsIgnoreCase("nvarchar(max)")
				|| sqlType.equalsIgnoreCase("mediumtext") || sqlType.equalsIgnoreCase("longtext")
				|| sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nvarchar(ntext)")
				|| sqlType.equalsIgnoreCase("uniqueidentifier") || sqlType.equalsIgnoreCase("xml")
				|| sqlType.equalsIgnoreCase("char") || sqlType.equalsIgnoreCase("varchar(max)")
				|| sqlType.equalsIgnoreCase("text") || sqlType.equalsIgnoreCase("varchar")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("real")) {
			return "Float";
		} else if (sqlType.equalsIgnoreCase("smallint") || sqlType.equalsIgnoreCase("tinyint")) {
			return "Short";
		} else if (sqlType.equalsIgnoreCase("date") || sqlType.equalsIgnoreCase("datetime")
				|| sqlType.equalsIgnoreCase("time") || sqlType.equalsIgnoreCase("datetime2")
				|| sqlType.equalsIgnoreCase("timestamp")) {
			return "Date";
		} else {
			System.out.println("数据类型异常，类型为：" + sqlType);
		}

		return null;
	}

	/**
	 * 下划线转驼峰
	 *
	 * @String para
	 */
	private String underlineToHump(String para) {
		StringBuilder result = new StringBuilder();
		String a[] = para.split("_");
		for (String s : a) {
			if (result.length() == 0) {
				result.append(s.toLowerCase());
			} else {
				result.append(s.substring(0, 1).toUpperCase());
				result.append(s.substring(1).toLowerCase());
			}
		}
		return result.toString();
	}
}
