import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Node extends Thread {
    static final int MAX_NEIGHBORS = 5;
    static final int IDEAL_NEIGHBORS = 3;


    //private ArrayList<String> neighbors = new ArrayList<String>(); //an array of Strings containing the IP addresses of the Node's neighbors.
    ConcurrentHashMap<Connection, String> connections = new ConcurrentHashMap<Connection,String>();

    //connection socket
    private ServerSocket serv = null; 

    private String hostIp; 

    private int hostPort;

    //method that starts server
    public void run() {

        System.out.println("Starting startServer...");

        Socket socket = null;

        this.hostPort = hostPort;

        //try to start server on given port
        try
        {
            //start on given port
            serv = new ServerSocket(hostPort);

            hostIp = InetAddress.getLocalHost().toString().split("/")[1];

            
            //System.out.println("populating neighbiors for own ip: " + ownIp);
            //populateNeighbors(ownIp, port, 1);

            //while less than 10 neighbors, accept new connections 
            while (true) {

                //if fewer than max_neighbors, accept, start connection thread, hand off port, then add to list of connections
                if (connections.size() < IDEAL_NEIGHBORS) {

                    socket = serv.accept();

                    Connection conn = new Connection(socket, this);

                    conn.start();
            
                    //put the new client connection into connections
                    String ipNeighbor = getIpFromSocket(socket);

                    addConnection(conn, ipNeighbor);
                    System.out.println("received a connection to:" + ipNeighbor);
                    System.out.println(connections.values());


                }
                
            }
    
        }

        catch (IOException e) {

            System.out.println(e);

        }

    }

    //method for a node to connect to a peer, given ip and port
    public synchronized void connectToPeer(String ip, int port) {

        System.out.println("Starting connectToPeer...");
        
       
        System.out.println("cur connections :" + connections.values());
        for (String connection: connections.values()) {
            //create properly formatted message
            if(connection.equals(ip)) {
                System.out.println("can't connect to the same ip twice");
                return;
            }
            
        }

        if(hostIp.equals(ip)) {
            System.out.println("can't connect to self");
            return;
        }

        System.out.println("is trying to connect to: " + ip);


        Socket socket = null;

        //try to connect to given IP on given port, catch exception
        try 
        {

            System.out.println("Starting socket...");
            //start connection, then thread
            socket = new Socket(ip, port);

            System.out.println(socket);

            System.out.println("Creating thread object...");
            Connection conn = new Connection(socket, this);

            System.out.println("Starting thread...");
            conn.start();

            //put the new server connection into connections
            //addConnection(conn, ip + "--" + port);
            addConnection(conn, ip);

            System.out.println("connected to:" + ip + " at port: " + port);

        }

        catch (Exception e) {

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

    public ConcurrentHashMap<Connection, String> getConnections() {
        return connections;
    }

    public Boolean addConnection(Connection connection, String ip) {

        if (connections.put(connection, ip) != null) {

            System.out.print("Added a connection: ");
            System.out.println(connections.values());
            System.out.flush();
            return true;

        } else {
            return false;
        }

        


    }

    public Boolean removeConnection(Connection connection) {
        if (connections.remove(connection) != null) {

            System.out.print("removed a connection: ");
            System.out.println(connections.values());
            System.out.flush();
            return true;

        } else {
            return false;
        }

    }

    public void populateNeighbors(String ip, int port, int counter) {
        
        try {
            TimeUnit.SECONDS.sleep(1);
        } 
        catch (Exception e) {
            System.out.print(e);
        }
        
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

    public String getHostIp() {

        return hostIp;

    }
    public void setHostIp(String hostIp) {

        this.hostIp = hostIp;

    }

    public int getHostPort() {

        return hostPort;

    }

    public void setHostPort(int hostPort) {

        this.hostPort = hostPort;

    }



    public synchronized void joinNode(String ip, int port) {

        System.out.println("Starting joinNode...");
        
       
        System.out.println("cur connections :" + connections.values());
        for (String connection: connections.values()) {
            //create properly formatted message
            if(connection.equals(ip)) {
                System.out.println("can't join an existing node");
                return;
            }
            
        }

        if(hostIp.equals(ip)) {
            System.out.println("can't connect to self");
            return;
        }

        System.out.println("is trying to connect to: " + ip);


        Socket socket = null;

        //try to connect to given IP on given port, catch exception
        try 
        {

            System.out.println("Starting socket...");
            //start connection, then thread
            socket = new Socket(ip, port);

            System.out.println(socket);

            System.out.println("Creating thread object...");
            Connection conn = new Connection(socket, this);

            System.out.println("Starting thread...");
            conn.start();

            //put the new server connection into connections
            //addConnection(conn, ip + "--" + port);
            addConnection(conn, ip);

            System.out.println("connected to:" + ip + " at port: " + port);

        }

        catch (Exception e) {

            System.out.println(e);

        }


    }

}