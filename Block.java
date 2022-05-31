import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Comparator;

public class Block {
    
    //int representing the heigh of the block instance 
    int blockHeight;
    
    //String representing the previous block's toString, hashed
    String previousBlockHash;

    //hash of the transaction data within the block
    String selfHash;

    //String representing the timedate of the block's creation
    String timeDateCreation;

    //int representing number of transactions stored in the block
    int numTransactions;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //next block in the chain
    Block next = null;

    //previous block in the chain
    Block previous = null;

    //list of transactions in the block
    ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    ArrayList<String> DistinctKeys = new ArrayList<String>();

    /**
     * Block(String block) is a constructor used to create a block object from a given string. It works only on a properly formatted string, 
     * which is achieved via the toString method of a previously created block
     * @param block a string representing the block object being initialized
     */
    public Block(String block) {

        String[] parsedBlock = block.split("<><><><>");
        int counter = 1;

        //iterate over transactions in string, create Transaction instances for each and add to list
        while (!parsedBlock[counter].equals("blockMetaData")) {

            addTransaction(parsedBlock[counter].split("=-=-=")[1]);

            counter++;

        }
        
        counter++;

        //set block heigh
        this.blockHeight = Integer.parseInt(parsedBlock[counter]);

        counter++;

        //set self hash
        this.selfHash = parsedBlock[counter];

        counter++;

        //set previousBlockHash
        this.previousBlockHash = parsedBlock[counter];

        counter++;

        //set timedate
        this.timeDateCreation = parsedBlock[counter];

        //set numtransactions after adding all to list
        this.numTransactions = transactions.size();

    }

    /**
     * Block(ArrayList<Transaction> transactions, Block previousBlock) is another Block object constructor. It is used by Node.java to create block objects that are
     * used in the blockchain representation stored on the node. 
     * @param transactions an ArrayList of Transaction objects that represent all the transactions to be stored in the block
     * @param previousBlock A Block object that represents the previous block in the chain. Can be null if this is the first block created by the network (genesis block)
     */
    public Block(ArrayList<Transaction> transactions, Block previousBlock) {

        //set timeSTamp of creation
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

        this.transactions = new ArrayList<Transaction>(transactions);

        //If this is not the first block in the blockchain (genesis block), increment previous height by 1, set previous block hash
        if (!Objects.isNull(previousBlock)) {

            this.blockHeight = previousBlock.getBlockHeight() + 1;
            this.previousBlockHash = previousBlock.getSelfHash();

        } else {
            //otherwise, this is the first block in the chain, no previous block hash
            this.blockHeight = 1;
            this.previousBlockHash = "no previous block hash";

        }
        
        //format, set timedate
        this.timeDateCreation = sdf.format(timeStamp);

        //set numTransactions
        this.numTransactions = transactions.size();

        //set previousBlock
        this.previous = previousBlock;

        //set selfHash
        this.setSelfHash();

    }

    /**
     * This is a basic toString method that allows nodes to quickly convert a Block object to a string that can be broadcast on the network.
     */
    public String toString() {

        String block = "block<><><><>";

        //add each transaction to the string
        for (Transaction transaction: transactions) {
            block += transaction.toString() + "<><><><>";
        }

        //add all block meta data
        block += "blockMetaData<><><><>";

        block += blockHeight + "<><><><>" + selfHash + "<><><><>" + previousBlockHash + "<><><><>" + timeDateCreation;

        return block;
    }

    /**
     * addTransaction takes a string, initializes it as a transaction object, and adds it to the Block instance's list of transactions
     * @param transaction a String representing some transaction
     */
    public void addTransaction(String transaction) {

        Transaction t = new Transaction(transaction);

        for (String key : DistinctKeys) {

            if (!key.equals(t.getRecipient()) && !key.equals(t.getSender())) {

                DistinctKeys.add(key);
            }

        }

        transactions.add(t);
    }

    /**
     * getBalance allows one to check the balace of a given publicKey within the given Block. The user's balance is equivalent to the balance after the latest transaction in the block
     * Because transactions are sorted by timestamp within the block, this will be the first transaction with the given publicKey, which can be either the sender or recipient.
     * The method can also return false, in the event that the given publicKey does not appear in any transactions within the block
     * @param publicKey
     * @return
     */
    public Object getBalance(String publicKey) {

        Double balance;
        Transaction t;
        //transactions stored in ascending order. Therefore, iterating backwards from the end of the list, checking public key against those in the transaction will yield latest balance
        for (int i = transactions.size() -  1; i >= 0; i--) {

            t = transactions.get(i);

            //if recipient of given transaction is given publicKey, return balance of recipient
            if (t.getRecipient().equals(publicKey)) {

                balance = Double.parseDouble(t.getRecipientNewBalance());
                return balance;
            }
            //if sender of given transaction is equal to given publicKey, return balance of sender
            else if (t.getSender().equals(publicKey)) {

                balance = Double.parseDouble(t.getSenderNewBalance());
                return balance;
            }

        }
        //if iterated over whole list of transactions, given public key isn't either a recipient or a sender, then isn't present in block, return false
        return false;   
    }

    /**
     * Comparator to sort transactions by their timestamps
     */
    class sortTransaction implements Comparator<Transaction> {
        // Method
        // Sorting in ascending order of timestamps
            public int compare(Transaction A, Transaction B)  {
                
                Timestamp timeA = Timestamp.valueOf(A.getTime());
                Timestamp timeB = Timestamp.valueOf(B.getTime());
                return timeA.compareTo(timeB);
            }
    }

    /**
     * hashSHA256(String data) simply returns the hash value of the given string as a string. Used to hash transaction data within the block when setting selfHash
     * @param data String representing the data to be hashed 
     * @return String representing the hashed data
     */
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
            System.out.println(e);
            return  null;
        }
            
    }

    public void cleanBlock() {

        double balance;

        for (String key : DistinctKeys) {

            //balance of -1 signifies that the key has not yet been seen in the transaction set
            balance = -1; 

            for (Transaction t : transactions) {

                if (key.equals(t.getRecipient())) {

                        balance = Double.parseDouble(t.getRecipientNewBalance());

                } else if (key.equals(t.getSender())) {

                    if (balance == -1) {

                        balance = Double.parseDouble(t.getSenderNewBalance());

                    } else {

                        balance -= Double.parseDouble(t.getAmount());

                        if (balance != Double.parseDouble(t.getSenderNewBalance())) {

                            transactions.remove(t);

                        }

                    }

                }


            }

        }

    }

    /**
     *  SETTER AND GETTER METHODS
     */
    public Block getPrevious(){

        return previous;

    }

    public int getBlockHeight() {

        return blockHeight;

    }

    public String getSelfHash() {

        return selfHash;

    }

    public int getNumTransactions() {

        return numTransactions;

    }

    public ArrayList<Transaction> getTransactions() {

        return transactions;

    }

    public void setSelfHash() {

        String data = "";
        for (Transaction t: transactions) {
            data += t.toString();
        }

        this.selfHash = hashSHA256(data);

    }

    public void setNext(Block next) {
        this.next = next;
    }

    public void setPrevious(Block previous) {
        this.previous = previous;
    }
}   
