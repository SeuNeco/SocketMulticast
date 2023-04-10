import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Client {

    private static final String TOPICO_ESPORTES = "Esportes";
    private static final String TOPICO_ENTRETENIMENTO = "Entretenimento";
    private static final int PORTA_ESPORTES = 6060;
    private static final int PORTA_ENTRETENIMENTO = 8080;
    private static final String EXIT = "SAIR";

    private static final Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        String nome = JOptionPane.showInputDialog("Qual é o seu nome?");
        System.out.println("Escolha um topico de interesse:");
        System.out.println("1 - Esportes");
        System.out.println("2 - Entretenimento");

        String topico = "";
        int porta;
        while (true) {
            int escolha = teclado.nextInt();
            teclado.nextLine();
            switch (escolha) {
                case 1:
                    topico = TOPICO_ESPORTES;
                    porta = PORTA_ESPORTES;
                    break;
                case 2:
                    topico = TOPICO_ENTRETENIMENTO;
                    porta = PORTA_ENTRETENIMENTO;
                    break;
                default:
                    System.out.println("Opção inválida. Digite novamente.");
                    continue;
            }
            break;
        }

        Thread server = new Thread(new Client().new RunServer(nome, porta));
        Thread client = new Thread(new Client().new RunClient(nome, porta));

        server.start();
        client.start();
    }

    private class RunServer implements Runnable {
        String nome;
        int porta;

        public RunServer(String nome, int porta) {
            this.nome  = nome;
            this.porta = porta;
        }

        public void run() {
            String mensagem = " ";
            byte[] envio = new byte[1024];

            try (MulticastSocket socket = new MulticastSocket(porta)) {
                InetAddress grupo = InetAddress.getByName("230.0.0.0");

                while (!mensagem.equals(EXIT)) {
                    mensagem = JOptionPane.showInputDialog(null, nome);
                    if(!mensagem.equals(EXIT)){
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
                        LocalDateTime data = LocalDateTime.now();
                        mensagem = "[" + dtf.format(data) + "]" + nome + ": " + mensagem;
                        envio = mensagem.getBytes();
                        DatagramPacket pacote = new DatagramPacket(envio, envio.length, grupo, porta);
                        socket.send(pacote);
                    }
                }
                mensagem = nome + " saiu do grupo";
                envio = mensagem.getBytes();
                DatagramPacket pacote = new DatagramPacket(envio, envio.length, grupo, porta);
                socket.send(pacote);
                socket.close();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class RunClient implements Runnable {
        String nome;
        int porta;

        public RunClient(String nome, int porta) {
            this.nome = nome;
            this.porta = porta;
        }

        public void run() {
            String msg = "";
            try (MulticastSocket socket = new MulticastSocket(porta)) {
                InetAddress ia = InetAddress.getByName("230.0.0.0");
                InetSocketAddress grupo = new InetSocketAddress(ia, porta);
                NetworkInterface ni = NetworkInterface.getByInetAddress(ia);

                socket.joinGroup(grupo, ni);

                while (!msg.equals(EXIT)) {
                    byte[] buffer = new byte[1024];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    msg = new String(packet.getData());
                    System.out.println(msg);
                }
                socket.leaveGroup(grupo, ni);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
