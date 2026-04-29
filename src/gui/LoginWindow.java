package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class LoginWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField usuarioTXT;
	private JTextField senhaTXT;
	private static  OpcoesWindow opcoesWindow;
	private static Socket ClientSocket = null;
	static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow frame = new LoginWindow(opcoesWindow,ClientSocket,in,out);
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
	
	

	public LoginWindow(OpcoesWindow opcoesWindow,Socket ClientSocket,DataInputStream in,PrintStream out) {
		
		this.opcoesWindow = opcoesWindow;
		getContentPane().setLayout(null);
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		iniciarComponentes(opcoesWindow,ClientSocket,in,out);
	}
	
	private String enviarParaServidor(Socket ClientSocket,DataInputStream in,PrintStream out) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "login";
		 msg.usuario = usuarioTXT.getText();
		 msg.senha = senhaTXT.getText();
		 
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();
			   System.out.println("RECEBIDO: "+respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if("200".equals(resposta.resposta)) {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				return resposta.token;
				
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
	
		return null;
	}
	
	private void fecharConexao() {
		try {
			ClientSocket.close();
			dispose();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null,"Erro ao fechar conexão", e1.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Create the frame.
	 */
	public void iniciarComponentes(OpcoesWindow opcoesWindow,Socket ClientSocket,DataInputStream in,PrintStream out) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 391, 298);
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
			//	 opcoesWindow.setToken(enviarParaServidor(ClientSocket, in, out));  
				 if(enviarParaServidor(ClientSocket, in, out) != null) {
					 dispose();
				 };  
				 
			}
		});
		btnNewButton.setBounds(139, 232, 85, 21);
		contentPane.add(btnNewButton);
	}

}
