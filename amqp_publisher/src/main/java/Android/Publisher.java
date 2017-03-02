package Android;
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

    public Publisher(BlockingDeque<String> queue, ConnectionFactory factory) {
        this.queue = queue;
        this.factory = factory;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Connection connection = factory.newConnection();
                Channel ch = connection.createChannel();
                ch.confirmSelect();

                String routingKey = "server.2";
                ch.queueDeclare(routingKey, false, false, false, null);
                while (true) {
                    String message = queue.takeFirst();
                    try {
//                        String exchangeName = "amq.fanout";
                        String exchangeName = "";
//                        String routingKey = "chat";

                        ch.basicPublish(exchangeName, routingKey, null, message.getBytes());
                        System.out.println("[s] " + message);
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e){
                        System.out.println("[f] " + message);
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
