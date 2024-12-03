package scraper.app.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface DataExtractor {
    String extractRecords(WebElement record, WebDriver driver, WebElement link);
}
