public class Transaction {

    private String month;
    private String day;
    private String year;
    private String hour;
    private String sec;
    private String amount;
    private String sender; //sender public key
    private String recipient; //recipient public key

    
    public Transaction(String month, String day, String year, String hour, String sec, String amount, String sender, String recipient) {
        this.month = month;
        this.day = day;
        this.year = year;
        this.hour = hour;
        this.sec = sec;
        this.amount = amount;
        this.sender = sender;
        this.recipient = recipient;
    }

    public String transactionInfo() {
        String info = month + "-" + day + "-" + year + "-" + hour + "-" + sec + "-" + amount + "-" + sender + "-" + recipient;
        return info;
    }






}