import java.net.*;
import java.util.*;
import java.io.*;


public class Node {

    //private ArrayList<String> neighbors = new ArrayList<String>(); //an array of Strings containing the IP addresses of the Node's neighbors.
    private HashMap<Connection, HashMap<String, String>> connections = new HashMap<Connection, HashMap<String, String>>();
    //a hashmap of the format {conn1: {"IP": someIp, "connType": "Client/Server"}, conn2...etc}

    //connection socket
    private ServerSocket serv = null; 

    public Node () {}

    //method that starts server
    public void startServer(int port) {

        Socket socket = null;

        //try to start server on given port
        try
        {
            serv = new ServerSocket(port);

            //while less than 10 neighbors, accept new connections 
            while (connections.size() <= 10) {

                socket = serv.accept();

                Connection conn = new Connection(socket);

                conn.start();
            
                //put the new client connection into connections
                String ipNeighbor = getIpFromSocket(socket);
                HashMap<String, String> infoConn = new HashMap<String, String>();
                infoConn.put("IP", ipNeighbor);
                infoConn.put("connType", "Client");
                connections.put(conn, infoConn);
                System.out.println("connected to:" + ipNeighbor);

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

            //put the new server connection into connections
            HashMap<String, String> infoConn = new HashMap<String, String>();
            infoConn.put("IP", ip);
            infoConn.put("connType", "Server");
            connections.put(conn, infoConn);
            System.out.println("connected to:" + ip);


        }

        catch (IOException e) {

            System.out.println(e);

        }


    }
    //given a socket, return the other sides ip address as string
    public String getIpFromSocket(Socket socketName) {
        InetSocketAddress sockaddr = (InetSocketAddress)socketName.getRemoteSocketAddress();
        InetAddress inaddr = sockaddr.getAddress();
        String ipString = inaddr.toString();
        return ipString;
    }


}