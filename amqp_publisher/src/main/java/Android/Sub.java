package Android;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sonpham on 2017/03/02.
 */
public class Sub {
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        Thread subscribeThread;

        setupConnectionFactory(factory);

        subscribeThread = new Subscriber(factory);
        subscribeThread.start();
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
}
