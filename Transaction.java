public class Transaction {

    private int month;
    private int day;
    private int year;
    private int hour;
    private int sec;
    private double amount;
    private String sender; //sender public key
    private String recipient; //recipient public key

    public Transaction(int month, int day, int year, int hour, int sec, double amount, String sender, String sender) {
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
        String info = str(month) + "-" + str(day) + "-" + str(year) + "-" + str(hour) + "-" + str(sec) + "-" + str(amount) + "-" + sender + "-" + recipient;
        return info;
    }






}