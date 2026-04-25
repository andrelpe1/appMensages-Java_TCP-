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

public class AtualizarUsuarioWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField nomeTXT;
	private JTextField senhaTXT;
	private static  OpcoesWindow opcoesWindow;

	private static Socket ClientSocket = null;
	static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
    private static String token = null;
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

	public AtualizarUsuarioWindow(OpcoesWindow opcoesWindow,Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		
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
	
	private void enviarParaServidor(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		ObjectMapper mapper = new ObjectMapper();
		 Mensagem msg = new Mensagem();
		 msg.op = "atualizarUsuario";
		 msg.nome = nomeTXT.getText();
		 msg.senha = senhaTXT.getText();
		 msg.token = token;
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();;
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			if(resposta.status == 200) {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.status) ,JOptionPane.INFORMATION_MESSAGE);
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.status) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Create the frame.
	 */
	public void iniciarComponentes(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 505, 266);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Atualizar Usuario");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblNewLabel.setBounds(10, 10, 149, 29);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Nome");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel_1.setBounds(63, 76, 45, 13);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_1 = new JLabel("Senha");
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel_1_1.setBounds(63, 113, 45, 13);
		contentPane.add(lblNewLabel_1_1);
		
		nomeTXT = new JTextField();
		nomeTXT.setToolTipText("");
		nomeTXT.setBounds(129, 75, 287, 19);
		contentPane.add(nomeTXT);
		nomeTXT.setColumns(10);
		
		senhaTXT = new JTextField();
		senhaTXT.setToolTipText("");
		senhaTXT.setColumns(10);
		senhaTXT.setBounds(129, 112, 287, 19);
		contentPane.add(senhaTXT);
		
		JButton btnAtualizar = new JButton("ATUALIZAR");
		btnAtualizar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarParaServidor(ClientSocket,in,out,token);
			}
		});
		btnAtualizar.setBounds(167, 159, 127, 29);
		contentPane.add(btnAtualizar);
	}

}
