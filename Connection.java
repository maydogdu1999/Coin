import java.net.*;

import java.io.*;


public class Connection extends Thread{
    static final int MAX_DEGREES = 4;

    private DataInputStream input = null;

    private DataOutputStream output = null;

    private Socket sock = null;

    //the node on the machine that originated this connection instance
    private Node source = null;

    //constructor 
    public Connection(Socket s, Node source) {

        sock = s;
        this.source = source;

    }
    

    /**
     * This is the function that the thread runs when called. It initializes data streams and listens for messages from the node it is connected to.
     * It parses messages upon recieving them. Connection termination is discovered by an EOF exceptionl; if the other node fails for whatever reason,
     * the connection is closed, and the corresponding entry is removed from the connections hashmap in the source node. 
     * 
     * PARAMS: ~
     * RETURNS: void
     */
    public void run(){

        try {

            input = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
            output = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));

        }
        catch (IOException e) {

            System.out.println(e);

        }
        System.out.println("Started Connection!");

        sendMessage("Connected!");

        //to represent a given message
        String message;

        //to help determine if connection that is the thread has stopped.
        boolean is_connected = true; 


        //while the socket is open, not throwing file error, keep reading new lines, parsing. Else break, because connection has closed
        while (is_connected) {
            try {

                message = input.readUTF();
                parseMessage(message);
                
            }
            catch(IOException i) {
                is_connected = false;
                System.out.println(i);
                closeConnection();
                
            }
        }
    }

    /**
     * This method takes care of closing the socket and data streams upon the loss of connection to a node, and removes it from the 
     * connections hashmap of the source node
     * 
     * PARAMS: ~
     * RETURNs: void
     */
    public void closeConnection() {

        //close socket, data streams
        try {
            sock.close();
            input.close();
            output.close();
        }

        catch (IOException e) {
            System.out.println(e);
        }

        //remove connection from connections hashmap
        if (source.removeConnection(this)) {
        
            System.out.println("removed connection successfully");
        }
        else {
            System.out.println("couldn't remove conn");
        }
    }

    /**
     * This function sends a message to the node on the other end of the connection's socket. No formatting or anything, 
     * simply writes message to socket. 
     * 
     * PARAMS: message - a string representing the message to be sent.
     * RETURNS: void
     * @param message
     */
    public void sendMessage(String message) {
        
  

        try {
            output.writeUTF(message);
            output.flush();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * parseMessage parses a given string, identifying one of the few potential excepted commands, fails to parse otherwise
     * 
     * PARAMS: message - a string representing the message read in the body while loop of the run method
     * RETURNS: void
     * @param message
     */
    public void parseMessage(String message) {

        System.out.println(message);

        String[] parsedMessage = message.split("=-=-=");

        if (parsedMessage[0].equals("populateNeighbors")) {

            String ip = parsedMessage[1];
            int port = Integer.parseInt(parsedMessage[2]);
            int counter = Integer.parseInt(parsedMessage[3]);
            handlePopulateNeighbors(ip, port, counter);
            return;
        }

        //if message is a transaction message
        if (parsedMessage[0].equals("transaction")) {

            Transaction t = new Transaction(parsedMessage[1]);

            //verify the transaction was signed by the public key
            if (source.verifyTransaction(t)) {
                
                if (source.addTransaction(t)) {
                    //if successfully added the transaction to source node's list, blast it to neighbors, 
                    source.blast(message, this);

                }

            }
            return;
        }
        
        //if we get this far without returning, must be a block message, split accordingly
        String[] parsedBlock = message.split("<><><><>");

        if(parsedBlock[0].equals("block")) {

            //handle block message
            source.handleIncomingBlock(message, this);
            

        }

    }

    /**
     * handlePopulateNeighbors is the function to handle a message from a node to populate neighbors. 
     * If the source node of the connection has fewer than the maximum neighbor number, it will 
     * attempt to connect to the node at the given IP and port. If the counter is less than the max counter 
     * constant, the source node will relay the populate neighbors message to its own neighbors. Otherwise, 
     * it will not - this is to prevent requests from circulating infinitely. 
     * 
     * RETURNS: void
     *          
     * @param ip: The IP of the machine originally sending the populateNeighbors request. This is the machine looking for neighbors
     * @param port: the corresponding port number of ip
     * @param counter: a counter representing the number of nodes that have forwarded this specific message. 
     */
    public void handlePopulateNeighbors(String ip, int port, int counter) {
        System.out.println("here in connection handlePopulateNeighbors");

        //check to see if some other node has sent you your own populate neighbors request
        Boolean isSelf = (ip.equals(source.getHostIp()));
        Connection newCon = null;

        //don't send populate neighbors request to self
        if (isSelf) {

            System.out.println("received own populate neighbors request...");
            return;

        }

        //check if source node connections less than max
        if ((source.getConnections()).size() < source.getMaxNeighbors()) {
            
            //if so, connect to node sending populate neighbors request
            newCon = source.connectToPeer(ip, port);
        } 

        //check if repeat counter less than max
        if (counter < MAX_DEGREES){

            //if repeat max not reached, broadcast the message again
            source.populateNeighbors(ip, port, ++counter, newCon);
        }

        else {
            System.out.println("max degrees reached");
        }

    }

    public DataOutputStream getOutputStream() {
        return this.output;
    }


}
