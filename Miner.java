import java.util.Random;

/**
 * The Miner class is what allows blocks to "mine." It starts a thread that iterates over hash values, setting the value of a character at a random index to a random value 
 * (within a given range of ASCII characters), looking for a hash that has at least "mining difficulty" number of leading zeros. 
 */
public class Miner extends Thread{
    
    //Node object representing the node that originates the Mining thread
    Node source;

    //int representing mining difficulty, retrieved from source node
    int difficulty;

    //String that has "difficulty" number of zeros, used to compare to hash
    String zeroString = "0";

    //String that is randomly altered and hashed to generate a hash String
    String unhashedString = "((((((((((((((((((((";

    //String that represented the hashed unhashed string
    String hashedString = "--------------------";

    /**
     * Constructor for the miner thread, takes source node as parameter
     * @param source Node representing originating node
     */
    public Miner(Node source) {
        
        this.source = source;
    }

    /**
     * The central mining process that the miner carries out, ceases when a hash value is discovered.
     */
    public void run() {

        System.out.println("Starting Mining... ");

        //set difficulty value
        difficulty = source.getMineDifficulty();

        //create zeroString according to difficulty
        for (int i = 1; i < difficulty; i++) {

            zeroString += "0";
        }

        Random rand = new Random();

        //while hashedString does not have requisite number of zeros, and source has not mined a block
        while(!hashValid(hashedString) && !source.getMined()) {

            //generate a random character
            char newChar = (char) (rand.nextInt(86) + 40);
   
            //generate a random index within range
            int changeIndex = rand.nextInt(20)+1;

            //generate the new hashed string, swapping in the new character at the index
            unhashedString = unhashedString.substring(0, changeIndex-1) + newChar + unhashedString.substring(changeIndex);

            //hash the new unhashedString
            hashedString = source.hashSHA256(unhashedString);

        }
        
        //if the while loop is exited must have found a hash value, set it in the source node
        source.setMineHash(hashedString);

        //initate the building of a block in the source node
        source.buildBlock();
    }


    /**
     * hashValid(String hash) check to see if a given hashed string has the requisite number of leading zeros.
     * @param hash String representing the hash value to be checked
     * @return T/F depending on whether it has enough leading zeros 
     */
    public Boolean hashValid(String hash) {

        return (hash.substring(0, difficulty).equals(zeroString));
    }

}
