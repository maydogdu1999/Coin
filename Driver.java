import java.net.*;
import java.util.*;

import java.io.*;
import java.util.Scanner;

import java.util.concurrent.TimeUnit;


public class Driver {
    static Node node1 = null;

    public static void main(String[] args) {

        
        Scanner scanner = new Scanner(System.in);
        String inputLine = "";

        //some code to make usage a little prettier when debugging 
        if (args.length < 1) {
            System.out.println("Usage: Driver [host port]");
            System.exit(1);
        } 

        //new node object
        Node node1 = new Node();

        try {

            node1.setHostPort(Integer.parseInt(args[0]));
            
        }
        catch (Exception e) {

            System.out.println("Usage: Driver [host port]");
            System.exit(1);

        }
        
        try {
            //System.out.println(InetAddress.getLocalHost().toString().split("/")[1]);
            node1.setHostIp(getIp());
        }
        catch (Exception e) {
            System.out.println(e);
        }


        node1.start();
       


        FrontEndServer frontEnd1 = new FrontEndServer();
        frontEnd1.setSource(node1);
        frontEnd1.start();

        while(true) {

            try {

                while(!scanner.hasNextLine()) {
                    //need this while loop because we get errors when we run through ssh
                }
                inputLine = scanner.nextLine();
                
                System.out.println("received command: " + inputLine);
                String[] inputParsed = inputLine.split("--");                

                if (inputParsed[0].equals("joinNode")) {
                    if (node1.getConnections().size() > node1.getMaxNeighbors() ) {
                       System.out.println("Can't join: Max number of neighbors reached");
                    }
                    String ip = inputParsed[1];
                    int neighborPort = Integer.parseInt(inputParsed[2]);
                    Connection newCon = node1.connectToPeer(ip, neighborPort);
                    node1.populateNeighbors(ip, neighborPort, 0, newCon);
                }

                else if (inputParsed[0].equals("joinNodes")) {
                    //this command take one port number for all ip's

                    if (node1.getConnections().size() > node1.getMaxNeighbors() ) {
                        System.out.println("Can't join: Max number of neighbors reached");
                    }

                    String[] ipParsed = inputParsed[1].split(" ");
                    int neighborPort = Integer.parseInt(inputParsed[2]);
                    for (String ip: ipParsed) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } 
                        catch (Exception e) {
                            System.out.print(e);
                        }
                        Connection newCon = node1.connectToPeer(ip, neighborPort);
                        node1.populateNeighbors(ip, neighborPort, 0, newCon);
                    }
                    
                }

                else if (inputParsed[0].equals("usage")) {
                    printUsage();
                }

                else if (inputParsed[0].equals("printConnections")) {
                    System.out.println("numConnections: " + node1.getConnections().size() + " connections: " + node1.getConnections().values());
                }

                else if (inputParsed[0].equals("userBalance")) {
                    String publicKey = getKeyAsString(inputParsed[1]);
                    System.out.println("Balance: " + node1.getUserBalance(publicKey));
                }

                else if (inputParsed[0].equals("printTransactions")) {
                    ArrayList<Transaction> currentBlock = node1.getCurrentBlock();
                    int size = currentBlock.size();
                    if (size == 0) {
                        System.out.println("No transactions made today");
                    }
                    else {
                        System.out.println("# transactions made today: " + size + ", last transaction: " + currentBlock.get(size - 1).toString());

                    }
                }

                else if (inputParsed[0].equals("makeTransaction")) {
                    String senderPublicKey;
                    String senderPrivateKey;
                    String recipientPublicKey;
                    String amount;            
                    try {
                        if (node1.getConnections().size() > node1.getMaxNeighbors() ) {
                            System.out.println("Can't join: Max number of neighbors reached");
                        }
                        senderPublicKey = getKeyAsString(inputParsed[1]);
                        senderPrivateKey = getKeyAsString(inputParsed[2]);
                        recipientPublicKey = getKeyAsString(inputParsed[3]);
                        amount = inputParsed[4];
                        node1.makeTransaction(senderPublicKey, senderPrivateKey, recipientPublicKey, amount);
                        //System.out.println("result of makeTransaction: " + success);
                    }
                    catch (Exception e) {
                        System.out.println("couldn't do transaction: " + e);
                        e.printStackTrace();
                    }
                    
                }

                else if (inputParsed[0].equals("printBlockchain")) {
                    System.out.println(node1.printBlockChain());
                }

                else if (inputParsed[0].equals("startMiner")) {

                    Miner miner = new Miner(node1);
                    miner.start();
                }

                else if (inputParsed[0].equals("startMiners")) {

                    int num = Integer.parseInt(inputParsed[1]);
                    for (int i = 0; i < num; i++) {
                        Miner miner = new Miner(node1);
                        miner.start();
                    }
                    
                }

                else if (inputParsed[0].equals("setDifficulty")) {

                    node1.setDifficulty(Integer.parseInt(inputParsed[1]));

                }


                else if (inputParsed[0].equals("Test1")) {

                    long start1 = System.currentTimeMillis();
                    int num = Integer.parseInt(inputParsed[1]);
                    for (int i = 0; i < 5; i++) {
                        Test1 test = new Test1();
                        test.source = node1;
                        test.start = start1;
                        test.num = num;
                        test.start();
                    }


                }

                else {
                    System.out.println("No such command. Type 'usage' to get help.");
                }


                

            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);


            }
            
            
        }    

    }

    public static String getKeyAsString(String filename) {
        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);  
            byte[] keyBytes = new byte[(int) file.length()];
            dis.readFully(keyBytes);
            dis.close();
            String keyAsString = Base64.getEncoder().encodeToString(keyBytes);
            return keyAsString;
        }
        catch (Exception e) {
            return null;
        }
        
    }

    public static void printUsage() {
        InputStream input;
        try {
            input = new BufferedInputStream(new FileInputStream("usage.txt"));
            byte[] buffer = new byte[10000];
            for (int length = 0; (length = input.read(buffer)) != -1;) {
                System.out.write(buffer, 0, length);
            }
            input.close();
        } 
        catch (Exception e) {
            System.out.println(e);
        }
    
    }

    public static String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
}
