package dao;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import entities.Usuario;

public class UsuarioDAO {
	private Connection conn;
	
	public UsuarioDAO(Connection conn) {
		this.conn = conn;
	} 

	
	public int cadastrarUsuario(Usuario usuario) throws SQLException{
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("insert into usuario (nome, usuario, senha) values (?, ?, ?)",java.sql.Statement.RETURN_GENERATED_KEYS);
			st.setString(1, usuario.getNome());
			st.setString(2,usuario.getUsuario());
			st.setString(3,usuario.getSenha());
			
			int retorno = st.executeUpdate();
			
			if(retorno == 1) {
				  return 1;
			  }else {
				  return 0;
			  }
			}finally {
				BancoDeDados.finalizarStatement(st);
				BancoDeDados.desconectar();
		}	
	}
	
	public int deletarUsuario(String usuario) throws SQLException {
	    PreparedStatement st = null;

	    try {
	        st = conn.prepareStatement("DELETE FROM usuario WHERE usuario = ?");

	        st.setString(1, usuario);

	        int retorno = st.executeUpdate();

	        if (retorno == 1) {
	            return 1;
	        } else {
	            return 0; 
	        }

	    } finally {
	        BancoDeDados.finalizarStatement(st);
	        BancoDeDados.desconectar();
	    }
	}
	
	public int atualizarUsuario(Usuario usuario) throws SQLException {
	    PreparedStatement st = null;

	    try {
	        st = conn.prepareStatement(
	            "UPDATE usuario SET nome = ?, senha = ? WHERE usuario = ?"
	        );

	        st.setString(1, usuario.getNome());
	        st.setString(2, usuario.getSenha());
	        st.setString(3, usuario.getUsuario());

	        int retorno = st.executeUpdate();

	        return retorno;

	    } finally {
	        BancoDeDados.finalizarStatement(st);
	        BancoDeDados.desconectar();
	    }
	}
	
	public Usuario buscarPorUsername(String username) throws SQLException {
	    PreparedStatement st = null;
	    ResultSet rs = null;

	    try {
	        st = conn.prepareStatement(
	            "SELECT * FROM usuario WHERE usuario = ?"
	        );

	        st.setString(1, username);

	        rs = st.executeQuery();

	        if (rs.next()) {
	            Usuario u = new Usuario();
	            u.setNome(rs.getString("nome"));
	            u.setUsuario(rs.getString("usuario"));
	            u.setSenha(rs.getString("senha"));
	            return u;
	        }

	        return null;

	    } finally {
	        BancoDeDados.finalizarResultSec(rs);
	        BancoDeDados.finalizarStatement(st);
	        BancoDeDados.desconectar();
	    }
	}
}