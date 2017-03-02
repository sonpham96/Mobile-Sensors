package com.sonpham.sensors_publisher;

import android.util.Log;
import android.widget.Toast;

import java.net.*;
import java.io.*;

public class Client {
    private final String host;
    private final int port;
    private DataOutputStream out;
    private boolean connected = true;
    private static final int TIME_OUT = 3000;

    public Client(final String host, final int port) {
        this.host = host;
        this.port = port;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("CLIENT", "Connecting to " + host + " on port " + port);
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), TIME_OUT);
                    connected = true;
                    Log.d("CLIENT", "Just connected to " + socket.getRemoteSocketAddress());
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    Log.d("CLIENT", "ERROR: CONNECTION FAILED");
                    connected = false;
                }
            }
        }).start();
    }

    public void sendMessage(String msg) {
        Log.d("CLIENT", "Your message: " + msg);

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}