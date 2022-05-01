import java.net.*;
import java.util.*;

import java.io.*;
import java.util.Scanner;
import java.util.logging.Handler;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.bouncycastle.util.io.pem.PemReader;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.security.spec.RSAPublicKeySpec;
import java.nio.charset.StandardCharsets;


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

        if (inputParsed[0].equals("printConnections")) {
            System.out.println("numConnections: " + node1.getConnections().size() + " connections: " + node1.getConnections().values());
        }

        if (inputParsed[0].equals("makeTransaction")) {
            String senderPublicKey;
            String senderPrivateKey;
            String recipientPublicKey;
            String amount;            
            try {
                if (node1.getConnections().size() > node1.getMaxNeighbors() ) {
                    System.out.println("Can't join: Max number of neighbors reached");
                }
                senderPublicKey = getKeyAsString(inputParsed[1]);
                senderPrivateKey = getKeyAsString(inputParsed[2]);
                recipientPublicKey = getKeyAsString(inputParsed[3]);
                amount = inputParsed[4];
                Boolean success = node1.makeTransaction(senderPublicKey, senderPrivateKey, recipientPublicKey, amount);
                System.out.println("result of makeTransaction: " + success);
            }
            catch (Exception e) {
                System.out.println("couldn't do transaction: " + e);
            }
            
        }
                
            }
            catch (Exception e) {
                System.out.println(e);

            }
            
        }    

    }

    public static String getKeyAsString(String filename) {
        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);  
            byte[] keyBytes = new byte[(int) file.length()];
            dis.readFully(keyBytes);
            dis.close();
            String keyAsString = Base64.getEncoder().encodeToString(keyBytes);
            return keyAsString;
        }
        catch (Exception e) {
            return null;
        }
        
    }
    
    
}
