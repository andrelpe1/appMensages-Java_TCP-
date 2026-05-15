package entities;

import java.util.List;

public class Mensagem {
	  public String op;
	  
	  public String nome;
	  public String usuario;
	  public String senha;
	  
	  public String resposta;
	  public String mensagem;
	  public String token;
	  public List<Usuario> lista_usuarios;
	  public String token_admin;
	  
	  public List<Usuario> getLista_usuarios() {
		    return lista_usuarios;
		}

		public void setLista_usuarios(List<Usuario> lista_usuarios) {
		    this.lista_usuarios = lista_usuarios;
		}
}
