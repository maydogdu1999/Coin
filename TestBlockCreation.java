public class TestBlockCreation {
    
static Node n;
    public static void main(String[] args) {
        
        int reps = Integer.parseInt(args[0]);

        int repsForConsistency = Integer.parseInt(args[1]);

        long total = 0;

        n = new Node();

        String pubkey1 = Driver.getKeyAsString("keys/public_key1.der");
        String prikey1 = Driver.getKeyAsString("keys/private_key1.der");
        String pubkey2 = Driver.getKeyAsString("keys/public_key2.der");
        String prikey2 = Driver.getKeyAsString("keys/private_key2.der");

        for(int i = 0; i < repsForConsistency; i++) {

            for (int j = 0; j < reps; j++) {

            n.makeTransaction(pubkey1, prikey1, pubkey2, "1");
            n.makeTransaction(pubkey2, prikey2, pubkey1, "1");

            }

        long start = System.currentTimeMillis();

        n.buildBlock();

        long end = System.currentTimeMillis();

        total += end - start;

        n.printBlockChain();

        System.out.println("Completed Iteration # " + i);

    }

        System.out.println("Took on average " + total / repsForConsistency + " milliseconds to construct a block containing " + reps * 2 + " transactions");


    }
}
