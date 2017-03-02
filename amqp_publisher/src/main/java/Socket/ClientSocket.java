package Socket;

import java.io.*;
import java.net.*;

/**
 * Created by sonpham on 2017/03/02.
 */
class ClientSocket extends Thread {
    private Socket socket;

    public ClientSocket(Socket s){
        socket = s;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String clientMsg;
            while (true) {
                clientMsg = in.readUTF();
                System.out.println(clientMsg);
                out.writeUTF("SERVER: Message Received!\n MESSAGE: " + clientMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
