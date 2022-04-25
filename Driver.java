import java.net.*;
import java.util.*;

import java.io.*;

public class Driver {

    public static void main(String[] args) {

        int hostPort = 0;
        String neighborip = "";
        int neighborPort = 0;
        String hostIp = "";

        try {
            hostIp = InetAddress.getLocalHost().toString().split("/")[1];

        } catch (UnknownHostException e1) {

            e1.printStackTrace();

        }


        //some code to make usage a little prettier when debugging 
        if (args.length > 3) {
            System.out.println("Usage: Driver [host port] [neighbor ip] [neighbor port");
            System.exit(1);
        } 

        try {

            hostPort = Integer.parseInt(args[0]);

            if (args.length != 1){
                neighborip = args[1];
                neighborPort = Integer.parseInt(args[2]);
            }
            
        }
        catch (Exception e) {

            System.out.println("Usage: Driver [port]");
            System.exit(1);

        }

        //new node object
        Node node1 = new Node();

        if (args.length > 1) {

            node1.connectToPeer(neighborip, neighborPort);

        }

        node1.startServer(hostPort);

        node1.populateNeighbors(hostIp, hostPort, 0);

        

    }
}
