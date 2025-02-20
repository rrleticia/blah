import java.util.concurrent.Semaphore;

class Consumer  implements Runnable  {
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private Semaphore items;
    private Semaphore spaces;
    
    public Consumer(int id, Buffer buffer, int sleepTime, Semaphore items, Semaphore spaces) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.items = items;
        this.spaces = spaces;
    }
    
    @Override
    public void run() { 
        while (true) {
            try {
                items.acquire();
                int item = buffer.remove();
                if (item == -1) break;
                System.out.println("Consumer " + id + " consumed item " + item);
                spaces.release();
                Thread.sleep(sleepTime);
            }  catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
   
}