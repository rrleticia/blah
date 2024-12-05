import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageMeanFilter {

    static BufferedImage originalImage;
    static BufferedImage filteredImage;
    static int kernelSize = 7;
    static int numThreads;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ImageMeanFilter <input_file> <num_threads>");
            System.exit(1);
        }

        String inputFile = args[0];
        numThreads = Integer.parseInt(args[1]);

        try {
            originalImage = ImageIO.read(new File(inputFile));
            filteredImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            int height = originalImage.getHeight();

            Thread[] workers = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                workers[i] = new Thread(new RowProcessor(i));
                workers[i].start();
            }

            for (Thread worker : workers) {
                worker.join();
            }

            ImageIO.write(filteredImage, "jpg", new File("filtered_output.jpg"));
            System.out.println("Image successfully processed and saved as filtered_output.jpg");

        } catch (IOException | InterruptedException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    static class RowProcessor implements Runnable {
        private int threadId;

        public RowProcessor(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public void run() {
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            for (int y = threadId; y < height; y += numThreads) {
                for (int x = 0; x < width; x++) {
                    int[] avgColor = calculateNeighborhoodAverage(originalImage, x, y, kernelSize);

                    synchronized (filteredImage) { // Ensure thread safety
                        filteredImage.setRGB(x, y, (avgColor[0] << 16) | (avgColor[1] << 8) | avgColor[2]);
                    }
                }
            }
        }
    }

    private static int[] calculateNeighborhoodAverage(BufferedImage image, int centerX, int centerY, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int pad = kernelSize / 2;

        long redSum = 0, greenSum = 0, blueSum = 0;
        int pixelCount = 0;

        for (int dy = -pad; dy <= pad; dy++) {
            for (int dx = -pad; dx <= pad; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;

                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int rgb = image.getRGB(x, y);
                    redSum += (rgb >> 16) & 0xFF;
                    greenSum += (rgb >> 8) & 0xFF;
                    blueSum += rgb & 0xFF;
                    pixelCount++;
                }
            }
        }

        return new int[]{
            (int) (redSum / pixelCount),
            (int) (greenSum / pixelCount),
            (int) (blueSum / pixelCount)
        };
    }
}
