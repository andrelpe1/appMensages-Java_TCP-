package gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;
import entities.Usuario;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

public class ConsultarUsuariosAdminWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static  OpcoesWindow opcoesWindow;
	private JTable tbUsuarios;
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
					ConsultarUsuariosAdminWindow frame = new ConsultarUsuariosAdminWindow(opcoesWindow,ClientSocket,in,out,token);
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
	
	

	public ConsultarUsuariosAdminWindow(OpcoesWindow opcoesWindow,Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		
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
		 msg.op = "consultarUsuariosAdmin";
		 msg.token_admin = token;
		 String json;
		try {
			json = mapper.writeValueAsString(msg);
			out.println(json);
			 System.out.println("ENVIADO: "+json);
			 //clienteTXT.setText(json);
		} catch (JsonProcessingException e) {
			JOptionPane.showMessageDialog(null,"Erro ao criar JSON", e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String respostaJson = in.readLine();
			   System.out.println("RECEBIDO: "+respostaJson);
			  // serverTXT.setText(respostaJson);
			Mensagem resposta = mapper.readValue(respostaJson, Mensagem.class);
			PopularTabela(resposta);
			if("200".equals(resposta.resposta)) {
				JOptionPane.showMessageDialog(null,"Lista encontrada!",String.valueOf(resposta.resposta) ,JOptionPane.INFORMATION_MESSAGE);
				return true;
			}else {
				JOptionPane.showMessageDialog(null,resposta.mensagem,String.valueOf(resposta.resposta) ,JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(null, "Conexão com servidor perdida!", "Erro", JOptionPane.ERROR_MESSAGE);
		    
		}
	
		return false;
	}
	
	
	
	private void PopularTabela(Mensagem lista) {

			try {
				DefaultTableModel modelo = (DefaultTableModel) tbUsuarios.getModel();
				modelo.fireTableDataChanged();
				modelo.setRowCount(0);
		
				List<Usuario> listaUsuarios = lista.lista_usuarios;
		
				for (Usuario usuario : listaUsuarios) {
		
					modelo.addRow(new Object[] { 
						usuario.getUsuario(), 
						usuario.getNome(), 
					});
				}
			
			} catch (Exception e) {

				JOptionPane.showMessageDialog(null, "Erro ao Listar usuarios: \n"+e.getMessage(), "Erro Listar Usuarios", JOptionPane.ERROR_MESSAGE);
			}
			
	}

	/**
	 * Create the frame.
	 */
	public void iniciarComponentes(Socket ClientSocket,DataInputStream in,PrintStream out,String token) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 581, 529);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Consultar Usuario ADMIN");
		lblNewLabel.setFont(new Font("MS Gothic", Font.BOLD, 18));
		lblNewLabel.setBounds(105, 10, 261, 46);
		contentPane.add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(29, 63, 528, 419);
		contentPane.add(scrollPane);
		
		tbUsuarios = new JTable();
		tbUsuarios.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Usuario", "Nome"
			}
		));
		scrollPane.setViewportView(tbUsuarios);
		enviarParaServidor( ClientSocket, in, out, token);
	}
}
