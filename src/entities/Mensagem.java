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
    public List<Usuario> lista_usuarios1;
    public List<String> lista_usuarios;
    public String destinatario;
    public String remetente;
}