package scraper.app.service;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.storage.DataStorage;

@RequiredArgsConstructor
public class ScraperService {
    public static final String OVERLAY = "overlay";
    private static final String RECORD_PATH = "//div[contains(@id, 'entityRecordDiv')]";
    private static final String PERMIT_DETAILS_LINK = ".//a[contains(@href, '#/permit/')]";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private final WebDriverProvider webDriverProvider;
    private final RecordNavigator recordNavigator;
    private final DataExtractor dataExtractor;
    private final DataStorage dataStorage;

    public void scrape(String url, int pages) {
        WebDriver driver = webDriverProvider.setupWebDriver();

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY)));
            recordNavigator.clickSearchButton(wait);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(RECORD_PATH)));

            int pageCount = 0;
            boolean hasNextPage = true;

            while (hasNextPage && pageCount < pages) {
                List<WebElement> records = driver.findElements(By.xpath(RECORD_PATH));

                for (WebElement record : records) {
                    try {
                        WebElement link = record.findElement(By.xpath(PERMIT_DETAILS_LINK));
                        String result = dataExtractor.extractRecordData(record, driver, link);
                        dataStorage.addData(result);
                    } catch (Exception e) {
                        System.out.println("Error extracting record details: " + e.getMessage());
                    }
                }
                hasNextPage = recordNavigator.navigateToNextPage(wait, pageCount);
                pageCount++;
            }
        } finally {
            driver.quit();
        }
    }
}
