package servidor;

import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;
import entities.Usuario;
import service.UsuarioService;

public class ServidorTCP {

    public static void main(String args[]) throws IOException {

        ServerSocket server;
        Socket clientSocket;
        String token = null;
        UsuarioService service = new UsuarioService();
        ObjectMapper mapper = new ObjectMapper();
        boolean logado = false;
        System.out.println("Qual porta o servidor deve usar? ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int porta = Integer.parseInt(br.readLine());

        server = new ServerSocket(porta);
        System.out.println("Servidor carregado na porta " + porta);

        while (true) {
            try {
                System.out.println("Aguardando conexão...");
                clientSocket = server.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true
                );


                //out.println("Conectado ao servidor");
                
                System.out.println("Cliente conectado!");
                while (true) {
                	String json = in.readLine();
                    if (json == null) break;

                    System.out.println("Recebido: " + json);
                 
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
                                if((!msg.usuario.matches("^[a-zA-Z0-9_]{5,20}$"))) {
                                	resposta.resposta = "401";
                                	
                                	resposta.mensagem = "Usuario com nome invalido (espacos, caracteres especiais ou nome com menos ou mais caracteres aceitaveis [5 a 20])";
                                	break;
                                }
                                if(!msg.senha.matches("^\\d{6}$")) {
                                	resposta.resposta = "401";
                          
                                	resposta.mensagem = "Senha invalida. Use apenas numeros e exatamente 6 digitos.";
                                	break;
                                }
                                if(service.mostrarUsuario(msg.usuario) != null) {
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
                                    resposta.resposta  = "200";
                                    resposta.mensagem = "Cadastrado com sucesso";
                           
                                } else {
                                    resposta.resposta  = "401";
                                    resposta.mensagem = "Erro Interno ao cadastrar";
                           
                                }
                                break;
                                
                            case "CONSULTARUSUARIO":

                                String usuarioToken = validarToken(msg.token);

                                if (usuarioToken == null || !logado) {
                                    resposta.resposta  = "401";
                                    resposta.mensagem = "Token invalido";
                            
                                    break;
                                }

                                Usuario retorno = service.mostrarUsuario(usuarioToken);

                                if (retorno != null && logado) {
                                    resposta.nome = retorno.getNome();
                                    resposta.usuario = retorno.getUsuario();
                                    resposta.resposta  = "200";
                                    resposta.mensagem = "Consulta realizada com sucesso";
                                  
                                } else {
                                    resposta.resposta  = "404";
                                    resposta.mensagem = "Usuario nao encontrado";
                                 
                                }

                                break;
                            	
                            case "LOGIN":
                                Usuario user = service.login(msg.usuario, msg.senha);
                                
                                if (user != null) {
                                    resposta.resposta  = "200";
                                    resposta.mensagem = "Login realizado com sucesso";
                                    resposta.token = "usr_"+user.getUsuario();
                                    logado = true;
                     
                                } else {
                                    resposta.resposta  = "401";
                                    resposta.mensagem = "Usuario ou senha invalidos";
                             
                                }
                                break;
                                
                            case "LOGOUT":

                            	 String usuarioLogout = validarToken(msg.token);
                            	 if (usuarioLogout != null) {
                            	        resposta.resposta  = "200";
                            	        resposta.mensagem = "Logout efetuado";
                            	        //token = null;
                            	        logado = false;
                            	    
                            	    } else {
                            	        resposta.resposta = "401";
                            	        resposta.mensagem = "Erro ao efetuar logout";
                            	       
                            	    }
                                break;

                            case "DELETARUSUARIO":
                            	String deletarToken = validarToken(msg.token);	
                            	 if ((deletarToken == null) || (!logado)) {
                            	        resposta.resposta  = "401";
                            	        resposta.mensagem = "Token invalido";
                            	  
                            	        break;
                            	    }
                                int del = service.deletarUsuario(deletarToken);

                                if (del == 1 && logado) {
                                    resposta.resposta = "200";
                                    resposta.mensagem = "Usuario deletado";
                                  
                                } else {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario não encontrado";
                                  
                                }
                                break;
                                
                            case "ATUALIZARUSUARIO":
                            	String atualizarToken = validarToken(msg.token);
                            	if ((atualizarToken == null) || (!logado)) {
                        	        resposta.resposta  = "401";
                        	        resposta.mensagem = "Token invalido";
                        	     
                        	        break;
                        	    }
                            	if (msg.nome == null || msg.senha == null || msg.nome.isBlank()) {
                                    resposta.resposta  = "401";
                                    resposta.mensagem = "Campos obrigatorios nao preenchidos";
                           
                                    break;
                                }
                            	 if(!msg.senha.matches("^\\d{6}$")) {
                                 	resposta.resposta = "401";
                                 	resposta.mensagem = "Senha invalida. Use apenas numeros e exatamente 6 digitos.";
                                
                                 	break;
                                 }
                                Usuario update = new Usuario();
                                update.setNome(msg.nome);
                                update.setUsuario(atualizarToken);
                                update.setSenha(msg.senha);
                            	int upd = service.atualizarUsuario(update);
                            	  if (upd == 1 && logado) {
                                      resposta.resposta  = "200";
                                      resposta.mensagem = "Atualizado com sucesso";
                                  
                                  } else {
                                      resposta.resposta  = "401";
                                      resposta.mensagem = "Erro Interno ao atualizar";
                                    
                                  }	
                            	break;



                            default:
                                resposta.resposta = "400";
                                resposta.mensagem = "Comando inválido";
                        }
                        System.out.println("Enviado: " + mapper.writeValueAsString(resposta));

           
                            out.println(mapper.writeValueAsString(resposta));
                       /* if ("login".equalsIgnoreCase(msg.op) && resposta.resposta == "200") {
                            token = resposta.token;
                        }
                        if ("logout".equalsIgnoreCase(msg.op) && resposta.resposta == "200") {
                            token = null;
                        }*/

                    } catch (Exception e) {
                        resposta.resposta  = "400";
                        resposta.mensagem = e.getMessage();
                        resposta.op = "ERRO";

                        out.println(mapper.writeValueAsString(resposta));
                        
                    }
                }

            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
        
    }
    
    
    private static String validarToken(String token) {

        if (token == null) return null;

        if (token.equals("adm")) {
            return "admin";
        }

        if (token.startsWith("usr_")) {
            return token.replace("usr_", "");
        }

        return null;
    }
}
