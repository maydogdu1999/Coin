import java.net.*;
import java.util.*;

import java.io.*;

public class Driver {

    public static void main(String[] args) {

        int hostPort = 0;
        String ip = "";
        int neighborPort = 0;


        //some code to make usage a little prettier when debugging 
        if (args.length > 3) {
            System.out.println("Usage: Driver [host port] [neighbor ip] [neighbor port");
            System.exit(1);
        } 

        try {

            hostPort = Integer.parseInt(args[0]);

            if (args.length != 1){
                ip = args[1];
                neighborPort = Integer.parseInt(args[2]);
            }
            
        }
        catch (Exception e) {

            System.out.println("Usage: Driver [port]");
            System.exit(1);

        }

        //new node object
        Node node1 = new Node();
        try {
            node1.setHostIp(InetAddress.getLocalHost().toString().split("/")[1]);
        }
        catch (IOException e) {
            System.out.println(e);
        }
        //start the server on the given port
        if (!ip.equals("--")) {
            System.out.println("not 1st node");
            node1.connectToPeer(ip, neighborPort);
        }

        node1.startServer(hostPort);
    }
}
