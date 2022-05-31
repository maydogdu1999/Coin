import java.net.*;
import java.util.*;
import java.io.*;

public class FrontEndConnection extends Thread {
    
    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    private Node source;

    public void run() {
        try {
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }

        catch(Exception e) {
            System.out.println("Exception in front end connection: " + e);
        }

        try {
            String message = input.readLine();
            System.out.println(message);
            handleMessage(message);

            
        }
        catch(IOException e) {
            System.out.println(e);

        }
    }

    public void handleMessage(String message) {
        String response = "";
        String[] messageParsed = message.split("--");

        if (messageParsed[0].equals("makeTransaction")) {
            String senderPublicKey;
            String senderPrivateKey;
            String recipientPublicKey;
            String amount;            
            try {
                if (source.getConnections().size() > source.getMaxNeighbors() ) {
                    System.out.println("Can't join: Max number of neighbors reached");
                }
                senderPublicKey = Driver.getKeyAsString(messageParsed[1]);
                senderPrivateKey = Driver.getKeyAsString(messageParsed[2]);
                recipientPublicKey = Driver.getKeyAsString(messageParsed[3]);
                amount = messageParsed[4];
                response = String.valueOf(source.makeTransaction(senderPublicKey, senderPrivateKey, recipientPublicKey, amount));

            }
            catch (Exception e) {
                System.out.println("couldn't do transaction in FrontEnd: " + e);
                e.printStackTrace();
                response = "false";
            }
            
        }

        if (messageParsed[0].equals("userBalance")) {
            String publicKey = Driver.getKeyAsString(messageParsed[1]);
            String balance = String.valueOf(source.getUserBalance(publicKey));

            response = balance;
        }

        try {
            output.writeBytes(response + "\n");
            output.flush();

            socket.close();
            input.close();
            output.close();
        }

        catch (Exception e) {
            System.out.println("Front End error: " + e);
        }


        
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setSource(Node source) {
        this.source = source;
    }
    
}
