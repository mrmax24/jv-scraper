package scraper.app.service.henderson;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.config.WebDriverProvider;
import scraper.app.model.FilterDate;
import scraper.app.service.DataExtractor;
import scraper.app.service.PageScraper;
import scraper.app.service.calabasas.CalabasasScraperService;
import scraper.app.util.WebDriverUtils;

@RequiredArgsConstructor
public class HendersonPageScraper implements PageScraper {
    private static final Logger log =
            LoggerFactory.getLogger(CalabasasScraperService.class);
    private static final String OVERLAY = "overlay";
    private static final String RECORD_PATH = "//div[contains(@id, 'entityRecordDiv')]";
    private static final String PERMIT_DETAILS_LINK = ".//a[contains(@href, '#/permit/')]";
    private static final String QUERY_PARAMS = "?m=1&fm=1&ps=10&pn=";
    private static final int ATTEMPTS_NUMBER = 3;
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
                log.info("Error scraping Henderson page {}: {}", pageNumber, e.getMessage());
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

        WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY)));
        return driver;
    }

    private void applyFilters(WebDriver driver, FilterDate filterDate) {
        log.info("Applying filters on Henderson website with date: {}",
                filterDate.getIssueDate());
        try {
            hendersonPageNavigator.applyFiltration(driver, filterDate);
            hendersonPageNavigator.clickSearchButton(driver);
            log.info("Henderson filters applied successfully");
        } catch (Exception e) {
            log.error("Error while applying filters on Henderson website: {}",
                    e.getMessage(), e);
        }
    }

    private List<WebElement> fetchRecords(WebDriver driver, int pageNumber) {
        log.info("Fetching records from Henderson  page {}", pageNumber);
        try {
            WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(RECORD_PATH)));

            List<WebElement> records = driver.findElements(By.xpath(RECORD_PATH));
            log.info("Fetched {} records from Henderson page {}", records.size(), pageNumber);
            return records;
        } catch (Exception e) {
            log.error("Failed to fetch records on Henderson page {}: {}",
                    pageNumber, e.getMessage(), e);
        }
        return null;
    }

    private void processRecords(WebDriver driver, List<WebElement> records,
                                List<String> processedPermits, int pageNumber) {
        log.error("Start scraping Henderson website records");
        String originalWindow = driver.getWindowHandle();
        for (WebElement record : records) {
            try {
                WebElement link = getPermitLink(record);
                if (link != null) {
                    String result = dataExtractor.extractRecords(record, driver, link);
                    processedPermits.add(result);
                } else {
                    log.info("Permit link not found in record on Henderson page {}", pageNumber);
                }
            } catch (Exception e) {
                log.info("Error extracting record on Henderson page {}: {}",
                        pageNumber, e.getMessage());
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
