import java.net.*;
import java.io.*;
import java.time.*;
import java.util.concurrent.TimeUnit;


public class ChainTimer extends Thread{
    Node source;

    public ChainTimer(Node source) {
        this.source = source;
    }
    

    
    public void run(){

        while (true) {

            LocalDate now = LocalDate.now();
            LocalDateTime startOfDay;
            LocalDateTime latest;
        
            if (source.getStartOfDay() == null || source.getStartOfDay().isBefore(now.atStartOfDay()) ) {
                source.setStartOfDay(now.atStartOfDay());
            }

            startOfDay = source.getStartOfDay();

            latest = startOfDay.plusMinutes(3); // tolerance, you can adjust it for your needs
            
            //System.out.println("now: " + LocalDateTime.now() + " start: " + startOfDay + " end: " + latest);
            if (isBetween(LocalDateTime.now(), startOfDay, latest)) {
                System.out.println("will mine soon");
                try {
                    TimeUnit.SECONDS.sleep(30);
                }
                catch(Exception e) {
                    System.out.print(e);
                }

                source.mine();
                while(isBetween(LocalDateTime.now(), startOfDay, latest)) {
                    //waiting for grace period to end
                }
                source.setStartOfDay((LocalDateTime) null);;
            }

        }
        
    }

    public static boolean isBetween(LocalDateTime candidate, LocalDateTime start, LocalDateTime end) {
        return !candidate.isBefore(start) && !candidate.isAfter(end);  // Inclusive.
    }

}
