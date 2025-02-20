import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Buffer {
    private Semaphore mutex;
    private final List<Integer> data = new ArrayList<>();

    public Buffer(Semaphore mutex){
        this.mutex = mutex;
    }
    
    public void put(int value) throws InterruptedException {
        mutex.acquire();
        if(data.size() < 100){ 
            data.add(value);
            System.out.println("Inserted: " + value + " | Buffer size: " + data.size());
            
        }
        mutex.release();
    }
    
    public int remove() throws InterruptedException {
        mutex.acquire();
        if (!data.isEmpty()) {
            int value = data.remove(0);
            System.out.println("Removed: " + value + " | Buffer size: " + data.size());
            mutex.release();
            return value;
        }
        mutex.release();
        return -1;
    }
}
