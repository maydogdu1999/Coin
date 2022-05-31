import java.util.Random;
public class Test1 extends Thread {

    public Node source = null;

    public long start = 0;

    public int num = 0;
    public void run() {
        Random rand = new Random();
        String sender;
        String recipient;
        String amount;
        Boolean result = null;
        String senderPublicKey = "";
        String senderPrivateKey = "";
        String recipientPublicKey = "";
        for(int i = 0; i < num; i++) {
            sender = "1";//String.valueOf(rand.nextInt(10)  + 1);
            recipient = "2";String.valueOf(rand.nextInt(10) + 1) ;
            amount =  "1";//String.valueOf(rand.nextInt(5) + 1);

            senderPublicKey = Driver.getKeyAsString("keys/public_key" + sender + ".der");
            senderPrivateKey = Driver.getKeyAsString("keys/private_key" + sender + ".der");
            recipientPublicKey = Driver.getKeyAsString("keys/public_key" + recipient + ".der");
           

            while (recipient.equals(sender)) {
                recipient = String.valueOf(rand.nextInt(10)) + 1;
            }

            result = source.makeTransaction(senderPublicKey, senderPrivateKey, recipientPublicKey, amount);
            

        }

        System.out.println(String.valueOf(result));

        System.out.println("took--------------" + (System.currentTimeMillis() - start));

    }
}