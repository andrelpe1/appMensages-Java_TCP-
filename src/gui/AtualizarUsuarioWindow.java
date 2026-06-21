package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
    private JTextArea serverTXT;
   	private JTextArea clienteTXT;
   	private JTextField tokenTXT;
   	private static ConexaoCliente conexao;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AtualizarUsuarioWindow frame = new AtualizarUsuarioWindow(opcoesWindow,conexao,token);
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

	public AtualizarUsuarioWindow(OpcoesWindow opcoesWindow,ConexaoCliente conexao,String token) {
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
		 msg.op = "atualizarUsuario";
		 msg.nome = nomeTXT.getText();
		 msg.senha = senhaTXT.getText();
		 msg.token = tokenTXT.getText();
		
		try {
			 String json = conexao.mapper.writeValueAsString(msg);
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
		setBounds(100, 100, 484, 524);
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
				enviarParaServidor();
					
			}
		});
		btnAtualizar.setBounds(167, 159, 127, 29);
		contentPane.add(btnAtualizar);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(63, 205, 342, 91);
		contentPane.add(scrollPane);
		
		 clienteTXT = new JTextArea();
		clienteTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane.setViewportView(clienteTXT);
		
		JLabel lblNewLabel_1_2 = new JLabel("Cliente enviou:");
		scrollPane.setColumnHeaderView(lblNewLabel_1_2);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(63, 306, 344, 93);
		contentPane.add(scrollPane_1);
		
		 serverTXT = new JTextArea();
		serverTXT.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scrollPane_1.setViewportView(serverTXT);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Servidor retornou");
		scrollPane_1.setColumnHeaderView(lblNewLabel_1_1_1);
		
		JLabel lblNewLabel_1_3 = new JLabel("Token");
		lblNewLabel_1_3.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel_1_3.setBounds(63, 49, 45, 13);
		contentPane.add(lblNewLabel_1_3);
		
		tokenTXT = new JTextField();
		tokenTXT.setToolTipText("");
		tokenTXT.setColumns(10);
		tokenTXT.setBounds(129, 49, 287, 19);
		contentPane.add(tokenTXT);
	}
}
