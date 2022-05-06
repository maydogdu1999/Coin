import java.sql.Timestamp;
import java.text.SimpleDateFormat;
public class Transaction {
    private String time;
    private String amount;
    private String sender; //sender public key
    private String recipient; //recipient public key
    private String senderNewBalance;
    private String recipientNewBalance;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    
    public Transaction(String senderPublicKey, String recipientPublicKey, String amount, Node node1) {
        
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        this.time = sdf.format(timeStamp);
        this.amount = amount;
        this.sender = senderPublicKey;
        this.recipient = recipientPublicKey;
        this.senderNewBalance = String.valueOf(node1.getUserBalance(senderPublicKey) - Float.parseFloat(amount));
        this.recipientNewBalance = String.valueOf(node1.getUserBalance(recipientPublicKey)  + Float.parseFloat(amount));
    }

    public String transactionInfo() {
        String info = time + "--" + amount + "--" + sender + "--" + recipient + "--" + senderNewBalance + "--" + recipientNewBalance;
        return info;
    }


}