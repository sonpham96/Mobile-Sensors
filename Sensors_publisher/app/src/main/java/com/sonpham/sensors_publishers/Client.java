package com.sonpham.sensors_publishers;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private static final String TAG = Client.class.getSimpleName().toUpperCase();

    private String host;
    private int port;
    private DataOutputStream out;
    private boolean online;
    private Socket socket;
    private static final int TIME_OUT = 10000; // 10s
    private Thread connection;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        online = false;
        socket = new Socket();

        establishConnection();
    }

    private void establishConnection() {
        connection = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isConnected() && !Thread.currentThread().isInterrupted()) {
                    try {
                        Log.d(TAG, "Connecting to /" + host + ":" + port + "...");
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(host, port), TIME_OUT);
                        online = true;
                        Log.d(TAG, "Connected to " + socket.getRemoteSocketAddress());
                        out = new DataOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        Log.d(TAG, "ERROR: CONNECTION FAILED");
                        online = false;
                    }
                }
            }
        });
        connection.start();
    }

    public void sendMessage(String msg) {
        Log.d(TAG, "Your message: " + msg);

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            if (online) {
                online = false;
                establishConnection();
            }
        }
    }

    public boolean isConnected() {
        return socket.isConnected() && online;
    }

    public void interruptConnection() {
        connection.interrupt();
    }
}