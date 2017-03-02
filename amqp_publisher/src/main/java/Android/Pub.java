package Android;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by sonpham on 2017/03/02.
 */
public class Pub {

    public static void main(String[] args) {
        final BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
        ConnectionFactory factory = new ConnectionFactory();
        final Thread publishThread;

        setupConnectionFactory(factory);
        publishThread = new Publisher(queue, factory);
        publishThread.start();
        new Thread(new Runnable() {
            public void run() {
                int i = 0;
                while (true) {
                    try {
//                        String msg = "sv1mk_";
//                        String msg = "sv2lion_";
//                        String msg = "sv3mk_";
                        publishMessage("sv2_" + Integer.toString(++i), queue);
//                        publishMessage("server1.dog." + Integer.toString(++i), queue);
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
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

    private static void publishMessage(String message, BlockingDeque<String> queue) {
        //Adds a message to internal blocking queue
        try {
            // System.out.println("[q] " + message);
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
