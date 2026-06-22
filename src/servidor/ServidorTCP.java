package servidor;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;
import entities.Usuario;
import service.UsuarioService;

public class ServidorTCP {
	   private static final ExecutorService threadPool = Executors.newFixedThreadPool(50);
	   private static final ConcurrentHashMap<String, ClienteHandler> usuariosOnline = new ConcurrentHashMap<>();
	   
    public static void main(String args[]) throws IOException {
    	
        UsuarioService service = new UsuarioService();
        System.out.println("Qual porta o servidor deve usar? ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int porta = Integer.parseInt(br.readLine());
        
        criarAdmin(service);
        
        ServerSocket server = new ServerSocket(porta);
        System.out.println("Servidor carregado na porta " + porta);

        while (true) {
            try {
                System.out.println("Aguardando conexão...");
                Socket clientSocket = server.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
             
                threadPool.execute(new ClienteHandler(clientSocket, service));    
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
        
    }
    
    static class ClienteHandler implements Runnable{
    	private final Socket clientSocket;
        private final UsuarioService service;
        private final ObjectMapper mapper = new ObjectMapper();
        private boolean logado = false;
        private String tokenArmazenado = null;

        
        private PrintWriter out;

        private String usuarioLogado = null;

        public ClienteHandler(Socket clientSocket, UsuarioService service) {
            this.clientSocket = clientSocket;
            this.service = service;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String json;
                while ((json = in.readLine()) != null) {
                    System.out.println("[" + Thread.currentThread().getName() + "] Recebido: " + json);

                    Mensagem resposta = new Mensagem();

                    try {
                        Mensagem msg = mapper.readValue(json, Mensagem.class);

                        switch (msg.op.toUpperCase()) {

                            case "CADASTRARUSUARIO":
                                if (msg.nome == null || msg.usuario == null || msg.senha.isEmpty()) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Campos obrigatorios nao preenchidos";
                                    break;
                                }
                                if (!msg.usuario.matches("^[a-zA-Z0-9_]{5,20}$")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Usuario com nome invalido (espacos, caracteres especiais ou nome com menos ou mais caracteres aceitaveis [5 a 20])";
                                    break;
                                }
                                if (!msg.senha.matches("^\\d{6}$")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Senha invalida. Use apenas numeros e exatamente 6 digitos.";
                                    break;
                                }
                                if (service.mostrarUsuario(msg.usuario) != null) {
                                    resposta.resposta = "401";
                                    resposta.op = "cadastrarUsuario";
                                    resposta.mensagem = "Usuario ja cadastrado";
                                    break;
                                }
                                Usuario u = new Usuario();
                                u.setNome(msg.nome);
                                u.setUsuario(msg.usuario);
                                u.setSenha(msg.senha);

                                int res = service.cadastrarUsuario(u);
                                if (res == 1) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Cadastrado com sucesso";
                                } else {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Erro Interno ao cadastrar";
                                }
                                break;

                            case "CONSULTARUSUARIO":
                                String usuarioToken = validarToken(msg.token);
                                if (usuarioToken == null || !logado) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Token invalido";
                                    break;
                                }
                                Usuario retorno = service.mostrarUsuario(usuarioToken);
                                if (retorno != null) {
                                    resposta.nome = retorno.getNome();
                                    resposta.usuario = retorno.getUsuario();
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Consulta realizada com sucesso";
                                } else {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario nao encontrado";
                                }
                                break;

                            case "LOGIN":
                                Usuario user = service.login(msg.usuario, msg.senha);
                                if (user != null) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Login realizado com sucesso";
                                    if (user.getUsuario().equals("admin")) {
                                        resposta.token = "adm";
                                        tokenArmazenado = resposta.token;
                                    } else {
                                        resposta.token = "usr_" + user.getUsuario();
                                        tokenArmazenado = resposta.token;
                                    }
                                    logado = true;
                                    usuarioLogado = user.getUsuario();
                                    usuariosOnline.put(usuarioLogado, this);
                                } else {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Usuario ou senha invalidos";
                                }
                                break;

                            case "LOGOUT":
                                String usuarioLogout = validarToken(msg.token);
                                if (usuarioLogout != null) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Logout efetuado";
                                    if (usuarioLogado != null) {
                                        usuariosOnline.remove(usuarioLogado);
                                    }
                                    tokenArmazenado = null;
                                    logado = false;
                                    usuarioLogado = null;
                                } else {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Erro ao efetuar logout";
                                }
                                break;

                            case "DELETARUSUARIO":
                                String deletarToken = validarToken(msg.token);
                                if (deletarToken == null || !logado) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Token invalido";
                                    break;
                                }
                                if (!msg.token.equals(tokenArmazenado)) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Esse token não é o seu!";
                                    break;
                                }
                                int del = service.deletarUsuario(deletarToken);
                                if (del == 1) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Usuario deletado";
                                    if (usuarioLogado != null) {
                                        usuariosOnline.remove(usuarioLogado);
                                    }
                                    logado = false;
                                    usuarioLogado = null;
                                } else {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario não encontrado";
                                }
                                break;

                            case "ATUALIZARUSUARIO":
                                String atualizarToken = validarToken(msg.token);
                                if (atualizarToken == null || !logado) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Token invalido";
                                    break;
                                }
                                if (msg.nome == null || msg.senha == null || msg.nome.isBlank()) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Campos obrigatorios nao preenchidos";
                                    break;
                                }
                                if (!msg.token.equals(tokenArmazenado)) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Esse token não é o seu!";
                                    break;
                                }
                                if (!msg.senha.matches("^\\d{6}$")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Senha invalida. Use apenas numeros e exatamente 6 digitos.";
                                    break;
                                }
                                Usuario update = new Usuario();
                                update.setNome(msg.nome);
                                update.setUsuario(atualizarToken);
                                update.setSenha(msg.senha);
                                int upd = service.atualizarUsuario(update);
                                if (upd == 1) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Atualizado com sucesso";
                                } else {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Erro Interno ao atualizar";
                                }
                                break;

                            case "CONSULTARUSUARIOSADMIN":
                                String adminConsultas = validarToken(msg.token);
                                if (adminConsultas == null || !logado) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Deve ser ADM para consultar a lista";
                                    break;
                                }
                                if (!adminConsultas.equals("adm")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Deve ser ADM para consultar a lista";
                                    break;
                                }
                                List<Usuario> usuarios = service.listarUsuarios();
                                resposta.resposta = "200";
                                resposta.lista_usuarios = usuarios;
                                break;

                            case "CONSULTARUSUARIOADMIN":
                                String adminConsulta = validarToken(msg.token);
                                if (adminConsulta == null) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Token inválido";
                                    break;
                                }
                                if (!adminConsulta.equals("adm")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Deve ser ADM para consultar a lista";
                                    break;
                                }
                                Usuario retornoUsu = service.mostrarUsuario(msg.usuario);
                                if (retornoUsu != null && logado) {
                                    resposta.nome = retornoUsu.getNome();
                                    resposta.usuario = retornoUsu.getUsuario();
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Consulta realizada com sucesso";
                                } else {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario nao encontrado";
                                }
                                break;

                            case "ATUALIZARUSUARIOADMIN":
                                String adminAtualizar = validarToken(msg.token);
                                if (adminAtualizar == null || !adminAtualizar.equalsIgnoreCase("adm")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Erro ao atualizar o usuario";
                                    break;
                                }
          
                                if (!msg.senha.matches("^\\d{6}$") && !msg.senha.isBlank()) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Senha invalida. Use apenas numeros e exatamente 6 digitos.";
                                    break;
                                }
                                Usuario atual = service.mostrarUsuario(msg.usuario);
                                if (atual == null) {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario nao encontrado";
                                    break;
                                }
                                Usuario update1 = new Usuario();
                                update1.setUsuario(msg.usuario);
                                update1.setNome(  (msg.nome  != null && !msg.nome.isBlank())  ? msg.nome  : atual.getNome());
                                update1.setSenha( (msg.senha != null && !msg.senha.isBlank()) ? msg.senha : atual.getSenha());
                                int upd1 = service.atualizarUsuario(update1);
                                if (upd1 == 1 && logado) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Usuário atualizado com sucesso";
                                } else {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Erro Interno ao atualizar";
                                }
                                break;

                            case "DELETARUSUARIOADMIN":
                                String adminDeletar = validarToken(msg.token);
                                if (adminDeletar == null || !adminDeletar.equalsIgnoreCase("adm")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Deve ser ADM para deletar um usuario";
                                    break;
                                }
                                if(msg.usuario.equals("admin")) {
                                  	 resposta.resposta = "401";
                                       resposta.mensagem = "Não pode deletar admins!";
                                       break;
                                  }
                                int del1 = service.deletarUsuario(msg.usuario);
                                if (del1 == 1 && logado) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Usuario deletado";
                         
                                    ClienteHandler vitima = usuariosOnline.remove(msg.usuario);
                                } else {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario não encontrado";
                                }
                                break;

                            case "ENVIARMENSAGEM": {
                                String remetenteValido = validarToken(msg.token);
                                if (remetenteValido == null || !logado || usuarioLogado == null
                                        || !msg.token.equals(tokenArmazenado)) {
                                    resposta.resposta = "401";
                                    resposta.op = "enviarMensagem";
                                    resposta.mensagem = "Token invalido";
                                    break;
                                }
                                if (msg.destinatario == null || msg.destinatario.isBlank()) {
                                    resposta.resposta = "400";
                                    resposta.op = "enviarMensagem";
                                    resposta.mensagem = "Destinatario obrigatorio";
                                    break;
                                }
                                if (msg.mensagem == null || msg.mensagem.isBlank()) {
                                    resposta.resposta = "400";
                                    resposta.op = "enviarMensagem";
                                    resposta.mensagem = "Mensagem nao pode ser vazia";
                                    break;
                                }

                                if (msg.destinatario.equals("/todos")) {
                                   
                                    int enviados = 0;
                                    for (ClienteHandler cliente : usuariosOnline.values()) {
                                        if (cliente == this) continue;
                                        Mensagem push = new Mensagem();
                                        push.op = "receberMensagem";
                                        push.remetente = usuarioLogado;
                                        push.mensagem = msg.mensagem;
                                        cliente.enviarPush(push);
                                        enviados++;
                                    }
                                    resposta.resposta = "200";
                                    resposta.op = "enviarMensagem";
                                    resposta.mensagem = "Broadcast enviado para " + enviados + " usuario(s) online";
                                } else {
                                    ClienteHandler destino = usuariosOnline.get(msg.destinatario);
                                    if (destino == null) {
                                        resposta.resposta = "404";
                                        resposta.op = "enviarMensagem";
                                        resposta.mensagem = "Usuario destinatario nao encontrado ou offline";
                                        break;
                                    }
                                    Mensagem push = new Mensagem();
                                    push.op = "receberMensagem";
                                    push.remetente = usuarioLogado;
                                    push.mensagem = msg.mensagem;
                                    destino.enviarPush(push);

                                    resposta.resposta = "200";
                                    resposta.op = "enviarMensagem";
                                    resposta.mensagem = "Mensagem enviada com sucesso";
                                }
                                break;
                            }

                            case "LISTARUSUARIOSLOGADOS": {
                                String quemPediu = validarToken(msg.token);
                                if (quemPediu == null || !logado || usuarioLogado == null) {
                                    resposta.resposta = "401";
                                    resposta.op = "listarUsuariosLogados";
                                    resposta.mensagem = "Token invalido";
                                    break;
                                }
                                List<Usuario> logados = new ArrayList<>();
                                for (String nomeLogado : usuariosOnline.keySet()) {
                                    Usuario u1 = new Usuario();
                                    u1.setUsuario(nomeLogado); 
                                    logados.add(u1);
                                }
                                resposta.resposta = "200";
                                resposta.op = "listarUsuariosLogados";
                                resposta.mensagem = "Lista de usuarios logados";
                                resposta.lista_usuarios = logados;
                                break;
                            }
                            default:
                                resposta.resposta = "400";
                                resposta.mensagem = "Comando inválido";
                        }

                        String jsonResposta = mapper.writeValueAsString(resposta);
                        System.out.println("[" + Thread.currentThread().getName() + "] Enviado: " + jsonResposta);
                        out.println(jsonResposta);

                    } catch (Exception e) {
                        resposta.resposta = "400";
                        resposta.mensagem = e.getMessage();
                        resposta.op = "ERRO";
                        out.println(mapper.writeValueAsString(resposta));
                    }
                }

            } catch (IOException e) {
                System.out.println("[" + Thread.currentThread().getName() + "] Cliente desconectado: " + e.getMessage());
            } finally {
                if (usuarioLogado != null) {
                    usuariosOnline.remove(usuarioLogado);
                }
                try {
                    if (in != null) in.close();
                } catch (IOException ignored) {}
                if (out != null) out.close();
                try {
                    clientSocket.close();
                } catch (IOException ignored) {}
            }
        }

        public void enviarPush(Mensagem push) {
            try {
                String json = mapper.writeValueAsString(push);
                out.println(json);
                System.out.println("[PUSH -> " + usuarioLogado + "] " + json);
            } catch (Exception e) {
                System.out.println("Erro ao enviar push para " + usuarioLogado + ": " + e.getMessage());
            }
        }
    }
    
    private static String validarToken(String token) {

        if (token == null) return null;

        if (token.equals("adm")) {
            return "adm";
        }

        if (token.startsWith("usr_")) {
            return token.replace("usr_", "");
        }

        return null;
    }
    
    private static void criarAdmin(UsuarioService service) {
    	try {
			if (service.mostrarUsuario("admin") == null) {

			    Usuario admin = new Usuario();

			    admin.setNome("Administrador");
			    admin.setUsuario("admin");
			    admin.setSenha("123456");
		
			    service.cadastrarUsuario(admin);		  
			}
		} catch (SQLException | IOException e) {
			 System.out.println("Erro: " + e.getMessage());
		}
    }
}