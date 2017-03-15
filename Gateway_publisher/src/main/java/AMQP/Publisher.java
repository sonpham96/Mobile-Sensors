package AMQP;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.BlockingDeque;

/**
 * Created by sonpham on 2017/03/01.
 */

public class Publisher extends Thread {
    private BlockingDeque<String> queue;
    private ConnectionFactory factory;
    private static final String EXCHANGE_NAME =  "amq.topic";
    public String publisherName = "GATEWAY";

    public Publisher(BlockingDeque<String> queue, ConnectionFactory factory) {
        this.queue = queue;
        this.factory = factory;
    }

    public Publisher(BlockingDeque<String> queue, ConnectionFactory factory, String publisherName) {
        this.queue = queue;
        this.factory = factory;
        this.publisherName = publisherName;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Connection connection = factory.newConnection();
                Channel ch = connection.createChannel();
                ch.confirmSelect();

                while (true) {
                    String message = queue.takeFirst();
                    String sensorType = message.substring(0, message.indexOf('.'));
                    String value = message.substring(message.indexOf('.') + 1, message.length());
                    try {
                        ch.basicPublish(EXCHANGE_NAME, publisherName + '.' + sensorType, null, value.getBytes());
                        System.out.println("[s] " + value);
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e){
                        System.out.println("[f] " + value);
                        queue.putFirst(message);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.out.println("Connection broken: " + e.getClass().getName());
                try {
                    Thread.sleep(5000); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }
}
