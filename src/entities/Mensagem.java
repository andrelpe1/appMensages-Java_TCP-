package entities;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Mensagem {
	  public String op;
	  
	  public String nome;
	  public String usuario;
	  public String senha;
	  
	  public String resposta;
	  public String mensagem;
	  public String token;
	  public List<Usuario> lista_usuarios;

	  // Usados pela funcionalidade de envio de mensagens entre usuarios
	  public String destinatario; // usuario destino, ou "/todos" para broadcast
	  public String remetente;    // preenchido pelo servidor ao repassar a mensagem para o destinatario

	  // Usado pela funcionalidade de listar usuarios logados
	  public List<String> usuariosLogados;
	  
	  public List<Usuario> getLista_usuarios() {
		    return lista_usuarios;
		}
		public void setLista_usuarios(List<Usuario> lista_usuarios) {
		    this.lista_usuarios = lista_usuarios;
		}
}