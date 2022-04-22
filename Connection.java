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

            input = new DataInputStream(new BufferedInputStream(sock.getInputStream()));

            output = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));

        }

        catch (IOException e) {

            System.out.println(e);

        }

        System.out.println("Started Connection!");

        
        String line = "";
        while (!line.equals("Over")) {
            try {
                line = input.readUTF();
                System.out.println(line);
            }
            catch(IOException i) {
                System.out.println(i);
            }
        }
        System.out.println("Closing connection");
 
        // close connection
        try {
            sock.close();
            input.close();
            output.close();
        }

        catch (IOException e) {

            System.out.println(e);

        }



    }


}
