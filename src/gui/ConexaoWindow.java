package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;

public class ConexaoWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField IPtxtField;
	private JTextField portaTxtField;
	private String serverIP;
	private int serverPorta;
	
	//Rotina para entrada de dados via teclado
    DataInputStream teclado = new DataInputStream(System.in);
    ObjectMapper mapper = new ObjectMapper();
    //Geração do socket
    Socket ClientSocket = null;
    String token = null;
    static DataInputStream in;                  // cria um duto de entrada
    static PrintStream out; 
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConexaoWindow frame = new ConexaoWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void definirIpePorta() {
		 try {
			 serverIP = IPtxtField.getText();
			 serverPorta = Integer.parseInt(portaTxtField.getText());
			ClientSocket = new Socket(serverIP, serverPorta);
			 in = new DataInputStream(ClientSocket.getInputStream());    // aponta o duto de entrada para o socket do cliente
             out = new PrintStream(ClientSocket.getOutputStream()); 
             JOptionPane.showMessageDialog(null,in.readLine(), "Servidor Retornou:", JOptionPane.INFORMATION_MESSAGE);
             abrirOpcoes(ClientSocket,in,out);
             
             
             
             //ClientSocket.close();
		} catch (UnknownHostException e) {
			 JOptionPane.showMessageDialog(null,"Host Desconhecido", "Erro ao conectar ao server", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			 JOptionPane.showMessageDialog(null,"IP ou Porta não Existe", "Erro ao conectar ao server", JOptionPane.ERROR_MESSAGE);
		}catch(Exception e) {
			JOptionPane.showMessageDialog(null,"Falha na conexão do servidor", "Erro ao conectar ao server", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void abrirOpcoes( Socket ClientSocket,DataInputStream in,PrintStream out){
		OpcoesWindow janelaOpcoes = new OpcoesWindow(this, ClientSocket,in,out);
		janelaOpcoes.setVisible(true);
		this.setVisible(false);
	}
	
	/**
	 * Create the frame.
	 */
	public ConexaoWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 372, 348);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("IP DO SERVIDOR");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel.setBounds(104, 10, 128, 44);
		contentPane.add(lblNewLabel);
		
		JLabel lblPortaDoServidor = new JLabel("PORTA DO SERVIDOR");
		lblPortaDoServidor.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPortaDoServidor.setBounds(104, 98, 172, 44);
		contentPane.add(lblPortaDoServidor);
		
		IPtxtField = new JTextField();
		IPtxtField.setBounds(114, 56, 128, 27);
		contentPane.add(IPtxtField);
		IPtxtField.setColumns(10);
		
		portaTxtField = new JTextField();
		portaTxtField.setColumns(10);
		portaTxtField.setBounds(114, 138, 128, 27);
		contentPane.add(portaTxtField);
		
		JButton btnEnviar = new JButton("ENVIAR");
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				definirIpePorta();
				
			}
		});
		btnEnviar.setBounds(114, 214, 116, 21);
		contentPane.add(btnEnviar);
	}
}
