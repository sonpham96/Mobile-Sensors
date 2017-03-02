package Testing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Created by sonpham on 2017/03/01.
 */
public class AMQP {
    public static void main(String[] args) throws Exception {
        String uri = System.getenv("CLOUDAMQP_URL");
        if (uri == null) uri = "amqp://cgpwrecl:HahPfY4iosAO946_prpD0SxJ4ao4fe7O@white-mynah-bird.rmq.cloudamqp.com/cgpwrecl";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);

        //Recommended settings
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(30000);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String queue = "hello";     //queue name
        boolean durable = false;    //durable - RabbitMQ will never lose the queue if a crash occurs
        boolean exclusive = false;  //exclusive - if queue only will be used by one connection
        boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

        channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
        String message = "Hello CloudAMQP!";

        String exchangeName = "";
        String routingKey = "hello";
        channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue, true, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
        }
    }
}
