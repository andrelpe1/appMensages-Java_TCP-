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
              System.out.println("Conectado ao Servidor");
            while (true) {
                System.out.println("\nOperações Disponíveis:");
                System.out.println("1 -	cadastrarUsuario");
                System.out.println("2 -	Login");
                System.out.println("3 -	atualizarUsuario");
                System.out.println("4 -	consultarUsuario");
                System.out.println("5 -	deletarUsuario");
                System.out.println("6 - 	Logout");
                System.out.println("7 -	BYE");
                System.out.println("Escolha uma das operações: ");

                String opcao = teclado.readLine();

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
                    	msg.op="atualizarUsuario";
                    	 System.out.print("Atualizar Nome: ");
                    	 msg.nome = teclado.readLine();
                    	 System.out.print("Atualizar Senha: ");
                    	 msg.senha = teclado.readLine();
                    	 msg.token = token;
                    	 break;

                    case "4": // VER
                        msg.op= "consultarUsuario";
                        msg.token = token;
                        break;

                    case "5": // DELETAR
                        msg.op = "deletarUsuario";   
                        msg.token = token;
                        break;

                    case "6": // SAIR
                        msg.op = "logout";
                        msg.token = token;
                        break;
                    case "7":
                    	 ClientSocket.close();
                    	break;

                    default:
                        System.out.println("Opcao invalida!");
                        continue;
                }

                // envia JSON
                String json = mapper.writeValueAsString(msg);
                out.println(json);
                System.out.println("ENVIADO: "+json);

                // recebe resposta
                String respostaJson = in.readLine();
                System.out.println("RECEBIDO: "+respostaJson);
                Mensagem respServer = mapper.readValue(respostaJson, Mensagem.class);
                
              
                if ("login".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta)) {
                
                    token = respServer.token;
     
                }
                if ("logout".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta) ){
                    token = null;
                    System.out.println(token);
                }
                
                
                System.out.println("Resposta: " + respServer.resposta + "\nMensagem: "+ respServer.mensagem);
                if("CONSULTARUSUARIO".equalsIgnoreCase(msg.op) && "200".equals(respServer.resposta)) {
                	System.out.println("Nome Retornado: "+ respServer.nome+"\nUsuario Retornado: "+respServer.usuario);
                }
            }

           

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
