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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
					CadastrarusuarioWindow frame = new CadastrarusuarioWindow(opcoesWindow,conexao);
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

	public CadastrarusuarioWindow(OpcoesWindow opcoesWindow, ConexaoCliente conexao) {
		this.conexao = conexao;
		this.opcoesWindow = opcoesWindow;
		getContentPane().setLayout(null);
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
		 msg.op = "cadastrarUsuario";
		 msg.usuario = usuarioTXT.getText();
		 msg.nome = nomeTXT.getText();
		 msg.senha = senhaTXT.getText();
		 
		try {
			 String json= conexao.mapper.writeValueAsString(msg);
			 clienteTXT.setText(json);
			 conexao.enviar(msg);
			 Mensagem resp = conexao.aguardarResposta();
			 
			 String jsonRecebido = conexao.mapper.writeValueAsString(resp);
			 serverTXT.setText(jsonRecebido);
			 if ("200".equals(resp.resposta)) {
	              JOptionPane.showMessageDialog(this,resp.mensagem, resp.resposta, JOptionPane.INFORMATION_MESSAGE);
	               fecharJanela();
			  }else {
	                JOptionPane.showMessageDialog(this,resp.mensagem, resp.resposta, JOptionPane.ERROR_MESSAGE);
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
	

	
	/**
	 * Create the frame.
	 */
	public void iniciarComponentes() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 422, 509);
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
				enviarParaServidor();		
			}
		});
		btnNewButton.setBounds(143, 205, 126, 35);
		contentPane.add(btnNewButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 259, 346, 72);
		contentPane.add(scrollPane);
		
		JLabel lblNewLabel_1_3 = new JLabel("Cliente enviou:");
		scrollPane.setColumnHeaderView(lblNewLabel_1_3);
		
		clienteTXT = new JTextArea();
		clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane.setViewportView(clienteTXT);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(20, 360, 344, 80);
		contentPane.add(scrollPane_1);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Servidor retornou");
		scrollPane_1.setColumnHeaderView(lblNewLabel_1_1_1);
		
		serverTXT = new JTextArea();
		serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane_1.setViewportView(serverTXT);
	}

}
