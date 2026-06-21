package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

import cliente.ConexaoCliente;
import entities.Mensagem;

public class MensagensWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel     contentPane;
    private JTextField destinatarioTXT;
    private JTextField mensagemTXT;
    private JTextPane  chatTEXTPANE;
    private JTextArea  clienteTXT;
    private JTextArea  serverTXT;

    private final ConexaoCliente conexao;
    private final String         token;

    public MensagensWindow(ConexaoCliente conexao, String token) {
        this.conexao = conexao;
        this.token   = token;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { fecharJanela(); }
        });

        conexao.setOnMensagemRecebida((remetente, mensagem) ->
            SwingUtilities.invokeLater(() ->
                adicionarMensagemEsquerda("[" + remetente + "]: " + mensagem)
            )
        );

        iniciarComponentes();
    }

    private void fecharJanela() {
        conexao.setOnMensagemRecebida(null);
        dispose();
    }


    private void enviarMensagem() {
        String destinatario = destinatarioTXT.getText().trim();
        String texto        = mensagemTXT.getText().trim();

        if (destinatario.isEmpty() || texto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Preencha o destinatário e a mensagem.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mensagem msg = new Mensagem();
        msg.op          = "enviarMensagem";
        msg.token       = token;
        msg.destinatario = destinatario;
        msg.mensagem    = texto;

        try {
            String jsonEnviado = conexao.mapper.writeValueAsString(msg);
            clienteTXT.setText(jsonEnviado);
            System.out.println("ENVIADO: " + jsonEnviado);

            conexao.enviar(msg);

            Mensagem resp = conexao.aguardarResposta();

            String jsonRecebido = conexao.mapper.writeValueAsString(resp);
            serverTXT.setText(jsonRecebido);
            System.out.println("RECEBIDO: " + jsonRecebido);

            if ("200".equals(resp.resposta)) {
                adicionarMensagemDireita("Você: " + texto);
                mensagemTXT.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                    resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(this, "Operação interrompida.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
            conexao.fechar();
            dispose();
        }
    }

    private void adicionarMensagemEsquerda(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatTEXTPANE.getStyledDocument();
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setForeground(estilo, Color.BLUE);
            StyleConstants.setAlignment(estilo, StyleConstants.ALIGN_LEFT);
            try {
                doc.insertString(doc.getLength(), mensagem + "\n", estilo);
                doc.setParagraphAttributes(doc.getLength() - mensagem.length() - 1,
                    mensagem.length() + 1, estilo, false);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void adicionarMensagemDireita(String mensagem) {
        StyledDocument doc = chatTEXTPANE.getStyledDocument();
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setForeground(estilo, Color.RED);
        StyleConstants.setAlignment(estilo, StyleConstants.ALIGN_RIGHT);
        try {
            doc.insertString(doc.getLength(), mensagem + "\n", estilo);
            doc.setParagraphAttributes(doc.getLength() - mensagem.length() - 1,
                mensagem.length() + 1, estilo, false);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1190, 709);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);


        chatTEXTPANE = new JTextPane();
        chatTEXTPANE.setBackground(new Color(192, 192, 192));
        chatTEXTPANE.setFont(new Font("Arial", Font.PLAIN, 16));
        chatTEXTPANE.setEditable(false);
        chatTEXTPANE.setBounds(23, 10, 765, 391);
        contentPane.add(chatTEXTPANE);


        JLabel lblDestinatario = new JLabel("Destinatario");
        lblDestinatario.setFont(new Font("Yu Gothic UI", Font.PLAIN, 14));
        lblDestinatario.setBounds(10, 431, 92, 33);
        contentPane.add(lblDestinatario);

        destinatarioTXT = new JTextField(10);
        destinatarioTXT.setBounds(112, 438, 489, 24);
        contentPane.add(destinatarioTXT);

        JLabel lblDica = new JLabel("Se deseja enviar para todos online digite \"/todos\"");
        lblDica.setBounds(111, 415, 289, 13);
        contentPane.add(lblDica);


        JLabel lblMensagem = new JLabel("Mensagem");
        lblMensagem.setFont(new Font("Yu Gothic UI", Font.PLAIN, 14));
        lblMensagem.setBounds(10, 485, 78, 58);
        contentPane.add(lblMensagem);

        mensagemTXT = new JTextField(10);
        mensagemTXT.setBounds(112, 472, 489, 103);
        contentPane.add(mensagemTXT);


        JButton btnEnviar = new JButton("Enviar Mensagem");
        btnEnviar.addActionListener(e -> enviarMensagem());
        btnEnviar.setBounds(209, 585, 232, 42);
        contentPane.add(btnEnviar);

        JScrollPane scrollCliente = new JScrollPane();
        scrollCliente.setBounds(824, 62, 342, 91);
        scrollCliente.setColumnHeaderView(new JLabel("Cliente enviou:"));
        contentPane.add(scrollCliente);

        clienteTXT = new JTextArea();
        clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scrollCliente.setViewportView(clienteTXT);

        JScrollPane scrollServer = new JScrollPane();
        scrollServer.setBounds(822, 179, 344, 93);
        scrollServer.setColumnHeaderView(new JLabel("Servidor retornou:"));
        contentPane.add(scrollServer);

        serverTXT = new JTextArea();
        serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scrollServer.setViewportView(serverTXT);
    }
}