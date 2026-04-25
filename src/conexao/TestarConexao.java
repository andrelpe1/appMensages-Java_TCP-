package conexao;

import java.sql.Connection;

public class TestarConexao {
	public static void main(String[] args) {
        try {
            Connection conn = Conexao_Postgres.conectar();

            if (conn != null) {
                System.out.println(" Conectado com sucesso!");
            }

            conn.close();

        } catch (Exception e) {
            System.out.println(" Erro ao conectar:");
            e.printStackTrace();
        }
    }
}
