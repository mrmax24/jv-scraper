package scraper.app.storage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataStorage {
    public void saveToCsv(List<String> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String row : data) {
                writer.write(row + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write records to file: " + filePath);
        }
    }
}
