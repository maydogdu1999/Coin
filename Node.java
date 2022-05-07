import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import java.time.*;
import org.bouncycastle.crypto.BlockCipher;
import java.sql.Timestamp;  

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.security.spec.RSAPublicKeySpec;
import java.nio.charset.StandardCharsets;


/**
 * The Node class represents the main thread of each BowdoinCoin node. It consists of one primary data structure: connections, a concurrent hashmap that maps connection
 * objects, each representing a connection to a "neighbor" node, to strings, each string representing the IP address of the corresponding connection. It also maintains
 * a series of constants for the purpose of controlling the total number of neighbors, required verifications, among other things
 */
public class Node extends Thread {

    //CONSTANTS
    static final int MAX_NEIGHBORS = 5;
    static final int IDEAL_NEIGHBORS = 3;
    static final int TIME_OUT_MAKE_TRANSACTION = 3; //in seconds
    static final int MIN_REQUIRED_VERIFICATIONS = 1;
    static final int MINE_DIFFUCULTY = 3;
    static final int GRACE_PERIOD_LENGTH = 3; // in minutes

     
    static LocalDateTime startOfDay = null;


    //connections stores a node's "neighbors"
    ConcurrentHashMap<Connection, String> connections = new ConcurrentHashMap<Connection,String>();

    static ArrayList<ArrayList<String>> blockChain = new ArrayList<ArrayList<String>>();
    static ArrayList<String> currentBlock = new ArrayList<>();
    //one transaction is stored in the format: rawMessage + "=-=-=" + signedMessage + "=-=-=" + senderPublicKey + "=-=-=" + recipientPublicKey
    //rawMessage is stored in the format: time + "--" + amount + "--" + senderPublicKey + "--" + recipientPublicKey + "--" + senderNewBalance + "--" + recipientNewBalance;

    private int numNeighborsVerified = 0;
    private boolean notVerifiedByNeighbor = false;
    private boolean mined = false;
    private boolean receivedBlock = false;
    private boolean isGracePeriod = false;


    //connection socket
    private ServerSocket serv = null; 

    //the IP of the node's host machine
    private String hostIp; 

    //Port num of the node's process
    private int hostPort;

    //method that starts the node
    public void run() {

        System.out.println("Starting startServer...");

        Socket socket = null;

        //try to start server on given port
        try
        {
            //start on hostPort
            serv = new ServerSocket(hostPort);

            //setting ownIP
            hostIp = InetAddress.getLocalHost().toString().split("/")[1];

            //main server loop body
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

    /**
     * This method allows a node to connect to another node as a neighbor. It adds the node to the connections hashmap
     * @param ip a String representing the ip of the node to which it is connecting
     * @param port int representing port of peer
     */
    public synchronized void connectToPeer(String ip, int port) {

        System.out.println("Starting connectToPeer...");
        System.out.println("Connections before connectToPeer: " + connections.values());
        System.out.println("Trying to connect to machine: " + ip);

        //check list of connections, don't want to connect to the same node twice
        for (String connection: connections.values()) {
            if(connection.equals(ip)) {
                System.out.println("can't connect to the same ip twice");
                return;
            }
        }

        //check to make sure not connecting to self
        if(hostIp.equals(ip)) {
            System.out.println("can't connect to self");
            return;
        }

        Socket socket = null;

        //try to connect to given IP on given port, catch exception
        try {

            System.out.println("Starting socket...");
            //start connection, then thread
            socket = new Socket(ip, port);

            System.out.println("Creating thread object...");
            Connection conn = new Connection(socket, this);

            System.out.println("Starting thread...");
            conn.start();

            //put the new server connection into connections
            addConnection(conn, ip);

            System.out.println("connected to: " + ip + " at port: " + port);

        } catch (Exception e) {

            System.out.println(e);

        }
    }

    /**
     * A method that returns the IP address of the machine on the other end of the given socket
     * @param socketName socket connected to some machine
     * @return string representing the id of the other machine.
     */
    public String getIpFromSocket(Socket socketName) {

        InetSocketAddress sockaddr = (InetSocketAddress)socketName.getRemoteSocketAddress();
        InetAddress inaddr = sockaddr.getAddress();
        String ipString = inaddr.toString().split("/")[1];
        return ipString;
    
    }

    /**
     * A method that adds a given connection object to the connections hashmap 
     * @param connection connection object representing the connection to be added
     * @param ip string representing the ip of other machine in the connection
     * @return boolean representing success. 
     */    
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

    /**
     * A method to remove a given connection from the connections hashmap.
     * @param connection connection object representing some connection
     * @return a boolean representing success. 
     */
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

    /**
     * populateNeighbors is one of the special commands in the BowdoinCoin network. It allows a node to populate its list of neighbors by broadcasting a message across the network,
     * looking for other nodes that also have fewer than the ideal number. This is called by some connection thread that recieves a populate neighbors request, or by the node itself
     * @param ip String representing the ip of the node sending the request
     * @param port integer representing the port number of node sending the request
     * @param counter int representing the degree broadcast counter 
     */
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
     * A method that converts a given string into a private key
     * @param key String representing some private key
     * @return A PrivateKey object representing a private key
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
        
        if (Float.parseFloat(amount) < 0) {
            return false; //cannot send negative amounts
        }
        
        Transaction newTransaction = new Transaction(senderPublicKey, recipientPublicKey, amount, this);
        String rawMessage = newTransaction.transactionInfo();
        PrivateKey priKey = stringToPrivateKey(senderPrivateKey);

        String hashedMessaged = hashSHA256(rawMessage);
        String signedMessage = encryptMessage(hashedMessaged, priKey); //encrypt message is currently not hashing. it needs to hash.

        //send the transaction to all neighbors for verification
        //we need to send transaction message without hashing but verifyTransaction take hashed message which needs to be changed
        String message = "verifyTransaction" + "=-=-=" + rawMessage + "=-=-=" + signedMessage + "=-=-=" + senderPublicKey; 
        System.out.println("result of verification at the local node: " + verifyTransaction(message));

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
            blastTransaction(rawMessage, signedMessage, senderPublicKey, recipientPublicKey, null);
            return true;
        }

        else {
            return false;
        }

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

    public void blastTransaction(String rawMessage, String signedMessage, String senderPublicKey, String recipientPublicKey, Connection excluded) {
        //excluded parameter is used for not sending the transaction to the connection that gave it to us in the first place
        
        String message = rawMessage + "=-=-=" + signedMessage + "=-=-=" + senderPublicKey + "=-=-=" + recipientPublicKey; 

        if (isDuplicateTransaction(message)) {
            System.out.println("received duplicate transaction");
            return;
        }

        currentBlock.add(message);

        message = "blastTransaction" + "=-=-=" + message;

        for (Connection connection: connections.keySet()) {
            if(connection != excluded) {
                connection.sendMessage(message);
            }
        }
    }



    public Double getUserBalance(String userPublicKey) {
        
        String[] transactionParsed;
        String sender;
        String recipient;
        double amount;

        //check in the currentBlock initially
        for (int i = currentBlock.size() - 1; i >= 0; i--) {

            transactionParsed = currentBlock.get(i).split("=-=-=");
            sender = transactionParsed[2];
            recipient = transactionParsed[3];

            if (sender.equals(userPublicKey)) {
                amount = Float.parseFloat(transactionParsed[0].split("--")[4]);
                return amount;
            }

            if (recipient.equals(userPublicKey)) {
                amount = Float.parseFloat(transactionParsed[0].split("--")[5]);
                return amount;
            }

        }

        for (int i = blockChain.size() - 1; i >= 0; i--) {
            ArrayList<String> block = blockChain.get(i);
            for (int j = block.size() - 2; j >= 0; j--) { //skip the last element because it is the hash value

                transactionParsed = block.get(j).split("=-=-=");
                sender = transactionParsed[2];
                recipient = transactionParsed[3];

                if (sender.equals(userPublicKey)) {
                    amount = Float.parseFloat(transactionParsed[0].split("--")[4]);
                    return amount;
                }

                if (recipient.equals(userPublicKey)) {
                    amount = Float.parseFloat(transactionParsed[0].split("--")[5]);
                    return amount;
                }
            
            }

        }

        //if no such user found, then he's new. Set his amount to 100.
        return 100.0;

    }

    public boolean isDuplicateTransaction(String transaction) {
        String oldTransaction;

        //check in the currentBlock initially
        for (int i = currentBlock.size() - 1; i >= 0; i--) {
            oldTransaction= currentBlock.get(i);
            if (transaction.equals(oldTransaction)) {
                return true;
            }
        }
        return false;

    }


    public void mine() {
        Random rand = new Random();
        int n = rand.nextInt(1000000);
        int counter = 0;

        while (counter < MINE_DIFFUCULTY * 10) {
            if (rand.nextInt(1000000) == n) {
                counter += 1;
            }
            if (mined) {
                return;
            }
        }

        if (!mined) {
            mined = true;
            blastMined(null);
            String block = "";
            System.out.println("I mined first");
            setReceivedBlock(true);

            sortCurrentBlock();

            for (String transaction: currentBlock) {
                block += transaction + "<><><><>";
            }
        
            String blockHash = hashSHA256(block);
            System.out.println("mined with hash: " + blockHash);


            block += blockHash;
            block = "block<><><><>" + block;
            blastBlock(block, null);
        }
        //need to sort and form block and send
    }

    public void blastBlock(String block, Connection excluded) {

        for (Connection connection: connections.keySet()) {
            if (connection != excluded) {
                connection.sendMessage(block);
            }
        }

        String[] blockParsed = block.split("<><><><>");
        ArrayList<String> blockAsList = new ArrayList<String>(Arrays.asList(blockParsed));  
        blockAsList.remove(0);

        blockChain.add(blockAsList);
        currentBlock = new ArrayList<String>();

    }

    public void blastMined(Connection excluded) {

        String message = "MINED";

        for (Connection connection: connections.keySet()) {
            if(connection != excluded) {
                connection.sendMessage(message);
            }
        }
    }

    public void sortCurrentBlock() {
        System.out.println("current block: " + currentBlock);
        Collections.sort(currentBlock, new sortTransaction());
    }
   


    class sortTransaction implements Comparator<String> {
    // Method
    // Sorting in ascending order of timestamps
        public int compare(String A, String B)  {
            System.out.println("in comparator:  " + A.split("=-=-=")[0].split("--")[0]);
            Timestamp timeA = Timestamp.valueOf(A.split("=-=-=")[0].split("--")[0]);
            Timestamp timeB = Timestamp.valueOf(B.split("=-=-=")[0].split("--")[0]);
            return timeA.compareTo(timeB);
        }
    }

    /**
     * SERIES OF GETTERS, SETTERS
     * 
     */

    public boolean isReceivedBlock() {
        return receivedBlock;
    }
    public void setReceivedBlock(boolean value) {
        receivedBlock = value;
    }

    public ArrayList<String> getCurrentBlock() {
        return currentBlock;
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

    public int getMaxNeighbors() {
        return MAX_NEIGHBORS;
    }

    public String getHostIp() {

        return hostIp;

    }

    public ConcurrentHashMap<Connection, String> getConnections() {
        return connections;
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

    public void setMined(Boolean value) {
        mined = value;
    }

    public boolean getMined() {
        return mined;
    }

    public void setStartOfDay(String start) {
        startOfDay = LocalDateTime.parse(start);
    }

    public void setStartOfDay(LocalDateTime time) {
        startOfDay = time;
    }


    public LocalDateTime getStartOfDay() {
        return startOfDay;
    }

    public boolean isGracePeriod() {
        return isGracePeriod;
    }


    public void setGracePeriod(boolean value) {
        isGracePeriod = value;
    }

    public ArrayList<ArrayList<String>> getBlockChain() {
        return blockChain;
    }  

    public int getGracePeriodLength() {
        return GRACE_PERIOD_LENGTH;
    }  


    public String printBlockChain() {
        String info = "";
        ArrayList<String> curBlock;
        int size;
        for (int i = 0; i < blockChain.size(); i++) {
            curBlock = blockChain.get(i);
            size = curBlock.size();
            info += "block " + i + " >>> numTransactions: " + (size - 1) + "  hashValue: " + curBlock.get(size - 1) + "\n";

        }

        return info;


    }  



}

