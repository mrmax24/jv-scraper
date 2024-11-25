package scraper.app.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface DataExtractor {
    String extractRecordsForFirstPage(WebElement record, WebDriver driver, WebElement link);

    String extractRecordsForSecondPage(WebElement record, WebDriver driver, WebElement link);
}
