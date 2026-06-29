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

import cliente.ConexaoCliente;
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
    
	private static ConexaoCliente conexao;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConsultarUsuariosAdminWindow frame = new ConsultarUsuariosAdminWindow(opcoesWindow,conexao,token);
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
	
	

	public ConsultarUsuariosAdminWindow(OpcoesWindow opcoesWindow,ConexaoCliente conexao,String token) {
		
		this.opcoesWindow = opcoesWindow;
		getContentPane().setLayout(null);
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		
		iniciarComponentes(token);
	}
	
	
	private void enviarParaServidor(String token) {
		 Mensagem msg = new Mensagem();
		 msg.op = "consultarUsuariosAdmin";
		 msg.token = token;

		try {
			 String json = conexao.mapper.writeValueAsString(msg);
			 //clienteTXT.setText(json);
			 Mensagem resp = conexao.aguardarResposta();
			 
			 String jsonRecebido = conexao.mapper.writeValueAsString(resp);
			 PopularTabela(resp);
			 if ("200".equals(resp.resposta)) {
	              JOptionPane.showMessageDialog(this,"Lista encontrada!", resp.resposta, JOptionPane.INFORMATION_MESSAGE);
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
	
	
	
	private void PopularTabela(Mensagem lista) {

			try {
				DefaultTableModel modelo = (DefaultTableModel) tbUsuarios.getModel();
				modelo.fireTableDataChanged();
				modelo.setRowCount(0);
		
				List<Usuario> listaUsuarios = lista.lista_usuarios1;
		
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
	public void iniciarComponentes(String token) {
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
		enviarParaServidor( token);
	}
}
