package scraper.app.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import scraper.app.model.FilterDate;

public interface RecordNavigator {
    void clickSearchButton(WebDriver driver);
    void applyFiltration(WebDriver driver, FilterDate filterDate);
    void findAndOpenLinkFromRecord(WebDriver driver, WebElement link);
}