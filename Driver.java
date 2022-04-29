import java.net.*;
import java.util.*;

import java.io.*;
import java.util.Scanner;
import java.util.logging.Handler;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;



public class Driver {
    static Node node1 = null;

    public static void main(String[] args) {

        int hostPort = 0;
        
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
                handleCommandLine(inputLine);
                
            }
            catch (Exception e) {
                System.out.println(e);

            }
            
        }    

    }

    public static void handleCommandLine(String inputLine) {
        String[] inputParsed = inputLine.split("--");

        if (inputParsed[0].equals("joinNode")) {
            if (node1.getConnections().size() > node1.getMaxNeighbors() ) {
                System.out.println("Can't join: Max number of neighbors reached");
            }
            String ip = inputParsed[1];
            int neighborPort = Integer.parseInt(inputParsed[2]);
            node1.joinNode(ip, neighborPort);
            node1.populateNeighbors(ip, neighborPort, 0);
        }

        if (inputParsed[0].equals("makeTransaction")) {
            String senderPublicKey;
            String senderPrivateKey;
            String recipientPublicKey;
            String amount;            
            try {
                senderPublicKey = readFile(inputParsed[1], StandardCharsets.US_ASCII);
                senderPrivateKey = readFile(inputParsed[2], StandardCharsets.US_ASCII);
                recipientPublicKey = readFile(inputParsed[3], StandardCharsets.US_ASCII);
                amount = inputParsed[4];
                node1.makeTransaction(senderPublicKey, senderPrivateKey, recipientPublicKey, amount);
            }
            catch (Exception e) {
                System.out.println(e);
            }
            
        }
    }

    static String readFile(String path, Charset encoding) throws IOException {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
    }
}
