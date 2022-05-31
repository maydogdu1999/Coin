public class TestHash {
 
static Node n;
    

public static void main(String[] args) {

    long start;
    long end;
    long time;
    long total = 0;

    Node n = new Node();

    for (int i = 0; i < 100; i++) { 
        n.setDifficulty(Integer.parseInt(args[0]));

        System.out.println("Difficulty: " + n.getMineDifficulty());

        Miner miner = new Miner(n);

        start = System.nanoTime();

        miner.start();
        
        try {

            miner.join();

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

        System.out.println("hash # " + i + " mined!");

        end = System.nanoTime();

        time = (end - start) / 1000000;
        total += time;

    }

    System.out.println("average time taken to mine at difficulty: " + n.getMineDifficulty() + " : "  + total / 100);


    
}

}


