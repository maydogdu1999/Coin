import java.net.*;
import java.util.*;
import java.io.*;


public class Node {
    static final int MAX_NEIGHBORS = 3;

    //private ArrayList<String> neighbors = new ArrayList<String>(); //an array of Strings containing the IP addresses of the Node's neighbors.
    HashMap<Connection, String> connections = new HashMap<Connection,String>();

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
            String ownIp = InetAddress.getLocalHost().toString().split("/")[1];
            System.out.println("populating neighbiors for own ip: " + ownIp);
            populateNeighbors(ownIp, port, 1);
            //while less than 10 neighbors, accept new connections 
            while (true) {

                if (connections.size() < MAX_NEIGHBORS) {
                    socket = serv.accept();

                    Connection conn = new Connection(socket, this);

                    conn.start();
            
                    //put the new client connection into connections
                    String ipNeighbor = getIpFromSocket(socket);
                    connections.put(conn, ipNeighbor);
                    System.out.println(connections.values());
                    System.out.println("connected to:" + ipNeighbor);
                }
                
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

            Connection conn = new Connection(socket, this);

            conn.start();

            //put the new server connection into connections
            connections.put(conn, ip);
            System.out.println(connections.values());

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
        String ipString = inaddr.toString().split("/")[1];
        return ipString;
    }

    public HashMap<Connection, String> getConnections() {
        return connections;
    }

    public Boolean removeConnection(Connection connection) {
        return (connections.remove(connection) != null);

    }

    public void populateNeighbors(String ip, int port, int counter) {
        for (Connection connection: connections.keySet()) {
            String message = "populateNeighbors--" + ip + "--" + String.valueOf(port) + "--" + String.valueOf(counter);
            connection.sendMessage(message);
            System.out.println("popu from node for conn " + connection.toString());
        }
    }

    public int getMaxNeighbors() {
        return MAX_NEIGHBORS;
    }


}