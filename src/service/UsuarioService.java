package service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import dao.BancoDeDados;
import entities.Usuario;
import dao.UsuarioDAO;

public class UsuarioService {

	public int cadastrarUsuario(Usuario usuario) throws SQLException, IOException {
		Connection conn = BancoDeDados.conectar();
		return new UsuarioDAO(conn).cadastrarUsuario(usuario);
	}
	
	public int deletarUsuario(String usuario) throws SQLException,IOException{
		Connection conn = BancoDeDados.conectar();
		return new UsuarioDAO(conn).deletarUsuario(usuario);
	}
	
	public int atualizarUsuario(Usuario usuario) throws SQLException,IOException{
		Connection conn = BancoDeDados.conectar();
		return new UsuarioDAO(conn).atualizarUsuario(usuario);
	}
	
	
	public Usuario mostrarUsuario(String usuario) throws SQLException,IOException{
		Connection conn = BancoDeDados.conectar();
		return new UsuarioDAO(conn).buscarPorUsername(usuario);
	}
	
	public Usuario login(String usuario, String senha) throws SQLException, IOException {
	    Connection conn = BancoDeDados.conectar();
	    try {
	        Usuario u = new UsuarioDAO(conn).buscarPorUsername(usuario);

	        if (u != null && u.getSenha().equals(senha)) {
	            return u;
	        }
	        return null;

	    } finally {
	        BancoDeDados.desconectar();
	    }
	}
	
}
