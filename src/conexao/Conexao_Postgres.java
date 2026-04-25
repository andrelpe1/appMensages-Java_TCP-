package conexao;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao_Postgres {
	  private static final String URL = "jdbc:postgresql://localhost:5432/appmensagens";
	  private static final String USER = "postgres";
	  private static final String PASSWORD = "123";

	  public static Connection conectar() throws Exception {
	        return DriverManager.getConnection(URL, USER, PASSWORD);
	    }
}
