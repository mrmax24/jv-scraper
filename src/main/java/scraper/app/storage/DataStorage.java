package scraper.app.storage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataStorage implements Runnable {
    private static final String POISON_PILL = "STOP_WRITING";
    private final BlockingQueue<String> dataQueue = new LinkedBlockingQueue<>();
    private final String filePath;

    public DataStorage(String filePath) {
        this.filePath = filePath;
    }

    public void addData(String data) {
        try {
            dataQueue.put(data);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            while (true) {
                String data = dataQueue.take();
                if (data.equals(POISON_PILL)) {
                    break;
                }
                writer.write(data + "\n");
                writer.flush();
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public void finish() {
        try {
            dataQueue.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
