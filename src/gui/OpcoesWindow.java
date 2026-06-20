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
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Font;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
public class OpcoesWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static  ConexaoWindow conexaoWindow;
	private static Socket ClientSocket = null;
	private JTextArea serverTXT;
	private JTextArea clienteTXT;

	static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
    private String token = null;
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
	
	public void setToken(String token) {
	    this.token = token;
	}
	
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
	
	private void abrirAtualizarUsuario(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		AtualizarUsuarioWindow janelaAtualizar = new AtualizarUsuarioWindow(this,ClientSocket,in,out,token);
		janelaAtualizar.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirAtualizarUsuarioAdmin(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		AtualizarUsuarioAdminWindow janelaAtualizarAdmin = new AtualizarUsuarioAdminWindow(this,ClientSocket,in,out,token);
		janelaAtualizarAdmin.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirConsultarUsuario(String nome, String usuario) {
		ConsultarUsuarioWindow janelaConsultar = new ConsultarUsuarioWindow(this,nome,usuario);
		janelaConsultar.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirConsultarUsuarioAdmin(String nome, String usuario) {
		ConsultarUsuarioAdminWindow janelaConsultarAdmin = new ConsultarUsuarioAdminWindow(this,nome,usuario);
		janelaConsultarAdmin.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirConsultarUsuariosAdmin(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ConsultarUsuariosAdminWindow janelaConsultarUsuariosAdmin = new ConsultarUsuariosAdminWindow(this,ClientSocket,in,out,token);
		janelaConsultarUsuariosAdmin.setVisible(true);
		this.setVisible(false);
	}
	
	private void abrirLogin(Socket ClientSocket,DataInputStream in,PrintStream out) {
		LoginWindow janelaLogin = new LoginWindow(this,ClientSocket,in,out);
		janelaLogin.setVisible(true);
		this.setVisible(false);
	}
	
	private void enviarParaServidorConsultar(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "consultarUsuario";
		 msg.token = token;
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
			clienteTXT.setText(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();;
			   System.out.println("RECEBIDO: "+respostaJson);
			   serverTXT.setText(respostaJson);
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
	
	private void enviarParaServidorConsultarADMIN(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "consultarUsuarioAdmin";
		 msg.token = token;
		 msg.usuario = JOptionPane.showInputDialog(null,"Digite o username", "Usuario",JOptionPane.INFORMATION_MESSAGE);
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
			clienteTXT.setText(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();;
			   System.out.println("RECEBIDO: "+respostaJson);
			   serverTXT.setText(respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if("200".equals(resposta.resposta)) {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				abrirConsultarUsuarioAdmin(resposta.nome,resposta.usuario);
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
	}
	
	private void enviarParaServidorDeletar(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "deletarUsuario";
		 //msg.token = token;
		 msg.token=JOptionPane.showInputDialog(null,"Digite o token", "token",JOptionPane.QUESTION_MESSAGE);
		 
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
			 clienteTXT.setText(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();
			   System.out.println("RECEBIDO: "+respostaJson);
			   serverTXT.setText(respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if("200".equals(resposta.resposta)) {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				setToken(null);
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
	}
	
	private void enviarParaServidorDeletarADMIN(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "deletarUsuarioAdmin";
		 msg.token = token;
		msg.usuario = JOptionPane.showInputDialog(null,"Digite o usuario", "Usuario",JOptionPane.INFORMATION_MESSAGE);
		 
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
			 clienteTXT.setText(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();
			   System.out.println("RECEBIDO: "+respostaJson);
			   serverTXT.setText(respostaJson);
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
	
	private void enviarParaServidorLogout(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "logout";
		 msg.token = token;
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
			 clienteTXT.setText(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();
			   System.out.println("RECEBIDO: "+respostaJson);
			   serverTXT.setText(respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if("200".equals(resposta.resposta)) {
				 setToken(null);
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
		setBounds(100, 100, 847, 614);
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
				abrirAtualizarUsuario(ClientSocket,in,out,token);
			}
		});
		btnAtualizarUsuario.setBounds(24, 121, 158, 37);
		contentPane.add(btnAtualizarUsuario);
		
		JButton btnConsultarUsuario = new JButton("Mandar Mensagem");
		btnConsultarUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarParaServidorConsultar(ClientSocket, in, out,token);
			
			}
		});
		btnConsultarUsuario.setBounds(24, 235, 158, 37);
		contentPane.add(btnConsultarUsuario);
		
		JButton btnDeletarUsuario = new JButton("Deletar Usuario");
		btnDeletarUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int resposta = JOptionPane.showConfirmDialog(null,"Deseja mesmo deletar seu usuario?","DELETAR USUARIO?" ,JOptionPane.YES_NO_OPTION);
				if (resposta == JOptionPane.YES_OPTION) {		   
					enviarParaServidorDeletar( ClientSocket,in, out,token);
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
					enviarParaServidorLogout( ClientSocket,in, out,token);
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
		btnFecharConexao.setBounds(106, 310, 211, 43);
		contentPane.add(btnFecharConexao);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(32, 363, 791, 74);
		contentPane.add(scrollPane);
		
		 clienteTXT = new JTextArea();
		 clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		 scrollPane.setViewportView(clienteTXT);
		 
		 JLabel lblNewLabel_1 = new JLabel("Cliente enviou:");
		 scrollPane.setColumnHeaderView(lblNewLabel_1);
		 
		 JScrollPane scrollPane_1 = new JScrollPane();
		 scrollPane_1.setBounds(32, 464, 791, 82);
		 contentPane.add(scrollPane_1);
		 
		  serverTXT = new JTextArea();
		  serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		  scrollPane_1.setViewportView(serverTXT);
		  
		  JLabel lblNewLabel_1_1 = new JLabel("Servidor retornou");
		  scrollPane_1.setColumnHeaderView(lblNewLabel_1_1);
		  
		  JSeparator separator = new JSeparator();
		  separator.setOrientation(SwingConstants.VERTICAL);
		  separator.setBounds(452, 10, 20, 343);
		  contentPane.add(separator);
		  
		  JLabel lblNewLabel_2 = new JLabel("ADMIN");
		  lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		  lblNewLabel_2.setBounds(614, 10, 72, 49);
		  contentPane.add(lblNewLabel_2);
		  
		  JButton btnConsultarUsuarioAdmin = new JButton("Consultar Usuario ADMIN");
		  btnConsultarUsuarioAdmin.addActionListener(new ActionListener() {
		  	public void actionPerformed(ActionEvent e) {
		  		enviarParaServidorConsultarADMIN( ClientSocket,in, out, token);
		  	}
		  });
		  btnConsultarUsuarioAdmin.setBounds(565, 63, 158, 37);
		  contentPane.add(btnConsultarUsuarioAdmin);
		  
		  JButton btnConsultarUsuariosAdmin = new JButton("Consultar todos Usuarios ADMIN ");
		  btnConsultarUsuariosAdmin.addActionListener(new ActionListener() {
		  	public void actionPerformed(ActionEvent e) {
		  		if("adm".equals(token)) {	
		  			abrirConsultarUsuariosAdmin( ClientSocket, in, out, token);
		  		}else {
		  			JOptionPane.showMessageDialog(null,"Precisa ser adm para acessar a lista!","Token Inválido",JOptionPane.ERROR_MESSAGE);
		  		}
		  	}
		  });
		  btnConsultarUsuariosAdmin.setFont(new Font("Tahoma", Font.PLAIN, 8));
		  btnConsultarUsuariosAdmin.setBounds(565, 122, 158, 37);
		  contentPane.add(btnConsultarUsuariosAdmin);
		  
		  JButton btnAtualizarUsuarioAdmin = new JButton("Atualizar Usuario ADMIN ");
		  btnAtualizarUsuarioAdmin.addActionListener(new ActionListener() {
		  	public void actionPerformed(ActionEvent e) {
		  		abrirAtualizarUsuarioAdmin( ClientSocket, in,out, token);
		  	}
		  });
		  btnAtualizarUsuarioAdmin.setBounds(565, 178, 158, 37);
		  contentPane.add(btnAtualizarUsuarioAdmin);
		  
		  JButton btnDeletarUsuarioAdmin = new JButton("Deletar Usuario ADMIN ");
		  btnDeletarUsuarioAdmin.addActionListener(new ActionListener() {
		  	public void actionPerformed(ActionEvent e) {
		  		int resposta = JOptionPane.showConfirmDialog(null,"Deseja mesmo deletar o usuario?","DELETAR USUARIO?" ,JOptionPane.YES_NO_OPTION);
				if (resposta == JOptionPane.YES_OPTION) {		  
		  		enviarParaServidorDeletarADMIN(ClientSocket,in, out, token);
				}
		  	}
		  });
		  btnDeletarUsuarioAdmin.setBounds(565, 235, 158, 37);
		  contentPane.add(btnDeletarUsuarioAdmin);
		  
		  JButton btnConsultarUsuario_1 = new JButton("Consultar Usuario");
		  btnConsultarUsuario_1.setBounds(24, 178, 158, 37);
		  contentPane.add(btnConsultarUsuario_1);
		  
		  JButton btnListarUsuarios = new JButton("Listar Usuarios");
		  btnListarUsuarios.setBounds(233, 235, 158, 37);
		  contentPane.add(btnListarUsuarios);
	}
}
