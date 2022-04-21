import java.net.*;
import java.io.*;


public class Node {



    String[] neighbors; //an array of Strings containing the IP addresses of the Node's neighbors.

    //connection socket
    private ServerSocket sock = null; 

    //constructor that starts the node
    public void startServer(int port) {

        //try to start server on given port
        try
        {
            sock = new ServerSocket(port);



        }


    }




//try to connect, establish input/output streams
try {

    sock = new Socket(ip, port);

    input = new DataInputStream(sock.getInputStream());

    output = new DataOutputStream(sock.getOutputStream());

}

//catch IO error
catch (IOException e) {

    System.out.println(e);

}








}