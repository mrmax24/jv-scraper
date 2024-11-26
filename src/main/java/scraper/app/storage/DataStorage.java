package scraper.app.storage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataStorage {
    public static final String LOG_FILE_PATH = "src/main/resources/log_info.csv";

    public void saveToCsv(List<String> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String row : data) {
                writer.write(row + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write records to file: " + filePath);
        }
    }

    public void saveLogToCsv(String row) {
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
            writer.write(row + System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Couldn't write records to file: " + LOG_FILE_PATH, e);
        }
    }
}
