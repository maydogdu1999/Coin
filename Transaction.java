import java.sql.Timestamp;
import java.text.SimpleDateFormat;
public class Transaction {
    private String time;
    private String amount;
    private String sender; //sender public key
    private String recipient; //recipient public key
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    
    public Transaction(String senderPublicKey, String recipientPublicKey, String amount) {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        this.time = sdf.format(timeStamp);
        this.amount = amount;
        this.sender = senderPublicKey;
        this.recipient = recipientPublicKey;
    }

    public String transactionInfo() {
        String info = time + "--" + amount + "--" + sender + "--" + recipient;
        return info;
    }


}