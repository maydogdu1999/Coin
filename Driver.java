public class Driver {

    public static void main(String[] args) {

        int port = 0;


        //some code to make usage a little prettier when debugging 
        if (args.length > 1) {
            System.out.println("Usage: Driver [port]");
            System.exit(1);
        } 

        try {
            port = Integer.parseInt(args[0]);
        }
        catch (Exception e) {

            System.out.println("Usage: Driver [port]");
            System.exit(1);

        }

        //new node object
        Node node1 = new Node();

        //start the server on the given port
        node1.startServer(port);

    }
}
