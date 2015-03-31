package com.zju.logservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

public class AppUtil {
	private static final Logger logger = Logger.getLogger(AppUtil.class);
	private final static String DATASOURCE_PROPERTIES = "datasource.properties";
	private String url;
	private String user;
	private String password;
	
	

	public AppUtil() {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(DATASOURCE_PROPERTIES);
		Properties props = new Properties();
		try {
			props.load(in);
			url = props.getProperty("url");
			user = props.getProperty("user");
			password = props.getProperty("password");
		} catch (IOException e) {
			logger.error("fail to load datasource properties!");
		}	
	}

	private Connection getConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{			
			Class.forName("org.postgresql.Driver").newInstance();
			return DriverManager.getConnection(url, user, password);
	}

	public String getAppNameById(String appID) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		Statement statement = null;
		Connection conn=getConnection();
		statement = conn.createStatement();
		String sql = "SELECT name FROM apps WHERE guid='" + appID + "'";
		ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		String name=resultSet.getString("name");
		close(statement,resultSet,conn);
		return name;	
	}

	public void close(Statement statement,ResultSet resultSet,Connection conn) throws SQLException {
		if (statement != null) {
			statement.close();
		}
		
		if (resultSet != null) {
			resultSet.close();
		}
		
		if (conn != null) {
			conn.close();
		}
	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		/*AppUtil ps = new AppUtil();
		try {
			System.out
					.println("name :::"
							+ ps.getAppNameById("7030c001-02c2-495a-b0db-73a5f72bfc75"));
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
*/
		long t = System.currentTimeMillis();
		System.out.print(t-t%3600000);
	}
}
