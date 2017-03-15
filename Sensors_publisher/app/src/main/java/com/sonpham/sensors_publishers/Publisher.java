package com.sonpham.sensors_publishers;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.BlockingDeque;

/**
 * Created by sonpham on 2017/03/01.
 */

public class Publisher extends Thread {
    private static final String TAG = Publisher.class.getSimpleName().toUpperCase();

    private BlockingDeque<Message> queue;
    private ConnectionFactory factory;
    private static final String EXCHANGE_NAME = "amq.topic";
    public String publisherName;

    public Publisher(BlockingDeque<Message> queue, ConnectionFactory factory) {
        this.queue = queue;
        this.factory = factory;
        this.publisherName = "Android";
    }

    public Publisher(BlockingDeque<Message> queue, ConnectionFactory factory, String publisherName) {
        this.queue = queue;
        this.factory = factory;
        this.publisherName = publisherName;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Connection connection = factory.newConnection();
                Channel ch = connection.createChannel();
                ch.confirmSelect();

                while (true) {
                    Message message = queue.takeFirst();
                    try {
                        ch.basicPublish(EXCHANGE_NAME, publisherName + '.' + message.sensorType, null, message.value.getBytes());
                        Log.d(TAG, "[s] " + message.value);
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e) {
                        Log.d(TAG, "[f] " + message.value);
                        queue.putFirst(message);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                Log.d(TAG, "Connection broken: " + e.getClass().getName());
                try {
                    Thread.sleep(5000); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }
}
