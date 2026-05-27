package gui;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Mensagem;
import entities.Usuario;
import service.UsuarioService;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField portTXT;
    private JTextArea clienteTXTAREA;
    private JTextArea servidorTEXTAREA;
    private ServerSocket server;
    private JButton btnFecharServer;
    private JButton btnAbrir;
    private JLabel lblNewLabel_3;


    private ExecutorService threadPool;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ServidorWindow frame = new ServidorWindow();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

   
    public void servidorTCP(ServerSocket server) {
        UsuarioService service = new UsuarioService();
        criarAdmin(service);

        while (!server.isClosed()) {
            try {
                appendServidor("Aguardando conexão...\n");
                Socket clientSocket = server.accept();
                appendServidor("Cliente conectado: " + clientSocket.getInetAddress() + "\n");

                
                threadPool.execute(new ClienteHandler(clientSocket, service));

            } catch (IOException e) {
                if (server.isClosed()) {
                    appendServidor("Servidor encerrado.\n");
                    break;
                } else {
                    e.printStackTrace();
                }
            }
        }
    }


    private class ClienteHandler implements Runnable {

        private final Socket clientSocket;
        private final UsuarioService service;
        private final ObjectMapper mapper = new ObjectMapper();

        
        private boolean logado = false;
        private String tokenArmazenado = null;

        public ClienteHandler(Socket clientSocket, UsuarioService service) {
            this.clientSocket = clientSocket;
            this.service = service;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String json;
                while ((json = in.readLine()) != null) {
                    final String jsonLog = json;
                    appendCliente("{" + hora() + "} CLIENT: " + jsonLog + "\n");

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
                                    tokenArmazenado = null;
                                    logado = false;
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
                                if (!msg.token.equals(tokenArmazenado)) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Esse token não é o seu!";
                                    break;
                                }
                                if (msg.nome == null || msg.senha == null || msg.nome.isBlank()) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Campos obrigatorios nao preenchidos";
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
                                String adminConsultas = validarToken(msg.token_admin);
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
                                String adminConsulta = validarToken(msg.token_admin);
                                if (adminConsulta == null) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Token inválido";
                                    break;
                                }
                                if (!adminConsulta.equals("adm")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Deve ser ADM para consultar um usuario";
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
                                String adminAtualizar = validarToken(msg.token_admin);
                                if (adminAtualizar == null || !adminAtualizar.equalsIgnoreCase("adm")) {
                                    resposta.resposta = "401";
                                    resposta.mensagem = "Erro ao atualizar o usuario";
                                    break;
                                }
                               
                                if (msg.senha != null && !msg.senha.isBlank() && !msg.senha.matches("^\\d{6}$")) {
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
                                String adminDeletar = validarToken(msg.token_admin);
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
                                } else {
                                    resposta.resposta = "404";
                                    resposta.mensagem = "Usuario não encontrado";
                                }
                                break;

                            default:
                                resposta.resposta = "400";
                                resposta.mensagem = "Comando inválido";
                        }

                        String jsonResposta = mapper.writeValueAsString(resposta);
                       
                        appendServidor("{" + hora() + "} SERVER: " + jsonResposta + "\n");
                        out.println(jsonResposta);

                    } catch (Exception e) {
                        resposta.resposta = "400";
                        resposta.mensagem = e.getMessage();
                        resposta.op = "ERRO";
                        try {
                            out.println(mapper.writeValueAsString(resposta));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            } catch (IOException e) {
                appendServidor("Cliente desconectado.\n");
            } 
            
        }
    }

    
    private void appendServidor(String texto) {
        SwingUtilities.invokeLater(() -> servidorTEXTAREA.append(texto));
    }

    private void appendCliente(String texto) {
        SwingUtilities.invokeLater(() -> clienteTXTAREA.append(texto));
    }

    private static String hora() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private static String validarToken(String token) {
        if (token == null) return null;
        if (token.equals("adm")) return "adm";
        if (token.startsWith("usr_")) return token.replace("usr_", "");
        return null;
    }

    private void colocarPorta() {
        int porta = Integer.parseInt(portTXT.getText());
        try {
            threadPool = Executors.newFixedThreadPool(50); 
            server = new ServerSocket(porta);
            new Thread(() -> servidorTCP(server)).start(); 
            btnAbrir.setEnabled(false);
            btnFecharServer.setEnabled(true);
            appendServidor("Servidor carregado na porta " + porta + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fecharServer() {
        if (server != null && !server.isClosed()) {
            try {
                threadPool.shutdownNow(); 
                server.close();
                JOptionPane.showMessageDialog(null, "Servidor foi fechado", "FECHADO", JOptionPane.INFORMATION_MESSAGE);
                btnFecharServer.setEnabled(false);
                btnAbrir.setEnabled(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparComponentes() {
        servidorTEXTAREA.setText("");
        clienteTXTAREA.setText("");
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

    public ServidorWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 925, 687);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblNewLabel = new JLabel("SERVIDOR");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 19));
        lblNewLabel.setBounds(399, -1, 90, 43);
        contentPane.add(lblNewLabel);

        portTXT = new JTextField();
        portTXT.setBounds(166, 52, 96, 43);
        contentPane.add(portTXT);
        portTXT.setColumns(10);

        JLabel lblNewLabel_1 = new JLabel("Porta do servidor");
        lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 19));
        lblNewLabel_1.setBounds(10, 55, 146, 29);
        contentPane.add(lblNewLabel_1);

        btnAbrir = new JButton("ABRIR SERVER");
        btnAbrir.addActionListener(e -> colocarPorta());
        btnAbrir.setBounds(482, 52, 146, 43);
        contentPane.add(btnAbrir);

        btnFecharServer = new JButton("FECHAR SERVER");
        btnFecharServer.addActionListener(e -> fecharServer());
        btnFecharServer.setBounds(638, 52, 146, 43);
        btnFecharServer.setEnabled(false);
        contentPane.add(btnFecharServer);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 105, 427, 498);
        contentPane.add(scrollPane);

        JLabel lblNewLabel_2 = new JLabel("CLIENTE MANDOU");
        scrollPane.setColumnHeaderView(lblNewLabel_2);

        clienteTXTAREA = new JTextArea();
        clienteTXTAREA.setFont(new Font("SansSerif", Font.PLAIN, 13));
        clienteTXTAREA.setEditable(false);
        scrollPane.setViewportView(clienteTXTAREA);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(482, 104, 419, 499);
        contentPane.add(scrollPane_1);

        servidorTEXTAREA = new JTextArea();
        servidorTEXTAREA.setFont(new Font("SansSerif", Font.PLAIN, 13));
        servidorTEXTAREA.setEditable(false);
        scrollPane_1.setViewportView(servidorTEXTAREA);

        lblNewLabel_3 = new JLabel("SERVIDOR RETORNOU");
        scrollPane_1.setColumnHeaderView(lblNewLabel_3);

        JButton btnapagar = new JButton("apagar");
        btnapagar.addActionListener(e -> limparComponentes());
        btnapagar.setBounds(419, 619, 85, 21);
        contentPane.add(btnapagar);
    }
}