package cliente;

import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;

public class ClienteTCP {
	static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
    public static void main(String[] args) {
    	 //Rotina para entrada de dados via teclado
        DataInputStream teclado = new DataInputStream(System.in);
        ObjectMapper mapper = new ObjectMapper();
        //Geração do socket
        Socket ClientSocket = null;
        String token = null;
        
        try {
        	  System.out.println("Qual o IP do servidor? ");
              BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
              String serverIP = br.readLine();

              System.out.println("Qual a Porta do servidor? ");
              br = new BufferedReader(new InputStreamReader(System.in));
              int serverPort = Integer.parseInt(br.readLine());

              System.out.println("Tentando conectar com host " + serverIP + " na porta " + serverPort);

              ClientSocket = new Socket(serverIP, serverPort);
              in = new DataInputStream(ClientSocket.getInputStream());    // aponta o duto de entrada para o socket do cliente
              out = new PrintStream(ClientSocket.getOutputStream());  
              System.out.println(in.readLine());
              System.out.println("Conectado. Digite (\"bye\" para sair)");
            while (true) {
                System.out.println("\nOperações Disponíveis:");
                System.out.println("	cadastrarUsuario");
                System.out.println("	Login");
                System.out.println("	atualizarUsuario");
                System.out.println("	consultarUsuario");
                System.out.println("	deletarUsuario");
                System.out.println("	Logout");
                System.out.println("	BYE");
                System.out.println("Digite uma das operações: ");

                String opcao = teclado.readLine();

                Mensagem msg = new Mensagem();

                switch (opcao.toLowerCase()) {

                    case "cadastrarusuario": // CADASTRAR
                        msg.op = "cadastrarUsuario";

                        System.out.print("Nome: ");
                        msg.nome = teclado.readLine();

                        System.out.print("Usuario: ");
                        msg.usuario = teclado.readLine();

                        System.out.print("Senha: ");
                        msg.senha = teclado.readLine();
                        break;

                    case "login": // LOGIN
                        msg.op = "LOGIN";

                        System.out.print("Usuario: ");
                        msg.usuario = teclado.readLine();

                        System.out.print("Senha: ");
                        msg.senha = teclado.readLine();
                        break;
                        
                    case "atualizarusuario":
                    	msg.op="atualizarUsuario";
                    	 System.out.print("Atualizar Nome: ");
                    	 msg.nome = teclado.readLine();
                    	 System.out.print("Atualizar Senha: ");
                    	 msg.senha = teclado.readLine();
                    	 msg.token = token;
                    	 break;

                    case "consultarusuario": // VER
                        msg.op= "consultarUsuario";
                        msg.token = token;
                        break;

                    case "deletarusuario": // DELETAR
                        msg.op = "deletarUsuario";   
                        msg.token = token;
                        break;

                    case "logout": // SAIR
                        msg.op = "logout";
                        msg.token = token;
                        break;
                    case "bye":
                    	msg.op = "bye";
                    	break;

                    default:
                        System.out.println("Opcao invalida!");
                        continue;
                }

                // envia JSON
                String json = mapper.writeValueAsString(msg);
                out.println(json);

                // recebe resposta
                String respostaJson = in.readLine();
                Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
                
                if ("LOGIN".equalsIgnoreCase(msg.op) && resposta.status == 200) {
                    token = resposta.token;
                }
                if ("LOGOUT".equalsIgnoreCase(msg.op) && resposta.status == 200) {
                    token = null;
                }
                System.out.println("Resposta: " + resposta.status + "\nMensagem: "+ resposta.mensagem);
                if("CONSULTARUSUARIO".equalsIgnoreCase(msg.op) && resposta.status == 200) {
                	System.out.println("Nome Retornado: "+ resposta.nome+"\nUsuario Retornado: "+resposta.usuario);
                }
                
                if ("BYE".equalsIgnoreCase(msg.op) && resposta.status == 200) {
                    break;
                }
            }

            ClientSocket.close();

        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: ");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IP ou Porta não existe ");
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Falha na conexão com o servidor");
        }
    }
}
