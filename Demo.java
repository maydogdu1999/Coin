import java.net.*;
import java.util.*;

import java.io.*;
import java.util.Scanner;

import java.util.concurrent.TimeUnit;


public class Demo {
    
    static Node n;

    public static void main(String[] args) {
        
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
            
            node1.setHostIp(getIp());
        }
        catch (Exception e) {
            System.out.println(e);
        }


        node1.start();


        Random rand = new Random();

        int sender = rand.nextInt(10) + 1;

        int recipient = rand.nextInt(10) + 1;

        for (int i = 0; i < 10; i++) {

            node1.makeTransaction("keys/public_key" + sender + ".der", "keys/private_key" + sender + ".der", "keys/public_key" + recipient + ".der", "1");

            sender = rand.nextInt(10) + 1;

            recipient = rand.nextInt(10) + 1;



        }


    }
    public static String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
