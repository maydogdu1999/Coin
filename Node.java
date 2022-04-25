import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;


public class Node {
    static final int MAX_NEIGHBORS = 3;

    //private ArrayList<String> neighbors = new ArrayList<String>(); //an array of Strings containing the IP addresses of the Node's neighbors.
    ConcurrentHashMap<Connection, String> connections = new ConcurrentHashMap<Connection,String>();

    //connection socket
    private ServerSocket serv = null; 

    public Node () {}

    //method that starts server
    public void startServer(int hostPort, String neighborIp, int neighborPort) {

        System.out.println("Starting startServer...");

        Socket socket = null;

        //try to start server on given port
        try
        {
            //start on given port
            serv = new ServerSocket(hostPort);

            String ownIp = InetAddress.getLocalHost().toString().split("/")[1];

            if (!neighborIp.equals("--") && !(neighborPort == 0)) {

                connectToPeer(neighborIp, neighborPort);
                populateNeighbors(ownIp, hostPort, 0);

            }

            
            //System.out.println("populating neighbiors for own ip: " + ownIp);
            //populateNeighbors(ownIp, port, 1);

            //while less than 10 neighbors, accept new connections 
            while (true) {

                //if fewer than max_neighbors, accept, start connection thread, hand off port, then add to list of connections
                if (connections.size() < MAX_NEIGHBORS - 1) {

                    socket = serv.accept();

                    Connection conn = new Connection(socket, this);

                    conn.start();
            
                    //put the new client connection into connections
                    String ipNeighbor = getIpFromSocket(socket);
                    addConnection(conn, ipNeighbor);

                    System.out.println(connections.values());


                    System.out.println("connected to:" + ipNeighbor);
                }
                
            }
    
        }

        catch (IOException e) {

            System.out.println(e);

        }

    }

    //method for a node to connect to a peer, given ip and port
    public void connectToPeer(String ip, int port) {

        System.out.println("Starting connectToPeer...");

        Socket socket = null;

        //try to connect to given IP on given port, catch exception
        try 
        {
            //start connection, then thread
            socket = new Socket(ip, port);

            Connection conn = new Connection(socket, this);

            conn.start();

            //put the new server connection into connections
            addConnection(conn, ip);

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

    public synchronized ConcurrentHashMap<Connection, String> getConnections() {
        return connections;
    }

    public synchronized Boolean addConnection(Connection connection, String ip) {

        if (connections.put(connection, ip) != null) {

            System.out.print("Added a connection: ");
            System.out.println(connections.values());
            return true;

        } else {
            return false;
        }

        


    }

    public synchronized Boolean removeConnection(Connection connection) {
        if (connections.remove(connection) != null) {

            System.out.print("removed a connection: ");
            System.out.println(connections.values());
            return true;

        } else {
            return false;
        }

    }

    public void populateNeighbors(String ip, int port, int counter) {

        System.out.println("Starting populate Neighbors...");

        System.out.print("connections while in populate neighbors: ");
        System.out.println(connections.values());

        //for each neighbor
        for (Connection connection: connections.keySet()) {

            //create properly formatted message
            String message = "populateNeighbors--" + ip + "--" + String.valueOf(port) + "--" + String.valueOf(counter);

            connection.sendMessage(message);

        }
    }

    public int getMaxNeighbors() {
        return MAX_NEIGHBORS;
    }


}