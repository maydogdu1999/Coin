import java.net.*;
import java.io.*;

/**
 * FrontEndServer starts a thread that handles the ruby connection, i.e., requests from the frontEnd server. 
 */
public class FrontEndServer extends Thread {

    private Node source = null;
    private ServerSocket serv = null; 
    
     //method that starts the node
     public void run() {

        System.out.println("Starting Front-End-Server on port 8101...");

        //try to start server on given port
        try
        {   
            serv = new ServerSocket(8101);
            while(true) {

                Socket socket = serv.accept();

                FrontEndConnection newFrontEnd = new FrontEndConnection();

                newFrontEnd.setSocket(socket);
                newFrontEnd.setSource(source);
                newFrontEnd.start();
            }
        }

        catch (IOException e) {

            System.out.println(e);
        }
    }

    
    public void setSource(Node source) {
        this.source = source;
    }
    
}
