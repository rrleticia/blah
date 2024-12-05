import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;

public class ImageMeanFilter {

    static BufferedImage originalImage;

    static BufferedImage filteredImage;

    static int kernelSize = 7;

    static int numThreads;

    static Semaphore multiplex; 

    static Semaphore mutex = new Semaphore(1);

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: java ImageMeanFilter <input_file>");
            System.exit(1);
        }

        String inputFile = args[0];

        numThreads = Integer.parseInt(args[1]);

        multiplex = new Semaphore(numThreads);

        try {

            // Load image
            originalImage = ImageIO.read(new File(inputFile));
        
            // Create result image
            filteredImage = new BufferedImage(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                BufferedImage.TYPE_INT_RGB
            );

            // Image processing
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
                
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    
                    Thread myThread = new Thread(new NeighborhoodAverage(x, y), "myThread-NeighborhoodAverage");
                    myThread.start();
                    
                }
            }
            
            ImageIO.write(filteredImage, "jpg", new File("filtered_output.jpg"));
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    public static class NeighborhoodAverage implements Runnable {
        int x;
        int y;

        public NeighborhoodAverage(int x, int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            try {
                multiplex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Calculate neighborhood average
            int[] avgColor = calculateNeighborhoodAverage(originalImage, x, y, kernelSize);

            try {
                mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Set filtered pixel
            filteredImage.setRGB(x, y, 
                (avgColor[0] << 16) | 
                (avgColor[1] << 8)  | 
                avgColor[2]
            );
            
            mutex.release();
            multiplex.release();
           
        }
        
        private static int[] calculateNeighborhoodAverage(BufferedImage image, int centerX, int centerY, int kernelSize) {
            int width = image.getWidth();
            int height = image.getHeight();
            int pad = kernelSize / 2;
            
            // Arrays for color sums
            long redSum = 0, greenSum = 0, blueSum = 0;
            int pixelCount = 0;
            
            // Process neighborhood
            for (int dy = -pad; dy <= pad; dy++) {
                for (int dx = -pad; dx <= pad; dx++) {
                    int x = centerX + dx;
                    int y = centerY + dy;
                    
                    // Check image bounds
                    if (x >= 0 && x < width && y >= 0 && y < height) {
                        // Get pixel color
                        int rgb = image.getRGB(x, y);
                        
                        // Extract color components
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;
                        
                        // Sum colors
                        redSum += red;
                        greenSum += green;
                        blueSum += blue;
                        pixelCount++;

                        
                    }
                }
            }
            
            // Calculate average
            return new int[] {
                (int)(redSum / pixelCount),
                (int)(greenSum / pixelCount),
                (int)(blueSum / pixelCount)
            };
        }
        
    }
}


