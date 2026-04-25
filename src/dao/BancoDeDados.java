package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class BancoDeDados {
	private static Connection conn = null;
	
	public static Connection conectar()throws SQLException, IOException{
		if(conn == null) {
			
			Properties props = carregarPropriedades();
			String url = props.getProperty("dburl");
			conn = DriverManager.getConnection(url, props);
		}
		
		return conn;
	}
	
	public static Connection desconectar() throws SQLException{
		if(conn != null) {
			conn.close();
			conn = null;
		}
		return conn;
	}
	
	private static Properties carregarPropriedades() throws IOException{
		FileInputStream propriedadesBanco = null;
		
		propriedadesBanco = new FileInputStream("database.properties");
		
		Properties props = new Properties();
		props.load(propriedadesBanco);
		
		return props;
		
	}
	
	public static void finalizarStatement(Statement st) throws SQLException{
		if(st != null) {
			st.close();
		}
	}
	
	public static void finalizarResultSec(ResultSet rs) throws SQLException{
		if(rs != null) {
			rs.close();
		}
	}
}