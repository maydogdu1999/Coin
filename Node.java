import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.security.spec.RSAPublicKeySpec;
import java.nio.charset.StandardCharsets;


public class Node extends Thread {
    static final int MAX_NEIGHBORS = 5;
    static final int IDEAL_NEIGHBORS = 3;
    static final int TIME_OUT_MAKE_TRANSACTION = 3; //in seconds
    static final int MIN_REQUIRED_VERIFICATIONS = 1;



    //private ArrayList<String> neighbors = new ArrayList<String>(); //an array of Strings containing the IP addresses of the Node's neighbors.
    ConcurrentHashMap<Connection, String> connections = new ConcurrentHashMap<Connection,String>();
    
    private int numNeighborsVerified = 0;
    private boolean notVerifiedByNeighbor = false;

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
            String message = "populateNeighbors=-=-=" + ip + "=-=-=" + String.valueOf(port) + "=-=-=" + String.valueOf(counter);

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

    /**
     * A method that converts a given string into a public key, necessary because key will be a string when read in from socket
     * @param key String representing some public key
     * @return A PublicKey object representing a public key
     */
    public PublicKey stringToPublicKey(String key) {

        RSAPublicKey pubKey = null; 

        try {
        byte[] pubKeyByte = Base64.getDecoder().decode(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyByte);

        KeyFactory keyFac = KeyFactory.getInstance("RSA");

        pubKey = (RSAPublicKey) keyFac.generatePublic(keySpec);
        
        } catch (Exception e) {

            System.out.println(e);

        }

        return pubKey;

    }

     /**
     * A method that converts a given string into a public key, necessary because key will be a string when read in from socket
     * @param key String representing some public key
     * @return A PublicKey object representing a public key
     */
    public PrivateKey stringToPrivateKey(String key) {

        RSAPrivateKey priKey = null; 

        try {
        byte[] priKeyByte = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priKeyByte);

        KeyFactory keyFac = KeyFactory.getInstance("RSA");

        priKey = (RSAPrivateKey) keyFac.generatePrivate(keySpec);
        
        } catch (Exception e) {

            System.out.println(e);

        }

        return priKey;

    }

    /**
     * This method encrypts a given string using the given private key. This is called "signing" a string. The resulting string can be decrypted using the corresponding
     * public key.
     * @param message A String to be encrypted
     * @param privateKey a String representing the private key with which to encrypt
     * @return a String representing the encrypted message
     */
    public String encryptMessage(String message, PrivateKey privateKey) {
        

        Cipher encrypt = null; 

        String encryptedMessage = "";

        try {

            
            encrypt = Cipher.getInstance("RSA");

            encrypt.init(Cipher.ENCRYPT_MODE, privateKey);

            encryptedMessage = Base64.getEncoder().withoutPadding().encodeToString(encrypt.doFinal(Base64.getDecoder().decode(message)));

        } catch (Exception e) {

            System.out.println(e);

        }

        return encryptedMessage;
    }

    /**
     * This method decrypts a given signature using the given public key. The resulting value is used by verifyTransaction to determine whether a message has been 
     * properly signed (has been authorized by the given public key)
     * @param signedMessage a String representing the "signature" in a transaction message
     * @param publicKey a String representing the public key of the keypair attempting to make a transaction
     * @return a String representing the hashed transaction message. 
     */
    public String decryptMessage(String signedMessage, String publicKey) {

        

        String decryptedMessage = "";

        PublicKey pubKey = stringToPublicKey(publicKey);

        Cipher decrypt = null;

        try {

            
            decrypt = Cipher.getInstance("RSA");

            decrypt.init(Cipher.DECRYPT_MODE, pubKey);

            decryptedMessage = Base64.getEncoder().withoutPadding().encodeToString(decrypt.doFinal(Base64.getDecoder().decode(signedMessage.trim())));

        } catch (Exception e) {

            System.out.println(e);

        }

        return decryptedMessage;
    }

    /**
     * A method that determines whether the transaction has been properly signed. If so, it can be written to the blockchain. It takes the raw message recieved over 
     * the socket, and outputs a boolean that is true if verification is successful, and false otherwise. 
     * @param message A string that is the transaction message read in from socket.
     * @return a boolean representing whether the transaction has been verified 
     */
    public Boolean verifyTransaction(String message) {

        String[] splitMsg = message.split("=-=-=");

        String rawMessage = splitMsg[1];

        String signedMessage = splitMsg[2];

        String senderPublicKey = splitMsg[3];

        return hashSHA256(rawMessage).equals(decryptMessage(signedMessage, senderPublicKey));

    }

    public Boolean makeTransaction(String senderPublicKey, String senderPrivateKey, String recipientPublicKey, String amount) {
        Transaction newTransaction = new Transaction(senderPublicKey, recipientPublicKey, amount);
        PrivateKey priKey = stringToPrivateKey(senderPrivateKey);
        String rawMessage = newTransaction.transactionInfo();
        String hashedMessaged = hashSHA256(rawMessage);
        String signedMessage = encryptMessage(hashedMessaged, priKey); //encrypt message is currently not hashing. it needs to hash.

        //send the transaction to all neighbors for verification
        //we need to send transaction message without hashing but verifyTransaction take hashed message which needs to be changed
        String message = "verifyTransaction" + "=-=-=" + rawMessage + "=-=-=" + signedMessage + "=-=-=" + senderPublicKey; 
        System.out.println("result of verification: " + verifyTransaction(message));

        setNumNeighborsVerified(0);
        setNotVerifiedByNeighbor(false);

        for (Connection connection: connections.keySet()) {
            //create properly formatted message
            connection.sendMessage(message);
        }
        
        long startTime = System.currentTimeMillis();

        while (true) {

            if (notVerifiedByNeighbor) {
                return false;
            }

            else if (getNumNeighborsVerified() == getConnections().size()) {
                break;
            }

            else if ((System.currentTimeMillis() - startTime) > 1000 * TIME_OUT_MAKE_TRANSACTION) {
                break;
            }
        }

        if (getNumNeighborsVerified() >= MIN_REQUIRED_VERIFICATIONS) {
            return true;
        }

        return false;
    }

    public String hashSHA256(String data) {
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }

        catch (Exception e) {
            System.out.println(e);
            return  null;
        }

        
    }

    public synchronized void setNumNeighborsVerified(int num) {
        numNeighborsVerified = num;
    }

    public synchronized int getNumNeighborsVerified() {
        return numNeighborsVerified;
    }

    public synchronized void setNotVerifiedByNeighbor(Boolean value) {
        notVerifiedByNeighbor = value;
    }
}