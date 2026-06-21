package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cliente.ConexaoCliente;

public class ConexaoWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel     contentPane;
    private JTextField IPtxtField;
    private JTextField portaTxtField;

    public ConexaoWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 372, 348);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JLabel lblIp = new JLabel("IP DO SERVIDOR");
        lblIp.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblIp.setBounds(104, 10, 128, 44);
        contentPane.add(lblIp);

        IPtxtField = new JTextField(10);
        IPtxtField.setBounds(114, 56, 128, 27);
        contentPane.add(IPtxtField);

        JLabel lblPorta = new JLabel("PORTA DO SERVIDOR");
        lblPorta.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblPorta.setBounds(104, 98, 172, 44);
        contentPane.add(lblPorta);

        portaTxtField = new JTextField(10);
        portaTxtField.setBounds(114, 138, 128, 27);
        contentPane.add(portaTxtField);

        JButton btnEnviar = new JButton("ENVIAR");
        btnEnviar.addActionListener(e -> conectar());
        btnEnviar.setBounds(114, 214, 116, 21);
        contentPane.add(btnEnviar);
    }

    private void conectar() {
        String ip    = IPtxtField.getText().trim();
        String porta = portaTxtField.getText().trim();

        if (ip.isEmpty() || porta.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Preencha o IP e a Porta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int serverPorta = Integer.parseInt(porta);

            Socket socket = new Socket(ip, serverPorta);
            ConexaoCliente conexao = new ConexaoCliente(socket);

            abrirOpcoes(conexao);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Porta inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(this,
                "Host desconhecido.", "Erro ao conectar ao servidor", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "IP ou Porta não existe.", "Erro ao conectar ao servidor", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Falha na conexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirOpcoes(ConexaoCliente conexao) {
        OpcoesWindow janelaOpcoes = new OpcoesWindow(this, conexao);
        janelaOpcoes.setVisible(true);
        this.setVisible(false);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new ConexaoWindow().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}