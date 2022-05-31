import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import javax.crypto.Cipher;
import java.time.*;
import java.sql.Timestamp;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


import java.nio.charset.StandardCharsets;


/**
 * The Node class represents the main thread of each BowdoinCoin node. It consists of one primary data structure: connections, a concurrent hashmap that maps connection
 * objects, each representing a connection to a "neighbor" node, to strings, each string representing the IP address of the corresponding connection. It also maintains
 * a series of constants for the purpose of controlling the total number of neighbors, required verifications, among other things
 */
public class Node extends Thread {

    //CONSTANTS
    static final int MAX_NEIGHBORS = 12;
    static final int IDEAL_NEIGHBORS = 8;
    static final int TIME_OUT_MAKE_TRANSACTION = 3; //in seconds

    static int MINE_DIFFUCULTY = 4;
    static final int GRACE_PERIOD_LENGTH = 3; // in minutes
    static final double NEW_USER_DEFAULT_BALANCE = 100.0; // in cryptocurrency unit


    static LocalDateTime startOfDay = null;

   


    //connections stores a node's "neighbors"
    ConcurrentHashMap<Connection, String> connections = new ConcurrentHashMap<Connection,String>();

    Block topBlock = null;

    static ArrayList<Transaction> currentBlock = new ArrayList<Transaction>();
    //one transaction is stored in the format: rawMessage + "=-=-=" + signedMessage + "=-=-=" + senderPublicKey + "=-=-=" + recipientPublicKey
    //rawMessage is stored in the format: time + "--" + amount + "--" + senderPublicKey + "--" + recipientPublicKey + "--" + senderNewBalance + "--" + recipientNewBalance;

    private int numNeighborsVerified = 0;
    
    private boolean mined = false;
    private String mineHash = "";
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
    public synchronized Connection connectToPeer(String ip, int port) {

        System.out.println("Starting connectToPeer...");
        System.out.println("Connections before connectToPeer: " + connections.values());
        System.out.println("Trying to connect to machine: " + ip);
        System.out.println("Host IP: " + hostIp);

        //check list of connections, don't want to connect to the same node twice
        for (String connection: connections.values()) {
            if(connection.equals(ip)) {
                System.out.println("can't connect to the same ip twice");
                return null;
            }
        }

        //check to make sure not connecting to self
        if(hostIp.equals(ip)) {
            
            System.out.println("can't connect to self");
            return null;
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
            return conn;

        } catch (Exception e) {

            System.out.println(e);

        }
        return null;
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
    public synchronized Boolean addConnection(Connection connection, String ip) {

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
    public void populateNeighbors(String ip, int port, int counter, Connection excluded) {
        
        System.out.println("Starting populate Neighbors...");
        System.out.print("connections while in populate neighbors: ");
        System.out.println(connections.values());
        String message = "populateNeighbors=-=-=" + ip + "=-=-=" + String.valueOf(port) + "=-=-=" + String.valueOf(counter);

        //for each neighbor
        for (Connection connection: connections.keySet()) {

            //create properly formatted message
            if (connection != excluded) {
                while (connection.getOutputStream() == null) {
                    //waiting for a connection to settle
                }
                connection.sendMessage(message);
            }

        }
    }

    public void blast(String message, Connection excluded) {

        System.out.println("Blasting...");

        for (Connection connection: connections.keySet()) {
            if(connection != excluded) {
                connection.sendMessage(message);
            }
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

            e.printStackTrace();
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

            e.printStackTrace();
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

            e.printStackTrace();
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
    public Boolean verifyTransaction(Transaction t) {


        String rawMessage = t.transactionInfo();

        String signedMessage = t.getSignature();

        String senderPublicKey = t.getSender();

        return hashSHA256(rawMessage).equals(decryptMessage(signedMessage, senderPublicKey));

    }

    public synchronized boolean makeTransaction(String senderPublicKey, String senderPrivateKey, String recipientPublicKey, String amount) {
        
        if (Double.parseDouble(amount) < 0) {
            System.out.println("Cannot send negative amounts");
            return false ; //cannot send negative amounts
        }
        
        Transaction newTransaction = new Transaction(senderPublicKey, recipientPublicKey, amount, senderPrivateKey, this);

        if (!verifyTransaction(newTransaction)) {
            return false;
        }

        currentBlock.add(newTransaction);

        blast(newTransaction.toString(), null);
        return true;

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
            e.printStackTrace();
            return  null;
        }
        
    }

    public Double getUserBalance(String userPublicKey) {
        
        Block b = topBlock;
        Object balance;

        //System.out.println(userPublicKey);

        if (currentBlock.size() > 0) {

            balance = getBalanceFromCurrentBlock(userPublicKey);
            if (!balance.equals(false)) {
                return (Double) balance;
            }
        }

        if (Objects.isNull(b)) {
            return 100.0;
        }

        balance = b.getBalance(userPublicKey);
        
        while (balance.equals(false)) {

            if (b.getPrevious() == null) {

                return 100.0;

            }

            balance = b.getPrevious().getBalance(userPublicKey);
            b = b.getPrevious();
            

        }
        return (Double) balance;

    }

    public Object getBalanceFromCurrentBlock(String key) {

        //sort current block's transactions in ascending order
        Collections.sort(currentBlock, new sortTransaction());

        //return latest wallet value of given key
        for (int i = currentBlock.size() -  1; i >= 0; i--) {

            if (currentBlock.get(i).getRecipient().equals(key)) {

                return Double.parseDouble(currentBlock.get(i).getRecipientNewBalance());

            } else if (currentBlock.get(i).getSender().equals(key)) {

                return Double.parseDouble(currentBlock.get(i).getSenderNewBalance());

            }
        }

        return false;


    }

    public synchronized Boolean addTransaction(Transaction transaction) {

        if (!isDuplicateTransaction(transaction)) {
            currentBlock.add(transaction);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isDuplicateTransaction(Transaction t) {
        
        Transaction oldTransaction;

        //check in the currentBlock initially
        for (int i = currentBlock.size() - 1; i >= 0; i--) {
            oldTransaction= currentBlock.get(i);
            if (t.toString().equals(oldTransaction.toString())) {
                return true;
            }
        }
        return false;

    }


    public synchronized void buildBlock() {
            
        sortCurrentBlock();

        Block newBlock = new Block(currentBlock, topBlock);
        
        /** 
        if (!Objects.isNull(topBlock)) {

            topBlock.setNext(newBlock);

        }
        */

        topBlock = newBlock;

        newBlock.cleanBlock();

        System.out.println(newBlock.toString());

        blast(newBlock.toString(), null);
        removeMinedTransactions(newBlock);


    }

    public synchronized void  sortCurrentBlock() {
        //System.out.println("current block: " + currentBlock);
        Collections.sort(currentBlock, new sortTransaction());
    }
   


    class sortTransaction implements Comparator<Transaction> {
        // Method
        // Sorting in ascending order of timestamps
            public int compare(Transaction A, Transaction B)  {
                
                Timestamp timeA = Timestamp.valueOf(A.getTime());
                Timestamp timeB = Timestamp.valueOf(B.getTime());
                return timeA.compareTo(timeB);
            }
        }

    public synchronized void removeMinedTransactions(Block block) {

        //split block, get transactions
        ArrayList<Transaction> transactions = block.getTransactions();
        //ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();

        for (int i = currentBlock.size() - 1; i >= 0; i--) {

            for (int j = transactions.size() - 1; j >= 0 ; j--) {

                if (currentBlock.get(i).transactionInfo().equals(transactions.get(j).transactionInfo())) {

                    currentBlock.remove(i);
                    break;
                }
            }
        }

    }
    public synchronized void handleIncomingBlock(String message, Connection excluded) {
        if(isDuplicateBlock(message)) {
            System.out.println("received same block");
            return;
        }
        else {
            Block b = new Block(message);
            System.out.println("received new block");
            addBlock(b);
            blast(message, excluded);
            
            removeMinedTransactions(b);
            return;

        }
    }

    public boolean isDuplicateBlock(String block) {
        if (Objects.isNull(topBlock)) {
            return false;
        }
        if (topBlock.toString().equals(block)) {
            return true;
        }
        return false;
    }

    public synchronized void addBlock(Block newBlock) {

        if (Objects.isNull(topBlock)) {

            newBlock.setPrevious(null);
            //note previous hash never initialized in this case, might be issue?
            topBlock = newBlock;
            return;

        }

        //topBlock.setNext(newBlock);

        newBlock.setPrevious(topBlock);
        topBlock = newBlock;

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

    public ArrayList<Transaction> getCurrentBlock() {
        return currentBlock;
    }

    public synchronized void setNumNeighborsVerified(int num) {
        numNeighborsVerified = num;
    }

    public synchronized int getNumNeighborsVerified() {
        return numNeighborsVerified;
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

    public synchronized void setMined(Boolean value) {
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


    public int getGracePeriodLength() {
        return GRACE_PERIOD_LENGTH;
    }  

    public int getMineDifficulty() {

        return MINE_DIFFUCULTY;

    }

    public void setMineHash(String hash) {

        mineHash = hash;

    }

    public String getMineHash() {

        return mineHash;

    }

    public void setDifficulty(int diff) {

        MINE_DIFFUCULTY = diff;

    }

    public String printBlockChain() {
        String info = "";

        Block b = topBlock;

        System.out.println("Beginning printBlockchain...");

        if (Objects.isNull(b)) {
            info = "No blocks in the chain yet";
            return info;
        }

        //println("Passed the first conditional...");

        while (!Objects.isNull(b)) {

            info += "block " + b.getBlockHeight() + " >>> numTransactions: " + b.getNumTransactions() + "  hashValue: " + b.getSelfHash() + "\n";
            b = b.getPrevious();
        }

        return info;


    }  



}

