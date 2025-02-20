import java.util.concurrent.Semaphore;

class Producer implements Runnable  {
    private final Buffer buffer;
    private final int maxItems;
    private final int sleepTime;
    private final int id;
    private Semaphore items;
    private Semaphore spaces;

    
    public Producer(int id, Buffer buffer, int maxItems, int sleepTime, Semaphore items, Semaphore spaces) {
        this.id = id;
        this.buffer = buffer;
        this.maxItems = maxItems;
        this.sleepTime = sleepTime; 
        this.items = items;
        this.spaces = spaces;
    }
    
    @Override
    public void run() { 
        for (int i = 0; i < maxItems; i++) {
            try {
                Thread.sleep(sleepTime);    
                spaces.acquire();
                int item = (int) (Math.random() * 100);
                System.out.println("Producer " + id + " produced item " + item);
                buffer.put(item);
                items.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
