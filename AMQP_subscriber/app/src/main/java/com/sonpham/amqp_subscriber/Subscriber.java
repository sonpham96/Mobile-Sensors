package com.sonpham.amqp_subscriber;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Created by sonpham on 2017/03/01.
 */

public class Subscriber extends Thread {
    private Handler handler;
    private ConnectionFactory factory;
    private static final String EXCHANGE_NAME = "amq.topic";
    public String publisherName = "AnhAnh";
    public String sensorType = "pressure";

    public Subscriber(Handler handler, ConnectionFactory factory) {
        this.handler = handler;
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
                channel.queueBind(q.getQueue(), EXCHANGE_NAME, publisherName + '.' + sensorType);
                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(q.getQueue(), true, consumer);

                // Process deliveries
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    String message = new String(delivery.getBody());
                    Log.d("", "[r] " + message);

                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();

                    bundle.putString("msg", message);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e1) {
                Log.d("", "Connection broken: " + e1.getClass().getName());
                try {
                    Thread.sleep(4000); //sleep and then try again
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
