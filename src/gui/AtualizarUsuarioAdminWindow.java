package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class AtualizarUsuarioAdminWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField nomeTXT;
	private JTextField senhaTXT;
	private static  OpcoesWindow opcoesWindow;

	private static Socket ClientSocket = null;
	static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
    private static String token = null;
    private JTextArea serverTXT;
   	private JTextArea clienteTXT;
   	private String armazenaUsername = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AtualizarUsuarioWindow frame = new AtualizarUsuarioWindow(opcoesWindow,ClientSocket,in,out,token);
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

	public AtualizarUsuarioAdminWindow(OpcoesWindow opcoesWindow,Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		
		this.opcoesWindow = opcoesWindow;
		getContentPane().setLayout(null);
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		iniciarComponentes(ClientSocket, in, out,token);
	}
	
	private boolean enviarParaServidor(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "atualizarUsuarioAdmin";
		 msg.nome = nomeTXT.getText();
		 msg.senha = senhaTXT.getText();
		 msg.usuario = armazenaUsername;
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
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				return true;
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    fecharConexao();
		}
	
		return false;
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
	public void iniciarComponentes(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 484, 524);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Atualizar Usuario ADMIN");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblNewLabel.setBounds(10, 10, 250, 29);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Nome");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel_1.setBounds(63, 171, 45, 13);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_1 = new JLabel("Senha");
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel_1_1.setBounds(63, 209, 45, 13);
		contentPane.add(lblNewLabel_1_1);
		
		nomeTXT = new JTextField();
		nomeTXT.setToolTipText("");
		nomeTXT.setBounds(129, 170, 287, 19);
		contentPane.add(nomeTXT);
		nomeTXT.setColumns(10);
		
		senhaTXT = new JTextField();
		senhaTXT.setToolTipText("");
		senhaTXT.setColumns(10);
		senhaTXT.setBounds(129, 203, 287, 19);
		contentPane.add(senhaTXT);
		
		JButton btnAtualizar = new JButton("ATUALIZAR");
		btnAtualizar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(enviarParaServidor(ClientSocket,in,out,token)) {
					//dispose();
				};
			}
		});
		btnAtualizar.setBounds(182, 232, 127, 29);
		contentPane.add(btnAtualizar);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(49, 283, 342, 91);
		contentPane.add(scrollPane);
		
		 clienteTXT = new JTextArea();
		clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane.setViewportView(clienteTXT);
		
		JLabel lblNewLabel_1_2 = new JLabel("Cliente enviou:");
		scrollPane.setColumnHeaderView(lblNewLabel_1_2);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(47, 384, 344, 93);
		contentPane.add(scrollPane_1);
		
		 serverTXT = new JTextArea();
		serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane_1.setViewportView(serverTXT);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Servidor retornou");
		scrollPane_1.setColumnHeaderView(lblNewLabel_1_1_1);
		
		JLabel lblNewLabel_1_3 = new JLabel("Atualizando Usuario:");
		lblNewLabel_1_3.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel_1_3.setBounds(24, 63, 155, 13);
		contentPane.add(lblNewLabel_1_3);
		
		JTextPane usuarioTXT = new JTextPane();
		usuarioTXT.setText("escolha um usuario ");
		usuarioTXT.setToolTipText("escolha um usuario");
		usuarioTXT.setEditable(false);
		usuarioTXT.setBounds(189, 57, 203, 19);
		contentPane.add(usuarioTXT);
		
		JButton alterarBtn = new JButton("alterar");
		alterarBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				armazenaUsername =JOptionPane.showInputDialog(null,"Digite o username que deseja atualizar", "Digite",JOptionPane.INFORMATION_MESSAGE);
				usuarioTXT.setText(armazenaUsername);
			}
		});
		alterarBtn.setBounds(199, 86, 192, 19);
		contentPane.add(alterarBtn);
	}
}
