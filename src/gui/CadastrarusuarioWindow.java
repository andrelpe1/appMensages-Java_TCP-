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
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.awt.event.ActionEvent;

public class CadastrarusuarioWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField usuarioTXT;
	private JTextField nomeTXT;
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
					CadastrarusuarioWindow frame = new CadastrarusuarioWindow(opcoesWindow,ClientSocket,in,out);
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

	public CadastrarusuarioWindow(OpcoesWindow opcoesWindow,Socket ClientSocket,DataInputStream in,PrintStream out) {
		
		this.opcoesWindow = opcoesWindow;
		getContentPane().setLayout(null);
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		iniciarComponentes(ClientSocket, in,out);
	}
	
	private void enviarParaServidor(Socket ClientSocket,DataInputStream in,PrintStream out) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "cadastrarUsuario";
		 msg.usuario = usuarioTXT.getText();
		 msg.nome = nomeTXT.getText();
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
			  if (respostaJson == null) {
			        JOptionPane.showMessageDialog(null, "Servidor desconectado!", "Erro", JOptionPane.ERROR_MESSAGE);
			        fecharConexao();
			        return;
			    }
			   System.out.println("RECEBIDO: "+respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if("200".equals(resposta.resposta)) {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				dispose();
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem + "OOOOOO",String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		}catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
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
	public void iniciarComponentes(Socket ClientSocket,DataInputStream in,PrintStream out) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Cadastro de Usuario");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNewLabel.setBounds(143, 10, 144, 35);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Usuário");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_1.setBounds(20, 57, 55, 21);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_1 = new JLabel("Nome");
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_1_1.setBounds(20, 100, 55, 21);
		contentPane.add(lblNewLabel_1_1);
		
		JLabel lblNewLabel_1_2 = new JLabel("Senha");
		lblNewLabel_1_2.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_1_2.setBounds(20, 155, 55, 21);
		contentPane.add(lblNewLabel_1_2);
		
		usuarioTXT = new JTextField();
		usuarioTXT.setBounds(85, 59, 253, 19);
		contentPane.add(usuarioTXT);
		usuarioTXT.setColumns(10);
		
		nomeTXT = new JTextField();
		nomeTXT.setColumns(10);
		nomeTXT.setBounds(85, 102, 253, 19);
		contentPane.add(nomeTXT);
		
		senhaTXT = new JTextField();
		senhaTXT.setColumns(10);
		senhaTXT.setBounds(85, 157, 253, 19);
		contentPane.add(senhaTXT);
		
		JButton btnNewButton = new JButton("CADASTRAR");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarParaServidor( ClientSocket, in, out);
				
			}
		});
		btnNewButton.setBounds(143, 205, 126, 35);
		contentPane.add(btnNewButton);
	}

}
