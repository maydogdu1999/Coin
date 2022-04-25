import java.net.*;
import java.util.*;

import java.io.*;
import java.util.Scanner;

public class Driver {

    public static void main(String[] args) {

        int hostPort = 0;
        String ip = "";
        int neighborPort = 0;
        Scanner scanner = new Scanner(System.in);
        String inputLine = "";


        //some code to make usage a little prettier when debugging 
        if (args.length < 1) {
            System.out.println("Usage: Driver [host port]");
            System.exit(1);
        } 

        //new node object
        Node node1 = new Node();

        try {

            node1.setHostPort(Integer.parseInt(args[0]));
            
        }
        catch (Exception e) {

            System.out.println("Usage: Driver [host port]");
            System.exit(1);

        }

        try {
            node1.setHostIp(InetAddress.getLocalHost().toString().split("/")[1]);
        }
        catch (IOException e) {
            System.out.println(e);
        }


        node1.start();

        while(true) {
            try {
                inputLine = scanner.nextLine();
                System.out.println("recevied command:" + inputLine);
                String[] inputParsed = inputLine.split("--");
                if (inputParsed[0].equals("joinNode")) {
                    if (node1.getConnections().size() > node1.getMaxNeighbors() ) {
                        System.out.println("Can't join: Max number of neighbors reached");
                    }
                    ip = inputParsed[1];
                    neighborPort = Integer.parseInt(inputParsed[2]);
                    node1.joinNode(ip, neighborPort);
                    node1.populateNeighbors(ip, neighborPort, 0);
                }
            }
            catch (Exception e) {
                System.out.println(e);

            }
            
        }    

    }
}
