package Socket;

import AMQP.Publisher;
import com.rabbitmq.client.ConnectionFactory;

import java.io.*;
import java.net.*;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by sonpham on 2017/03/02.
 */
class ClientSocket extends Thread {
    private Socket socket;
    private static BlockingDeque<String> queue;
    private static ConnectionFactory factory;
    private static Publisher publishThread;

    public ClientSocket(Socket s) {
        socket = s;
        queue = new LinkedBlockingDeque<String>();
    }

    private static void setupConnectionFactory(ConnectionFactory factory) {
        String uri = "amqp://cgpwrecl:HahPfY4iosAO946_prpD0SxJ4ao4fe7O@white-mynah-bird.rmq.cloudamqp.com/cgpwrecl";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        } catch (URISyntaxException e3) {
            e3.printStackTrace();
        }
    }

    private static void publishMessage(String message) {
        //Adds a message to internal blocking queue
        try {
            System.out.println("[q] " + message);
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        factory = new ConnectionFactory();
        setupConnectionFactory(factory);
        publishThread = new Publisher(queue, factory, "GATEWAY");
        publishThread.start();

        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String clientMsg;
            while (true) {
                clientMsg = in.readUTF();
                publishMessage(clientMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
