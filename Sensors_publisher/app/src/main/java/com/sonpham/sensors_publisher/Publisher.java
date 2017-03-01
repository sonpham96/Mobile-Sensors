package com.sonpham.sensors_publisher;

import android.util.Log;


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

                while (true) {
                    String message = queue.takeFirst();
                    try{
                        ch.basicPublish("amq.fanout", "chat", null, message.getBytes());
                        Log.d("", "[s] " + message);
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e){
                        Log.d("","[f] " + message);
                        queue.putFirst(message);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                Log.d("", "Connection broken: " + e.getClass().getName());
                try {
                    Thread.sleep(5000); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }
}
