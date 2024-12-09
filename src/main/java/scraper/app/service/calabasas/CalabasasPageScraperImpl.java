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

@RequiredArgsConstructor
public class CalabasasPageScraperImpl implements PageScraper {
    private static final String PERMIT_DETAILS_LINK
            = "https://ci-calabasas-ca.smartgovcommunity.com"
            + "/PermittingPublic/PermitLandingPagePublic/Index/";
    private static final String SEARCH_ITEMS_TAG = "search-result-item";
    private static final String LINK_TAIL = "Detail/";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final CalabasasPageRecordNavigator calabasasPageRecordNavigator;
    private final DataExtractor dataExtractor;

    @Override
    public List<String> scrapeResource(String url, int pageNumber, FilterDate filterDate) {
        List<String> allResults = new ArrayList<>();
        WebDriver driver = new ChromeDriver();
        int currentPage = 1;

        try {
            driver.get(url);
            applyFilters(driver, filterDate);
            while (currentPage <= pageNumber) {
                List<WebElement> records = fetchRecords(driver, currentPage);
                List<String> results = openLinksFromRecords(records, driver);
                allResults.addAll(results);
                if (currentPage < pageNumber) {
                    calabasasPageRecordNavigator.clickNextButton(driver);
                }
                currentPage++;
            }
        } catch (Exception e) {
            System.out.println("Error scraping page " + currentPage + ": "
                    + e.getMessage());
        } finally {
            driver.quit();
        }
        return allResults;
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
        List<String> processedRecords = Collections.synchronizedList(new ArrayList<>());
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        for (WebElement record : records) {
            String tail = fetchLinksTailsFromRecords(record);
            if (tail != null) {
                String fullURL = PERMIT_DETAILS_LINK + tail;
                jsExecutor.executeScript("window.open('" + fullURL + "', '_blank');");
            }
        }
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());

        for (int i = 1; i < tabs.size(); i++) {
            String tabHandle = tabs.get(i);
            driver.switchTo().window(tabHandle);

            try {
                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

                String data = dataExtractor.extractRecords(null, driver, null);
                processedRecords.add(data);
            } catch (Exception e) {
                System.out.println("Error scraping tab: " + e.getMessage());
            } finally {
                jsExecutor.executeScript("window.close();");
            }
        }
        driver.switchTo().window(tabs.get(0));
        return processedRecords;
    }


    private String fetchLinksTailsFromRecords(WebElement record) {
        WebElement linkElement = record.findElement(By.tagName("a"));
        String onclickValue = linkElement.getAttribute("onclick");

        if (onclickValue != null && onclickValue.contains(LINK_TAIL)) {
            String detailPart = onclickValue.split(LINK_TAIL)[1];
            detailPart = detailPart.split("'")[0];
            return detailPart;
        } else {
            System.out.println("Link was not found in record: " + record);
            return null;
        }
    }
}