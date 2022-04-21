import java.net.*;
import java.util.*;
import java.io.*;


public class Node {



    ArrayList<String> neighbors; //an array of Strings containing the IP addresses of the Node's neighbors.

    //connection socket
    private ServerSocket serv = null; 

    //method that starts server
    public void startServer(int port) {

        Socket socket = null;

        //try to start server on given port
        try
        {
            serv = new ServerSocket(port);

            //while less than 10 neighbors, accept new connections 
            while (neighbors.size() <= 10) {

                socket = serv.accept();

                Connection conn = new Connection(socket);

                conn.start();

            }
    
        }

        catch (IOException e) {

            System.out.println(e);

        }

    }

    public void connectToPeer(String ip, int port) {

        Socket socket = null;

        //try to connect to given IP on given port, catch exception
        try 
        {
            socket = new Socket(ip, port);

            Connection conn = new Connection(socket);

            conn.start();
        }

        catch (IOException e) {

            System.out.println(e);

        }


    }






}