package gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class ConsultarUsuarioAdminWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static  OpcoesWindow opcoesWindow;
	private static String nome;
	private static String usuario;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConsultarUsuarioAdminWindow frame = new ConsultarUsuarioAdminWindow(opcoesWindow,nome,usuario);
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
	
	

	public ConsultarUsuarioAdminWindow(OpcoesWindow opcoesWindow,String nome,String usuario) {
		
		this.opcoesWindow = opcoesWindow;
		getContentPane().setLayout(null);
		addWindowListener(new WindowAdapter() {	
			@Override
			public void windowClosed(WindowEvent e) {
				fecharJanela();
			}
		});
		iniciarComponentes(nome,usuario);
	}

	/**
	 * Create the frame.
	 */
	public void iniciarComponentes(String nome,String usuario) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 471, 366);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Consultar Usuario ADMIN");
		lblNewLabel.setFont(new Font("MS Gothic", Font.BOLD, 18));
		lblNewLabel.setBounds(105, 10, 261, 46);
		contentPane.add(lblNewLabel);
		
		JTextPane nomeTXT = new JTextPane();
		nomeTXT.setEditable(false);
		nomeTXT.setFont(new Font("Tahoma", Font.PLAIN, 18));
		nomeTXT.setBounds(92, 72, 268, 46);
		nomeTXT.setText(nome);
		contentPane.add(nomeTXT);
		
		JLabel lblNewLabel_1 = new JLabel("Nome");
		lblNewLabel_1.setBounds(37, 79, 45, 13);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_1 = new JLabel("Usuario");
		lblNewLabel_1_1.setBounds(37, 174, 45, 13);
		contentPane.add(lblNewLabel_1_1);
		
		JTextPane usuarioTXT = new JTextPane();
		usuarioTXT.setEditable(false);
		usuarioTXT.setFont(new Font("Tahoma", Font.PLAIN, 18));
		usuarioTXT.setBounds(92, 166, 268, 46);
		usuarioTXT.setText(usuario);
		contentPane.add(usuarioTXT);
	}
}
