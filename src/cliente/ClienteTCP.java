package cliente;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;

public class ClienteTCP {

    static PrintWriter out;
    static ObjectMapper mapper = new ObjectMapper();

    static BlockingQueue<String> filaRespostas = new LinkedBlockingQueue<>();

    static volatile boolean conectado = true;

    private static void imprimirListaUsuariosLogados(Mensagem resp) {
        if ("200".equals(resp.resposta) && resp.usuariosLogados != null) {
            System.out.println("\nUsuarios logados agora (" + resp.usuariosLogados.size() + "):");
            for (String u : resp.usuariosLogados) {
                System.out.println(" - " + u);
            }
        } else {
            System.out.println("Nao foi possivel obter a lista de usuarios logados: " + resp.mensagem);
        }
    }

    public static void main(String[] args) {
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = null;
        String token = null;

        try {
            System.out.println("Qual o IP do servidor? ");
            String serverIP = teclado.readLine();

            System.out.println("Qual a Porta do servidor? ");
            int serverPort = Integer.parseInt(teclado.readLine());

            System.out.println("Tentando conectar com host " + serverIP + " na porta " + serverPort);

            clientSocket = new Socket(serverIP, serverPort);
            final BufferedReader inSocket = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("Conectado ao Servidor");

            Thread leitor = new Thread(() -> {
                try {
                    String linha;
                    while ((linha = inSocket.readLine()) != null) {
                        Mensagem recebida;
                        try {
                            recebida = mapper.readValue(linha, Mensagem.class);
                        } catch (Exception parseEx) {
                            continue;
                        }

                        if ("receberMensagem".equalsIgnoreCase(recebida.op)) {
                            
                            if (recebida.remetente != null && recebida.mensagem != null) {
                                System.out.println("\n[Mensagem recebida de " + recebida.remetente + "]: " + recebida.mensagem);
                            }
                            
                        } else {
                            filaRespostas.put(linha);
                        }
                    }
                } catch (IOException e) {
                
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    conectado = false;
                }
            }, "leitor-servidor");
            leitor.setDaemon(true);
            leitor.start();

            while (conectado) {
                System.out.println("\nOperações Disponíveis:");
                System.out.println("1 -  cadastrarUsuario");
                System.out.println("2 -  Login");
                System.out.println("3 -  atualizarUsuario");
                System.out.println("4 -  consultarUsuario");
                System.out.println("5 -  deletarUsuario");
                System.out.println("6 -  Logout");
                System.out.println("7 -  consultarUsuariosAdmin");
                System.out.println("8 -  consultarUsuarioAdmin");
                System.out.println("9 -  atualizarUsuarioAdmin");
                System.out.println("10 - deletarUsuarioAdmin");
                System.out.println("11 - enviarMensagem");
                System.out.println("12 - listarUsuariosLogados");
                System.out.println("13 - BYE");
                System.out.println("Escolha uma das operações: ");

                String opcao = teclado.readLine();
                if (opcao == null) break;

                Mensagem msg = new Mensagem();

                switch (opcao.toLowerCase()) {

                    case "1": // CADASTRAR
                        msg.op = "cadastrarUsuario";

                        System.out.print("Nome: ");
                        msg.nome = teclado.readLine();

                        System.out.print("Usuario: ");
                        msg.usuario = teclado.readLine();

                        System.out.print("Senha: ");
                        msg.senha = teclado.readLine();
                        break;

                    case "2": // LOGIN
                        msg.op = "login";

                        System.out.print("Usuario: ");
                        msg.usuario = teclado.readLine();

                        System.out.print("Senha: ");
                        msg.senha = teclado.readLine();
                        break;

                    case "3":
                        msg.op = "atualizarUsuario";
                        System.out.print("Atualizar Nome: ");
                        msg.nome = teclado.readLine();
                        System.out.print("Atualizar Senha: ");
                        msg.senha = teclado.readLine();
                        System.out.print("Mandar token: ");
                        msg.token = teclado.readLine();
                        break;

                    case "4":
                        msg.op = "consultarUsuario";
                        msg.token = token;
                        break;

                    case "5":
                        msg.op = "deletarUsuario";
                        System.out.print("Mandar token: ");
                        msg.token = teclado.readLine();
                        break;

                    case "6":
                        msg.op = "logout";
                        msg.token = token;
                        break;

                    case "7":
                        msg.op = "consultarUsuariosAdmin";
                        msg.token = token;
                        break;

                    case "8":
                        msg.op = "consultarUsuarioAdmin";
                        msg.token = token;
                        System.out.print("Digite o username do Usuario que deseja consultar:");
                        msg.usuario = teclado.readLine();
                        break;

                    case "9":
                        msg.op = "atualizarUsuarioAdmin";
                        msg.token = token;
                        System.out.print("Digite o username do Usuario que deseja atualizar:");
                        msg.usuario = teclado.readLine();
                        System.out.print("Atualizar Nome do usuario: ");
                        msg.nome = teclado.readLine();
                        System.out.print("Atualizar Senha do usuario: ");
                        msg.senha = teclado.readLine();
                        break;

                    case "10":
                        msg.op = "deletarUsuarioAdmin";
                        msg.token = token;
                        System.out.println("Digite o usuario que deseja deletar: ");
                        msg.usuario = teclado.readLine();
                        break;

                    case "11": // ENVIAR MENSAGEM
                        if (token == null) {
                            System.out.println("Você precisa estar logado para enviar mensagens.");
                            continue;
                        }
                        msg.op = "enviarMensagem";
                        msg.token = token;
                        System.out.print("Destinatario (usuario, ou /todos para broadcast): ");
                        msg.destinatario = teclado.readLine();
                        System.out.print("Mensagem: ");
                        msg.mensagem = teclado.readLine();
                        break;

                    case "12": 
                        if (token == null) {
                            System.out.println("Você precisa estar logado.");
                            continue;
                        }
                        msg.op = "listarUsuariosLogados";
                        msg.token = token;
                        break;

                    case "13":
                        System.out.println("Encerrando conexão...");
                        conectado = false;
                        try { clientSocket.close(); } catch (IOException ignored) {}
                        continue;

                    default:
                        System.out.println("Opcao invalida!");
                        continue;
                }

          
                String json = mapper.writeValueAsString(msg);
                out.println(json);
                System.out.println("ENVIADO: " + json);

              
                String respostaJson;
                try {
                    respostaJson = filaRespostas.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                System.out.println("RECEBIDO: " + respostaJson);
                Mensagem respServer = mapper.readValue(respostaJson, Mensagem.class);

                if ("login".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta)) {
                    if (respServer.token != null) {
                        token = respServer.token;
                    }
                }
                if ("logout".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta)) {
                    token = null;
                }

                System.out.println("Resposta: " + respServer.resposta + "\nMensagem: " + respServer.mensagem);
                if ("CONSULTARUSUARIO".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta)) {
                    System.out.println("Nome Retornado: " + respServer.nome + "\nUsuario Retornado: " + respServer.usuario);
                }

            
                if ("login".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta) && token != null) {
                    try {
                        Mensagem listaReq = new Mensagem();
                        listaReq.op = "listarUsuariosLogados";
                        listaReq.token = token;
                        out.println(mapper.writeValueAsString(listaReq));

                        String listaJson = filaRespostas.take();
                        Mensagem listaResp = mapper.readValue(listaJson, Mensagem.class);
                        imprimirListaUsuariosLogados(listaResp);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

            
                if ("listarUsuariosLogados".equalsIgnoreCase(msg.op)) {
                    imprimirListaUsuariosLogados(respServer);
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: ");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IP ou Porta não existe ");
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Falha na conexão com o servidor " + e.getMessage());
        }
    }
}