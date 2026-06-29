package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cliente.ConexaoCliente;
import entities.Mensagem;
import entities.Usuario;

public class OpcoesWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private final ConexaoWindow conexaoWindow;
    private final ConexaoCliente conexao;

    private String token = null;


    private JTextArea clienteTXT;
    private JTextArea serverTXT;

    public OpcoesWindow(ConexaoWindow conexaoWindow, ConexaoCliente conexao) {
        this.conexaoWindow = conexaoWindow;
        this.conexao       = conexao;

        conexao.setOnMensagemRecebida((remetente, mensagem) ->
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    mensagem,
                    "Mensagem de " + remetente,
                    JOptionPane.INFORMATION_MESSAGE)
            )
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { fecharJanela(); }
        });

        iniciarComponentes();
    }

    public void setToken(String token) {
        this.token = token;
    }

    private void fecharJanela() {
        dispose();
        if (conexaoWindow != null) conexaoWindow.setVisible(true);
    }

    private void abrirCadastro() {
        new CadastrarusuarioWindow(this, conexao).setVisible(true);
        this.setVisible(false);
    }

    private void abrirLogin() {
        new LoginWindow(this, conexao).setVisible(true);
        this.setVisible(false);
    }

    private void abrirAtualizarUsuario() {
        new AtualizarUsuarioWindow(this, conexao, token).setVisible(true);
        this.setVisible(false);
    }

    private void abrirAtualizarUsuarioAdmin() {
        new AtualizarUsuarioAdminWindow(this, conexao, token).setVisible(true);
        this.setVisible(false);
    }

    private void abrirConsultarUsuario(String nome, String usuario) {
        new ConsultarUsuarioWindow(this, nome, usuario).setVisible(true);
        this.setVisible(false);
    }

    private void abrirConsultarUsuarioAdmin(String nome, String usuario) {
        new ConsultarUsuarioAdminWindow(this, nome, usuario).setVisible(true);
        this.setVisible(false);
    }

    private void abrirConsultarUsuariosAdmin() {
        new ConsultarUsuariosAdminWindow(this, conexao, token).setVisible(true);
        this.setVisible(false);
    }

    private void abrirMensagens() {
        new MensagensWindow(conexao, token).setVisible(true);
    }

    private Mensagem enviarEAguardar(Mensagem msg) {
        try {
            String jsonEnviado = conexao.mapper.writeValueAsString(msg);
            clienteTXT.setText(jsonEnviado);
            conexao.enviar(msg);

            Mensagem resp = conexao.aguardarResposta();
            serverTXT.setText(conexao.mapper.writeValueAsString(resp));
            return resp;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(this, "Operação interrompida.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
            fecharConexao();
        }
        return null;
    }

    private void consultarUsuario() {
        Mensagem msg = new Mensagem();
        msg.op    = "consultarUsuario";
        msg.token = token;

        Mensagem resp = enviarEAguardar(msg);
        if (resp == null) return;

        if ("200".equals(resp.resposta)) {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
            abrirConsultarUsuario(resp.nome, resp.usuario);
        } else {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
        }
    }
    


    private void consultarUsuarioAdmin() {
        String usuarioAlvo = JOptionPane.showInputDialog(this, "Digite o username", "Usuário", JOptionPane.INFORMATION_MESSAGE);
        if (usuarioAlvo == null || usuarioAlvo.isBlank()) return;

        Mensagem msg = new Mensagem();
        msg.op      = "consultarUsuarioAdmin";
        msg.token   = token;
        msg.usuario = usuarioAlvo;

        Mensagem resp = enviarEAguardar(msg);
        if (resp == null) return;

        if ("200".equals(resp.resposta)) {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
            abrirConsultarUsuarioAdmin(resp.nome, resp.usuario);
        } else {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletarUsuario() {
        String tokenInformado = JOptionPane.showInputDialog(this, "Digite o token", "Token", JOptionPane.QUESTION_MESSAGE);
        if (tokenInformado == null) return;

        Mensagem msg = new Mensagem();
        msg.op    = "deletarUsuario";
        msg.token = tokenInformado;

        Mensagem resp = enviarEAguardar(msg);
        if (resp == null) return;

        if ("200".equals(resp.resposta)) {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
            setToken(null);
        } else {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletarUsuarioAdmin() {
        String usuarioAlvo = JOptionPane.showInputDialog(this, "Digite o usuario", "Usuário", JOptionPane.INFORMATION_MESSAGE);
        if (usuarioAlvo == null || usuarioAlvo.isBlank()) return;

        Mensagem msg = new Mensagem();
        msg.op      = "deletarUsuarioAdmin";
        msg.token   = token;
        msg.usuario = usuarioAlvo;

        Mensagem resp = enviarEAguardar(msg);
        if (resp == null) return;

        if ("200".equals(resp.resposta)) {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        Mensagem msg = new Mensagem();
        msg.op    = "logout";
        msg.token = token;

        Mensagem resp = enviarEAguardar(msg);
        if (resp == null) return;

        if ("200".equals(resp.resposta)) {
            setToken(null);
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listarUsuariosLogados() {
        if (token == null) {
            JOptionPane.showMessageDialog(this, "Você precisa estar logado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Mensagem msg = new Mensagem();
        msg.op    = "listarUsuariosLogados";
        msg.token = token;

        Mensagem resp = enviarEAguardar(msg);
        if (resp == null) return;

        if ("200".equals(resp.resposta) && resp.lista_usuarios != null) {
            StringBuilder sb = new StringBuilder("Usuários logados (" + resp.lista_usuarios.size() + "):\n");
            for (String u : resp.lista_usuarios) sb.append(" - ").append(u).append("\n");
            JOptionPane.showMessageDialog(this, sb.toString(), "Usuários Online", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fecharConexao() {
        conexao.fechar();
        dispose();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 755, 587);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);


        JLabel lblConectado = new JLabel("Conectado ao servidor");
        lblConectado.setBounds(10, 10, 196, 13);
        contentPane.add(lblConectado);

        JButton btnCadastrar = new JButton("Cadastrar Usuario");
        btnCadastrar.addActionListener(e -> abrirCadastro());
        btnCadastrar.setBounds(24, 63, 158, 37);
        contentPane.add(btnCadastrar);

        JButton btnLogar = new JButton("Logar");
        btnLogar.addActionListener(e -> abrirLogin());
        btnLogar.setBounds(233, 63, 145, 37);
        contentPane.add(btnLogar);

        JButton btnAtualizar = new JButton("Atualizar Usuario");
        btnAtualizar.addActionListener(e -> abrirAtualizarUsuario());
        btnAtualizar.setBounds(24, 121, 158, 37);
        contentPane.add(btnAtualizar);

        JButton btnDeletar = new JButton("Deletar Usuario");
        btnDeletar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Deseja mesmo deletar seu usuario?", "DELETAR USUARIO?", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) deletarUsuario();
        });
        btnDeletar.setBounds(233, 121, 145, 37);
        contentPane.add(btnDeletar);

        JButton btnConsultar = new JButton("Consultar Usuario");
        btnConsultar.addActionListener(e -> consultarUsuario());
        btnConsultar.setBounds(24, 178, 158, 37);
        contentPane.add(btnConsultar);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Deseja mesmo deslogar?", "LOGOUT", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) logout();
        });
        btnLogout.setBounds(233, 178, 145, 37);
        contentPane.add(btnLogout);

        JButton btnMensagem = new JButton("Mandar Mensagem");
        btnMensagem.addActionListener(e -> abrirMensagens());
        btnMensagem.setBounds(24, 235, 158, 37);
        contentPane.add(btnMensagem);

        JButton btnListar = new JButton("Listar Usuarios ONLINE");
        btnListar.addActionListener(e -> listarUsuariosLogados());
        btnListar.setBounds(233, 235, 145, 37);
        contentPane.add(btnListar);

        JButton btnFechar = new JButton("Fechar Conexao");
        btnFechar.addActionListener(e -> fecharConexao());
        btnFechar.setBounds(109, 311, 211, 43);
        contentPane.add(btnFechar);


        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setBounds(452, 10, 20, 343);
        contentPane.add(sep);

        JLabel lblAdmin = new JLabel("ADMIN");
        lblAdmin.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblAdmin.setBounds(562, 10, 72, 49);
        contentPane.add(lblAdmin);

        JButton btnConsultarAdmin = new JButton("Consultar Usuario ADMIN");
        btnConsultarAdmin.addActionListener(e -> consultarUsuarioAdmin());
        btnConsultarAdmin.setBounds(519, 63, 176, 43);
        contentPane.add(btnConsultarAdmin);

        JButton btnConsultarTodosAdmin = new JButton("Consultar todos Usuarios ADMIN");
        btnConsultarTodosAdmin.setFont(new Font("Tahoma", Font.PLAIN, 8));
        btnConsultarTodosAdmin.addActionListener(e -> {
            if ("adm".equals(token)) {
                abrirConsultarUsuariosAdmin();
            } else {
                JOptionPane.showMessageDialog(this, "Precisa ser adm para acessar a lista!", "Token Inválido", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnConsultarTodosAdmin.setBounds(519, 116, 176, 49);
        contentPane.add(btnConsultarTodosAdmin);

        JButton btnAtualizarAdmin = new JButton("Atualizar Usuario ADMIN");
        btnAtualizarAdmin.addActionListener(e -> abrirAtualizarUsuarioAdmin());
        btnAtualizarAdmin.setBounds(519, 178, 176, 47);
        contentPane.add(btnAtualizarAdmin);

        JButton btnDeletarAdmin = new JButton("Deletar Usuario ADMIN");
        btnDeletarAdmin.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Deseja mesmo deletar o usuario?", "DELETAR USUARIO?", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) deletarUsuarioAdmin();
        });
        btnDeletarAdmin.setBounds(519, 235, 176, 54);
        contentPane.add(btnDeletarAdmin);

        JScrollPane scrollCliente = new JScrollPane();
        scrollCliente.setBounds(32, 411, 699, 49);
        contentPane.add(scrollCliente);

        clienteTXT = new JTextArea();
        clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scrollCliente.setViewportView(clienteTXT);
        scrollCliente.setColumnHeaderView(new JLabel("Cliente enviou:"));

        JScrollPane scrollServer = new JScrollPane();
        scrollServer.setBounds(32, 480, 699, 49);
        contentPane.add(scrollServer);

        serverTXT = new JTextArea();
        serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scrollServer.setViewportView(serverTXT);
        scrollServer.setColumnHeaderView(new JLabel("Servidor retornou:"));
    }

    private JPanel contentPane;
}