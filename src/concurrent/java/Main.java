import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Main {
    static Semaphore mutex = new Semaphore(1);
    static Semaphore items = new Semaphore(0);
    static Semaphore spaces;

    public static void main(String[] args) throws InterruptedException {

        if (args.length != 5) {
            System.out.println("Use: java Main <num_producers> <max_items_per_producer> <producing_time> <num_consumers> <consuming_time>");
            return;
        }
        
        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numConsumers = Integer.parseInt(args[3]);
        int consumingTime = Integer.parseInt(args[4]);

        Buffer buffer = new Buffer(mutex);
        spaces = new Semaphore(100);

        ArrayList<Thread> threadsP =  new ArrayList<Thread>();
        for (int i = 1; i <= numProducers - 1; i++) {
            Producer producer = new Producer(i, buffer, maxItemsPerProducer, producingTime, items, spaces);
        
            Thread myThread = new Thread(producer, ("myThread-Producer " + i));
            threadsP.add(myThread);
            myThread.start();
        }

        ArrayList<Thread> threadsC =  new ArrayList<Thread>();
        for (int i = 1; i <= numConsumers; i++) {
            Consumer consumer = new Consumer(i, buffer, consumingTime, items, spaces);
            
            Thread myThread = new Thread(consumer, ("myThread-Consumer " + i));
            threadsC.add(myThread);
            myThread.start();
        }

        for (Thread myThread : threadsP){
            myThread.join();
        }

        for (Thread myThread : threadsC){
            myThread.join();
        }

    }
}
