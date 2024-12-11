package scraper.app.service.henderson;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.config.WebDriverProvider;
import scraper.app.model.FilterDate;
import scraper.app.service.DataExtractor;
import scraper.app.service.PageScraper;

@RequiredArgsConstructor
public class HendersonPageScraper implements PageScraper {
    public static final String OVERLAY = "overlay";
    private static final String RECORD_PATH = "//div[contains(@id, 'entityRecordDiv')]";
    private static final String PERMIT_DETAILS_LINK = ".//a[contains(@href, '#/permit/')]";
    private static final String QUERY_PARAMS = "?m=1&fm=1&ps=10&pn=";
    private static final int ATTEMPTS_NUMBER = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final WebDriverProvider webDriverProvider;
    private final HendersonPageNavigator hendersonPageNavigator;
    private final DataExtractor dataExtractor;

    @Override
    public List<String> scrapeResource(
            String url, int pageNumber, FilterDate filterDate) {
        List<String> processedPermits = new ArrayList<>();
        WebDriver driver = new ChromeDriver();
        int attempts = ATTEMPTS_NUMBER;

        while (attempts > 0) {
            try {
                driver = setupDriver(url, pageNumber);
                applyFilters(driver, filterDate);
                List<WebElement> records = fetchRecords(driver, pageNumber);
                processRecords(driver, records, processedPermits, pageNumber);
                break;
            } catch (Exception e) {
                System.out.println("Error scraping page " + pageNumber + ": " + e.getMessage());
                attempts--;
            } finally {
                driver.quit();
            }
        }
        return processedPermits;
    }

    private WebDriver setupDriver(String url, int pageNumber) {
        WebDriver driver = webDriverProvider.setupWebDriver();
        String pageUrl = url + QUERY_PARAMS + pageNumber;
        driver.get(pageUrl);

        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY)));
        return driver;
    }

    private void applyFilters(WebDriver driver, FilterDate filterDate) {
        hendersonPageNavigator.applyFiltration(driver, filterDate);
        hendersonPageNavigator.clickSearchButton(driver);
    }

    private List<WebElement> fetchRecords(WebDriver driver, int pageNumber) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(RECORD_PATH)));

        List<WebElement> records = driver.findElements(By.xpath(RECORD_PATH));
        if (records.isEmpty()) {
            throw new IllegalStateException("No records found on page " + pageNumber);
        }
        return records;
    }

    private void processRecords(WebDriver driver, List<WebElement> records,
                                List<String> processedPermits, int pageNumber) {
        String originalWindow = driver.getWindowHandle();

        for (WebElement record : records) {
            try {
                WebElement link = getPermitLink(record);
                if (link != null) {
                    String result = dataExtractor.extractRecords(record, driver, link);
                    processedPermits.add(result);
                } else {
                    System.out.println("Permit link not found in record on page " + pageNumber);
                }
            } catch (Exception e) {
                System.out.println("Error extracting record on page "
                        + pageNumber + ": " + e.getMessage());
            } finally {
                closeAdditionalTabs(driver, originalWindow);
            }
        }
    }

    private WebElement getPermitLink(WebElement record) {
        List<WebElement> links = record.findElements(By.xpath(PERMIT_DETAILS_LINK));
        return links.isEmpty() ? null : links.get(0);
    }

    private void closeAdditionalTabs(WebDriver driver, String originalWindow) {
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle).close();
            }
        }
        driver.switchTo().window(originalWindow);
    }
}
