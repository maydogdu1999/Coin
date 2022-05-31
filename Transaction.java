import java.sql.Timestamp;
import java.text.SimpleDateFormat;
public class Transaction {

    //String representing timedate transaction is created
    private String time;

    //String representing the number of BowdoinCoins to be sent
    private String amount;

    //String representing the publicKey sending the coins
    private String sender; 

    //String representing the publicKey of the recipient of the coins
    private String recipient;
    
    //String representing the balance of the sender after the transaction is completed
    private String senderNewBalance;

    //String representing the balance of the recipient after the transaction is completed
    private String recipientNewBalance;

    //String representing the signed transaction
    private String signature;

    //to format timedate
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * A constructor for a Transaction object to be used by Node.java. Creates a Transaction instance using data only the Node will have
     * @param senderPublicKey String representing the public key of the sender
     * @param recipientPublicKey String representing the public key of the recipient
     * @param amount String representing the amount of coins to be sent
     * @param senderPrivateKey String representing a private key of the sender 
     * @param node1 originating node to be used for some helper functions in creating the transaction object
     */
    public Transaction(String senderPublicKey, String recipientPublicKey, String amount, String senderPrivateKey, Node node1) {
        
        //create timestamp
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        this.time = sdf.format(timeStamp);

        //set amount
        this.amount = amount;

        //set publicKey of sender
        this.sender = senderPublicKey;

        //set publicKey of recipient
        this.recipient = recipientPublicKey;

        //set new balance of the sender
        this.senderNewBalance = String.valueOf(node1.getUserBalance(senderPublicKey) - Float.parseFloat(amount));

        //set new balance of the recipient
        this.recipientNewBalance = String.valueOf(node1.getUserBalance(recipientPublicKey)  + Float.parseFloat(amount));

        //generate signature
        this.setSignature(senderPrivateKey, node1);
    }

    /**
     * Constructor to be used to create a transaction object from a Transaction object's info method. Used to easily generate a Transaction instance representing a transaction 
     * recieved from the network
     * @param info String representing the information of a transaction
     */
    public Transaction(String info) {

        String[] transactionData = info.split("--");

        //setting data based on split string
        this.time = transactionData[0];
        this.amount = transactionData[1];
        this.sender = transactionData[2];
        this.recipient = transactionData[3];
        this.senderNewBalance = transactionData[4];
        this.recipientNewBalance = transactionData[5];
        this.signature = transactionData[6];
    }

    /**
     * setSignature(String privatekey, Node n) sets the signature of a given transaction, using the method in Node.java, and the private key of the sender
     * @param privateKey String representing the private key of the sender
     * @param n Node object representing node creating the transaction
     */
    public void setSignature(String privateKey, Node n) {

        this.signature = n.encryptMessage(n.hashSHA256(this.transactionInfo()), n.stringToPrivateKey(privateKey));
    }

    /**
     * transactionInfo returns a string that represents ONLY the data of the transaction, not including the header used to send the transaction.
     * @return String representing data of the transaction
     */
    public String transactionInfo() {

        String info = time + "--" + amount + "--" + sender + "--" + recipient + "--" + senderNewBalance + "--" + recipientNewBalance;
        return info;
    }

    /**
     * toString representing the transaction string that will be broadcast across the network
     */
    public String toString() {

        return "transaction" + "=-=-=" + this.transactionInfo() + "--" + this.signature;
    }

    /**
     * GETTERS AND SETTERS
    */
    public String getSender() {

        return sender;

    }

    public String getRecipient() {

        return recipient;

    }

    public String getSenderNewBalance() {

        return senderNewBalance;

    }

    public String getRecipientNewBalance() {

        return recipientNewBalance;

    }

    public String getTime() {

        return time;

    }

    public String getSignature() {

        return signature;

    }

    public String getAmount() {

        return amount;

    }

}