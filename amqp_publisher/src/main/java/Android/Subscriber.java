package Android;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Created by sonpham on 2017/03/01.
 */

public class Subscriber extends Thread {
    private ConnectionFactory factory;

    public Subscriber(ConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.basicQos(1);
                AMQP.Queue.DeclareOk q = channel.queueDeclare();

//                String exchangeName = "amq.fanout";
                String exchangeName = "amq.topic";
                String qname = q.getQueue();
//                        String routingKey = "chat";
//                String routingKey = "chat";
//                channel.queueBind(q.getQueue(), exchangeName, routingKey);
//                channel.queueBind(q.getQueue(), exchangeName, "*.monkey");
                channel.queueBind("server.1", exchangeName, "server.*");
                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume("server.1", true, consumer);
                channel.basicConsume("server.2", true, consumer);

                // Process deliveries
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    String message = new String(delivery.getBody());
                    System.out.println("[r] " + message);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e1) {
                System.out.println("Connection broken: " + e1.getClass().getName());
                try {
                    Thread.sleep(4000); //sleep and then try again
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
