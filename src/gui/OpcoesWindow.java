package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import entities.Mensagem;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.awt.event.ActionEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
public class OpcoesWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static  ConexaoWindow conexaoWindow;
	private static Socket ClientSocket = null;
	//private String token = null;

	static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OpcoesWindow frame = new OpcoesWindow(conexaoWindow,ClientSocket,in,out);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void fecharJanela() {
		this.dispose();
		  if (conexaoWindow != null) {
			  conexaoWindow.setVisible(true);
		  }
	}
	
	/*public void setToken(String token) {
	    this.token = token;
	}*/
	public OpcoesWindow(ConexaoWindow conexaoWindow,Socket ClientSocket,DataInputStream in,PrintStream out) {
		
		this.conexaoWindow = conexaoWindow;
		getContentPane().setLayout(null);
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		iniciarComponenetes(ClientSocket,in,out);
	}
	
	
	private void abrirCadastro(Socket ClientSocket,DataInputStream in,PrintStream out) {
		CadastrarusuarioWindow  janelaCadastro = new CadastrarusuarioWindow(this,ClientSocket,in,out);
		janelaCadastro.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirAtualizarUsuario(Socket ClientSocket,DataInputStream in,PrintStream out)
	{
		AtualizarUsuarioWindow janelaAtualizar = new AtualizarUsuarioWindow(this,ClientSocket,in,out);
		janelaAtualizar.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirConsultarUsuario(String nome, String usuario) {
		ConsultarUsuarioWindow janelaConsultar = new ConsultarUsuarioWindow(this,nome,usuario);
		janelaConsultar.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirLogin(Socket ClientSocket,DataInputStream in,PrintStream out) {
		LoginWindow janelaLogin = new LoginWindow(this,ClientSocket,in,out);
		janelaLogin.setVisible(true);
		this.setVisible(false);
	}
	
	private void enviarParaServidorConsultar(Socket ClientSocket,DataInputStream in,PrintStream out) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "consultarUsuario";
		 //msg.token = token;
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();;
			   System.out.println("RECEBIDO: "+respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if("200".equals(resposta.resposta)) {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				abrirConsultarUsuario(resposta.nome,resposta.usuario);
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
	}
	
	private void enviarParaServidorDeletar(Socket ClientSocket,DataInputStream in,PrintStream out) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "deletarUsuario";
		// msg.token = token;
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
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
	}
	
	private void enviarParaServidorLogout(Socket ClientSocket,DataInputStream in,PrintStream out) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "logout";
		// msg.token = token;
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
				 //setToken(null);
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
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
	public void iniciarComponenetes(Socket ClientSocket,DataInputStream in,PrintStream out) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 400, 363);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Conectado ao servidor");
		lblNewLabel.setBounds(10, 10, 196, 13);
		contentPane.add(lblNewLabel);
		
		JButton btnLogar = new JButton("Logar");
		btnLogar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abrirLogin(ClientSocket,in,out);
			}
		});
		btnLogar.setBounds(233, 63, 145, 37);
		contentPane.add(btnLogar);
		
		JButton btnCadastrarUsuario = new JButton("Cadastrar Usuario");
		btnCadastrarUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abrirCadastro(ClientSocket,in,out);
			}
		});
		btnCadastrarUsuario.setBounds(24, 63, 158, 37);
		contentPane.add(btnCadastrarUsuario);
		
		JButton btnAtualizarUsuario = new JButton("Atualizar Usuario");
		btnAtualizarUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abrirAtualizarUsuario(ClientSocket,in,out);
			}
		});
		btnAtualizarUsuario.setBounds(24, 121, 158, 37);
		contentPane.add(btnAtualizarUsuario);
		
		JButton btnConsultarUsuario = new JButton("Consultar Usuario");
		btnConsultarUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarParaServidorConsultar(ClientSocket, in, out);
			
			}
		});
		btnConsultarUsuario.setBounds(24, 178, 158, 37);
		contentPane.add(btnConsultarUsuario);
		
		JButton btnDeletarUsuario = new JButton("Deletar Usuario");
		btnDeletarUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int resposta = JOptionPane.showConfirmDialog(null,"Deseja mesmo deletar seu usuario?","DELETAR USUARIO?" ,JOptionPane.YES_NO_OPTION);
				if (resposta == JOptionPane.YES_OPTION) {		   
					enviarParaServidorDeletar( ClientSocket,in, out);
				} 
			}
		});
		btnDeletarUsuario.setBounds(233, 121, 145, 37);
		contentPane.add(btnDeletarUsuario);
		
		JButton btnLogout = new JButton("Logout");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int resposta = JOptionPane.showConfirmDialog(null,"Deseja mesmo deslogar?","LOGOUT" ,JOptionPane.YES_NO_OPTION);
				if (resposta == JOptionPane.YES_OPTION) {		   
					enviarParaServidorLogout( ClientSocket,in, out);
				} 
			}
		});
		btnLogout.setBounds(233, 178, 145, 37);
		contentPane.add(btnLogout);
		
		JButton btnFecharConexao = new JButton("Fechar Conexao");
		btnFecharConexao.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ClientSocket.close();
					dispose();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null,"Erro ao fechar conexão", e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		btnFecharConexao.setBounds(75, 273, 211, 43);
		contentPane.add(btnFecharConexao);
	}

}
