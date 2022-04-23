import java.net.*;
import java.io.*;


public class Connection extends Thread{
    static final int MAX_DEGREES = 3;

    private DataInputStream input = null;

    private DataOutputStream output = null;

    private Socket sock = null;
    private Node source = null;

    public Connection(Socket s, Node source) {

        sock = s;
        this.source = source;
    }
    
    public void run(){

        try {

            input = new DataInputStream(new BufferedInputStream(sock.getInputStream()));

            output = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));

        }

        catch (IOException e) {

            System.out.println(e);

        }

        System.out.println("Started Connection!");

        
        String line = "";
        while (!line.equals("Close connection")) {
            try {
                line = input.readUTF();
                System.out.println(line);
                parseMessage(line);
                
            }
            catch(IOException i) {
                System.out.println(i);
                break;
            }
        }

        if (source.removeConnection(this)) {
            System.out.print("after removed " + source.getConnections().keySet());

            System.out.println("removed conn successfully");
        }
        else {
            System.out.println("couldn't remove conn");
        }

        System.out.println("Closing connection");
 
        // close connection
        try {
            sock.close();
            input.close();
            output.close();
        }

        catch (IOException e) {

            System.out.println(e);

        }

    }

    public void sendMessage(String message) {
        System.out.println(output);
        try {
            output.writeUTF(message);
            output.flush();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public void parseMessage(String message) {

        String[] parsedMessage = message.split("--");
        System.out.println("here in cnnection parsemessage");
        System.out.println(parsedMessage[0]);
        if (parsedMessage[0].equals("populateNeighbors")) {

            String ip = parsedMessage[1];
            int port = Integer.parseInt(parsedMessage[2]);
            int counter = Integer.parseInt(parsedMessage[3]);
            handlePopulateNeighbors(ip, port, counter);
        }
    }
    public void handlePopulateNeighbors(String ip, int port, int counter) {
        System.out.println("here in connection handlepopulateneighbors");
        if (source.getConnections().size() < source.getMaxNeighbors()) {
            source.connectToPeer(ip, port);
        } 

        if (counter < MAX_DEGREES){
            System.out.println("popu from conn");
            source.populateNeighbors(ip, port, ++counter);
        }

        else {
            System.out.println("max degrees reached");
        }

    }


}
