package scraper.app.storage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataStorage {
    public void saveToCsv(List<String> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String row : data) {
                writer.write(row + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}