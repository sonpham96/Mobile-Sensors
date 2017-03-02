import Socket.*;
import java.io.*;

/**
 * Created by sonpham on 2017/03/02.
 */
public class Main {
    public static void main(String[] args) {
        int port = 1996;
        try {
            new Server(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
