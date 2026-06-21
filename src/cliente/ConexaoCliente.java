package cliente;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Mensagem;

public class ConexaoCliente {

    public final ObjectMapper mapper = new ObjectMapper();

    private final Socket socket;
    private final PrintStream out;
    private final BlockingQueue<String> filaRespostas = new LinkedBlockingQueue<>();

    private BiConsumer<String, String> onMensagemRecebida; 

    private volatile boolean conectado = true;

    public ConexaoCliente(Socket socket) throws IOException {
        this.socket = socket;
        this.out    = new PrintStream(socket.getOutputStream(), true);

        DataInputStream dataIn = new DataInputStream(socket.getInputStream());

        Thread leitor = new Thread(() -> {
            try {
                String linha;
                while ((linha = dataIn.readLine()) != null) {
                    Mensagem recebida;
                    try {
                        recebida = mapper.readValue(linha, Mensagem.class);
                    } catch (Exception parseEx) {
                        continue;
                    }

                    if ("receberMensagem".equalsIgnoreCase(recebida.op)) {
                        if (recebida.remetente != null && recebida.mensagem != null
                                && onMensagemRecebida != null) {
                            onMensagemRecebida.accept(recebida.remetente, recebida.mensagem);
                        }
                    } else {
                        filaRespostas.put(linha);
                    }
                }
            } catch (IOException e) {
          
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                conectado = false;
            }
        }, "leitor-servidor");
        leitor.setDaemon(true);
        leitor.start();
    }

    public void enviar(Mensagem msg) throws IOException {
        String json = mapper.writeValueAsString(msg);
        out.println(json);
        System.out.println("ENVIADO: " + json);
    }

    public Mensagem aguardarResposta() throws InterruptedException, IOException {
        String json = filaRespostas.take();
        System.out.println("RECEBIDO: " + json);
        return mapper.readValue(json, Mensagem.class);
    }

    public void setOnMensagemRecebida(BiConsumer<String, String> callback) {
        this.onMensagemRecebida = callback;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void fechar() {
        try { socket.close(); } catch (IOException ignored) {}
        conectado = false;
    }
}