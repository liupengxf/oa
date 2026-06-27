package org.buu.oa.util;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 工具类，用于管理数据库连接、查询、修改和资源关闭
 */
public class JDBCUtil {
	private static final String driver = "com.mysql.cj.jdbc.Driver";
	/*private static final String url = "jdbc:mysql://18.233.255.172:3306/bird_observation?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
	private static final String username = "root";
	private static final String password = "TianShui#2025!";*/
	private static final String url = "jdbc:mysql://localhost:3306/oa_office?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
	private static final String username = "root";
	private static final String password = "root";
	/**
	 * 获取数据库连接
	 * 
	 * @return Connection 对象
	 */
	public static Connection getConnection() {
		// 数据库连接对象
		Connection conn = null;
		try {
			// 加载并注册JDBC驱动
			Class.forName(driver);
			// 获取数据库连接，传入URL、用户名和密码
			conn = DriverManager.getConnection(url, username, password);
			// 检查连接是否成功且未关闭
			if (conn != null && !conn.isClosed()) {
				return conn;
			}
		} catch (ClassNotFoundException e) {
			// 驱动类未找到时的处理（打印异常信息）
			System.out.println(e);
		} catch (SQLException e) {
			// 数据库连接异常时的处理（抛出运行时异常）
			throw new RuntimeException(e);
		}
		// 返回连接对象（可能为null）
		return conn;
	}

	public static int executeUpdate(String sql, Object... objs) {
		// 获取数据库连接
		Connection connection = JDBCUtil.getConnection();
		// 声明PreparedStatement对象，用于执行SQL语句
		PreparedStatement preparedStatement = null;
		// 用于存储SQL执行影响的行数
		int n = 0;
		try {
			// 创建PreparedStatement对象，预编译SQL语句
			preparedStatement = connection.prepareStatement(sql);
			// 遍历参数数组，设置SQL语句中的参数
			for (int i = 1; i <= objs.length; i++) {
				// 设置第i个参数的值，注意索引从1开始
				preparedStatement.setObject(i, objs[i - 1]);
			}
			// 执行SQL更新操作，返回影响的行数
			n = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// 捕获SQL异常，转换为运行时异常抛出
			throw new RuntimeException(e);
		} finally {
			// 确保资源被释放，关闭连接和PreparedStatement
			JDBCUtil.close(connection, preparedStatement);
		}
		// 根据影响的行数判断操作是否成功，返回布尔值
		return n ;
	}
	public static int executeUpdate(String sql[], List<Object>... objs) throws Exception{
		// 获取数据库连接
		Connection connection = JDBCUtil.getConnection();
		// 声明PreparedStatement对象，用于执行SQL语句
		PreparedStatement preparedStatement = null;
		// 用于存储SQL执行影响的行数
		int n = 0;
		try {
			connection.setAutoCommit(false);
			for (int j=0;j< sql.length;j++){
				// 创建PreparedStatement对象，预编译SQL语句
				preparedStatement = connection.prepareStatement(sql[j]);
				// 遍历参数数组，设置SQL语句中的参数
				for (int i = 1; i <= objs[j].size(); i++) {
					// 设置第i个参数的值，注意索引从1开始
					preparedStatement.setObject(i, objs[j].get(i-1));
				}
				// 执行SQL更新操作，返回影响的行数
				n += preparedStatement.executeUpdate();
			}
			connection.commit();
		} catch (SQLException e) {
			if (connection != null) {
				try {
					connection.rollback(); // 回滚事务
				} catch (SQLException re) {
					re.printStackTrace();
				}
			}
			// 捕获SQL异常，转换为运行时异常抛出
			throw new RuntimeException(e);

		} finally {
			// 确保资源被释放，关闭连接和PreparedStatement
			JDBCUtil.close(connection, preparedStatement);
		}
		// 根据影响的行数判断操作是否成功，返回布尔值
		return n ;
	}

	/**
	 * 执行SQL查询并返回结果列表
	 * @param sql 要执行的SQL查询语句
	 * @param clazz 结果集映射的目标类
	 * @param objs SQL查询的参数值
	 * @return 映射后的结果列表
	 */
	public static List executeQuery(String sql, Class clazz, Object... objs) {
		// 获取数据库连接
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			// 创建PreparedStatement对象，预编译SQL语句
			preparedStatement = connection.prepareStatement(sql);

			// 设置SQL查询参数
			for (int i = 1; i <= objs.length; i++) {
				// 将参数值设置到PreparedStatement中，索引从1开始
				preparedStatement.setObject(i, objs[i - 1]);
			}
			// 执行查询并获取结果集
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			// 捕获SQL异常并转换为运行时异常抛出
			throw new RuntimeException(e);
		}
		// 调用JDBCUtil的parseResult方法将结果集映射为指定类型的列表
		return JDBCUtil.parseResult(resultSet, clazz);
	}
	/**
	 * 将数据库查询结果集(ResultSet)映射为指定类型的对象列表 实体类的属性应与数据库中表的属性类型一致否则在解析时会出现类型冲突
	 * @param resultSet 数据库查询结果集
	 * @param clazz 目标对象的类类型
	 * @return 映射后的对象列表
	 */
	public static <T> List<T> parseResult(ResultSet resultSet, Class<T> clazz) {
		// 1. 初始化结果列表，用于存储映射后的对象
		List<T> resultList = new ArrayList<>();
		try {
			// 2. 获取结果集的元数据，包含列信息
			ResultSetMetaData metaData = resultSet.getMetaData();
			// 3. 获取结果集的列数
			int columnCount = metaData.getColumnCount();
			// 4. 遍历结果集的每一行
			while (resultSet.next()) {
				// 5. 创建目标类的一个新实例
				T obj = clazz.getDeclaredConstructor().newInstance();
				// 6. 遍历当前行的每一列
				for (int i = 1; i <= columnCount; i++) {
					// 7. 获取当前列的名称
					String columnName = metaData.getColumnName(i);
					// 8. 获取当前列的值
					Object columnValue = resultSet.getObject(i);
					try {
						// 9. 尝试获取目标类中与列名同名的字段
						Field field = clazz.getDeclaredField(columnName);
						// 10. 设置字段可访问（即使字段是private）
						field.setAccessible(true);
						// 11. 将列值设置到对象的对应字段中
						field.set(obj, columnValue);
					} catch (NoSuchFieldException e) {
						// 12. 如果类中没有对应的字段，则忽略该列（不处理异常）
						e.printStackTrace();
					}
				}
				// 13. 将映射完成的对象添加到结果列表中
				resultList.add(obj);
			}
		} catch (Exception e) {
			// 14. 捕获所有可能的异常，并包装为运行时异常抛出
			throw new RuntimeException("Failed to parse result set", e);
		}
		// 15. 返回包含所有映射对象的列表
		return resultList;
	}
	/**
	 关闭数据库资源
	 */
	public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
		// 检查ResultSet是否非空，避免空指针异常
		if (rs != null) {
			try {
				// 关闭ResultSet对象，释放数据库资源
				rs.close();
			} catch (SQLException e) {
				// 捕获关闭时可能抛出的SQL异常，打印堆栈信息
				e.printStackTrace();
			}
		}
		// 检查PreparedStatement是否非空，避免空指针异常
		if (ps != null) {
			try {
				// 关闭PreparedStatement对象，释放数据库资源
				ps.close();
			} catch (SQLException e) {
				// 捕获关闭时可能抛出的SQL异常，打印堆栈信息
				e.printStackTrace();
			}
		}
		// 检查Connection是否非空，避免空指针异常
		if (conn != null) {
			try {
				// 关闭Connection对象，释放数据库资源
				conn.close();
			} catch (SQLException e) {
				// 捕获关闭时可能抛出的SQL异常，打印堆栈信息
				e.printStackTrace();
			}
		}
	}
	/**
	 * 关闭数据库资源（无结果集）
	 * 
	 * @param conn
	 *            数据库连接
	 * @param ps
	 *            预处理语句
	 */
	public static void close(Connection conn, PreparedStatement ps) {
		close(conn, ps, null);
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @param conn
	 *            数据库连接
	 */
	public static void close(Connection conn) {
		close(conn, null, null);
	}

	public static void main(String[] args) {
		JDBCUtil.getConnection();
		//System.out.println(JDBCUtil.executeUpdate("insert into user(username,email,`password`) values(?,?,?)","3G","13246@qq.com","123123"));
	}
}
