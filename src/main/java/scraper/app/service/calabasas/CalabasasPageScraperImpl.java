package scraper.app.service.calabasas;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.model.FilterDate;
import scraper.app.service.DataExtractor;
import scraper.app.service.PageScraper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class CalabasasPageScraperImpl implements PageScraper {
    private static final String PERMIT_DETAILS_LINK
            = "https://ci-calabasas-ca.smartgovcommunity.com"
            + "/PermittingPublic/PermitLandingPagePublic/Index/";
    private static final String SEARCH_ITEMS_TAG = "search-result-item";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final CalabasasPageRecordNavigator calabasasPageRecordNavigator;
    private final DataExtractor dataExtractor;

    @Override
    public List<String> scrapeResource(String url, int pageNumber, FilterDate filterDate) {
        List<String> results = new ArrayList<>();
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(url);
            applyFilters(driver, filterDate);
            List<WebElement> records = fetchRecords(driver, pageNumber);
            results = openLinksFromRecords(records, driver);
        } catch (Exception e) {
            System.out.println("Error scraping page " + pageNumber + ": " + e.getMessage());
        } finally {
            driver.quit();
        }
        return results;
    }

    private void applyFilters(WebDriver driver, FilterDate filterDate) {
        calabasasPageRecordNavigator.applyFiltration(driver, filterDate);
        calabasasPageRecordNavigator.clickSearchButton(driver);
    }

    private List<WebElement> fetchRecords(WebDriver driver, int pageNumber) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        List<WebElement> records = wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.className(SEARCH_ITEMS_TAG)));

        if (records.isEmpty()) {
            throw new IllegalStateException("No records found on page " + pageNumber);
        }
        System.out.println("Found " + records.size() + " records on page " + pageNumber);
        return records;
    }

    private List<String> openLinksFromRecords(List<WebElement> records, WebDriver driver) {
        ExecutorService executor = Executors.newFixedThreadPool(records.size());
        List<String> processedRecords = Collections.synchronizedList(new ArrayList<>());

        for (WebElement record : records) {
            executor.submit(() -> {
                String tail = fetchLinksTailsFromRecords(record);
                if (tail != null) {
                    String fullURL = PERMIT_DETAILS_LINK + tail;

                    synchronized (driver) {
                        ((JavascriptExecutor) driver).executeScript(
                                "window.open('" + fullURL + "', '_blank');");
                        System.out.println("Opening link for: " + fullURL);

                        switchToNewTab(driver);

                        String processedRecord = dataExtractor.extractRecords(record, driver, null);
                        processedRecords.add(processedRecord);

                        ((JavascriptExecutor) driver).executeScript("window.close();");
                        switchToMainTab(driver);

                        System.out.println("Finished extracting data for: " + fullURL);
                    }
                } else {
                    System.out.println("No link found for record.");
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        return processedRecords;
    }


    private void switchToNewTab(WebDriver driver) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));
    }


    private void switchToMainTab(WebDriver driver) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
    }

    private String fetchLinksTailsFromRecords(WebElement record) {
        WebElement linkElement = record.findElement(By.tagName("a"));
        String onclickValue = linkElement.getAttribute("onclick");

        if (onclickValue != null && onclickValue.contains("Detail/")) {
            String detailPart = onclickValue.split("Detail/")[1];
            detailPart = detailPart.split("'")[0];
            return detailPart;
        } else {
            System.out.println("Link was not found in record: " + record);
            return null;
        }
    }
}
