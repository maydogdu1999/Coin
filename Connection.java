import java.net.*;
import java.io.*;

public class Connection extends Thread{

    private DataInputStream input = null;

    private DataOutputStream output = null;

    private Socket sock = null;

    public Connection(Socket s) {

        sock = s;

    }
    
    public void run(){

        try {

        input = new DataInputStream(sock.getInputStream());

        output = new DataOutputStream(sock.getOutputStream());

        }

        catch (IOException e) {

            System.out.println(e);

        }

        System.out.println("Started Connection!");



    }


}
