package scraper.app.service.calabasas;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class CalabasasPageScraperImpl implements PageScraper {
    public static final String OVERLAY = "overlay";
    private static final String PERMIT_DETAILS_LINK
            = ".//a[contains(@onclick, 'FormSupport.submitAction')]";
    private static final String SEARCH_ITEMS_TAG = "search-result-item";
    private static final String QUERY_PARAMS = "/Index/";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final WebDriverProvider webDriverProvider;
    private final CalabasasPageRecordNavigator calabasasPageRecordNavigator;
    private final DataExtractor dataExtractor;

    @Override
    public List<String> scrapeResource(String url, int pageNumber, FilterDate filterDate) {
        List<String> processedPermits = new ArrayList<>();
        WebDriver driver = new ChromeDriver();
        try {
            driver = webDriverProvider.setupWebDriver();
            driver.get(url);
            applyFilters(driver, filterDate);
            List<WebElement> records = fetchRecords(driver, pageNumber);
            processRecords(driver, records, processedPermits, pageNumber);
        } catch (Exception e) {
            System.out.println("Error scraping page "
                    + pageNumber + ": " + e.getMessage());
        } finally {
            driver.quit();
        }
        return processedPermits;
    }

    private void applyFilters(WebDriver driver, FilterDate filterDate) {
        calabasasPageRecordNavigator.applyFiltration(driver, filterDate);
        calabasasPageRecordNavigator.clickSearchButton(driver);
    }

    private List<WebElement> fetchRecords(WebDriver driver, int pageNumber) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        List<WebElement> records = wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.className(SEARCH_ITEMS_TAG))
        );
        if (records.isEmpty()) {
            throw new IllegalStateException("No records found on page " + pageNumber);
        }
        return records;
    }

    private void processRecords(WebDriver driver, List<WebElement> records,
                                List<String> processedPermits, int pageNumber) {
        String originalWindow = driver.getWindowHandle();
        System.out.println("Original window handle: " + originalWindow);
        System.out.println("Number of records to process: " + records.size());

        try {
            for (int i = 0; i < 1; i++) {
                System.out.println("Processing record " + (i + 1) + " of " + records.size());
                try {
                    List<WebElement> updatedRecords = fetchRecords(driver, pageNumber);
                    WebElement record = updatedRecords.get(i);

                    WebElement link = getPermitLink(record, driver);
                    if (link != null) {
                        System.out.println("Clicking on permit link for record " + (i + 1));
                        link.click();
                        System.out.println("Switching to new tab...");
                        switchToNewTab(driver);

                        System.out.println("Extracting data for record " + (i + 1));
                        String result = dataExtractor.extractRecords(record, driver, link);
                        processedPermits.add(result);
                        System.out.println("Data extracted successfully for record " + (i + 1));

                        closeAdditionalTabs(driver, originalWindow);
                        System.out.println("Returned to original tab for record " + (i + 1));
                    } else {
                        System.out.println("Permit link not found in record " + (i + 1));
                    }
                } catch (Exception e) {
                    System.err.println("Error extracting record on page " + pageNumber +
                            ", record " + (i + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            System.out.println("Closing driver...");
            driver.quit();
        }
    }

    private void switchToNewTab(WebDriver driver) {
        String originalHandle = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                break;
            }
        }
    }


    private void closeAdditionalTabs(WebDriver driver, String originalWindow) {
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle).close();
            }
        }
        driver.switchTo().window(originalWindow);
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }


    private WebDriver setupDriver(String url, int pageNumber) {
        WebDriver driver = webDriverProvider.setupWebDriver();
        String pageUrl = url + QUERY_PARAMS + pageNumber;
        driver.get(pageUrl);

        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY)));
        return driver;
    }

    private WebElement getPermitLink(WebElement record, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.xpath(PERMIT_DETAILS_LINK)));

        List<WebElement> links = record.findElements(By.xpath(PERMIT_DETAILS_LINK));

        if (links.isEmpty()) {
            return null;
        }
        return links.get(0);
    }

    private String extractPermitDetailsLink(WebElement record, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(PERMIT_DETAILS_LINK)));

        List<WebElement> links = record.findElements(By.xpath(PERMIT_DETAILS_LINK));

        if (links.isEmpty()) {
            return null;
        }

        WebElement link = links.get(0);

        String onclickValue = link.getAttribute("onclick");

        if (onclickValue != null && !onclickValue.isEmpty()) {
            String detailPath = onclickValue.replaceAll(".*'(Detail/.*?)'.*", "$1");

            System.out.println("Extracted Detail Path: " + detailPath);
        }
        return onclickValue;
    }

    private void refreshPageAndWait(WebDriver driver) {
        driver.navigate().refresh();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SEARCH_ITEMS_TAG)));
    }
}
