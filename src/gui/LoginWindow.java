package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cliente.ConexaoCliente;
import entities.Mensagem;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LoginWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField usuarioTXT;
	private JTextField senhaTXT;
	private static  OpcoesWindow opcoesWindow; 
    private JTextArea serverTXT;
	private JTextArea clienteTXT;
	private static ConexaoCliente conexao;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow frame = new LoginWindow(opcoesWindow,conexao);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void fecharJanela() {
		this.dispose();
		  if (opcoesWindow != null) {
			  opcoesWindow.setVisible(true);
		  }
	}
	
	

	public LoginWindow(OpcoesWindow opcoesWindow,ConexaoCliente conexao) {
		
		 this.opcoesWindow = opcoesWindow;
	     this.conexao      = conexao;
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		iniciarComponentes();
	}
	
	private void enviarParaServidor() {
		 Mensagem msg = new Mensagem();
		 msg.op = "login";
		 msg.usuario = usuarioTXT.getText();
		 msg.senha = senhaTXT.getText();
		 
	
		try {
			String json = conexao.mapper.writeValueAsString(msg);
			 clienteTXT.setText(json);
			 conexao.enviar(msg);
			 Mensagem resp = conexao.aguardarResposta();
			 
			 String jsonRecebido = conexao.mapper.writeValueAsString(resp);
			 serverTXT.setText(jsonRecebido);
			 
			  if ("200".equals(resp.resposta)) {
	              JOptionPane.showMessageDialog(this,resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
	              opcoesWindow.setToken(resp.token);
	               fecharJanela();
			  }else {
	                JOptionPane.showMessageDialog(this,resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
	                }
		}  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(this, "Operação interrompida.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
            conexao.fechar();
            dispose();
        }
	}
	

	/**
	 * Create the frame.
	 */
	public void iniciarComponentes() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 371, 479);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("LOGIN");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel.setBounds(22, 10, 64, 34);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Usuario");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNewLabel_1.setBounds(158, 53, 64, 23);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_1 = new JLabel("Senha");
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNewLabel_1_1.setBounds(158, 130, 64, 23);
		contentPane.add(lblNewLabel_1_1);
		
		usuarioTXT = new JTextField();
		usuarioTXT.setBounds(86, 86, 214, 23);
		contentPane.add(usuarioTXT);
		usuarioTXT.setColumns(10);
		
		senhaTXT = new JTextField();
		senhaTXT.setColumns(10);
		senhaTXT.setBounds(86, 163, 214, 23);
		contentPane.add(senhaTXT);
		
		JButton btnNewButton = new JButton("Logar");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			 enviarParaServidor();  
			}
		});
		btnNewButton.setBounds(124, 213, 122, 32);
		contentPane.add(btnNewButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 274, 346, 72);
		contentPane.add(scrollPane);
		
	clienteTXT = new JTextArea();
		clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane.setViewportView(clienteTXT);
		
		JLabel lblNewLabel_1_2 = new JLabel("Cliente enviou:");
		scrollPane.setColumnHeaderView(lblNewLabel_1_2);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 362, 344, 80);
		contentPane.add(scrollPane_1);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Servidor retornou");
		scrollPane_1.setColumnHeaderView(lblNewLabel_1_1_1);
		
		serverTXT = new JTextArea();
		serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane_1.setViewportView(serverTXT);
	}

}
