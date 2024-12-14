package scraper.app.service.calabasas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.model.FilterDate;
import scraper.app.service.DataExtractor;
import scraper.app.util.WebDriverUtils;

@RequiredArgsConstructor
public class CalabasasPageScraper {
    private static final Logger log = LoggerFactory.getLogger(CalabasasPageScraper.class);

    private static final String PERMIT_DETAILS_LINK = "https://ci-calabasas-ca.smartgovcommunity.com"
            + "/PermittingPublic/PermitLandingPagePublic/Index/";
    private static final String SEARCH_ITEMS_TAG = "search-result-item";
    private static final String LINK_TAIL = "Detail/";
    private final CalabasasPageNavigator calabasasPageNavigator;
    private final DataExtractor dataExtractor;

    void applyFilters(WebDriver driver, FilterDate filterDate) {
        log.info("Applying filters on Calabasas website with date: {}", filterDate.getIssueDate());
        try {
            calabasasPageNavigator.applyFiltration(driver, filterDate);
            calabasasPageNavigator.clickSearchButton(driver);
            log.info("Calabasas filters applied successfully");
        } catch (Exception e) {
            log.error("Error while applying filters on Calabasas website: {}", e.getMessage(), e);
        }
    }

    List<WebElement> fetchRecords(WebDriver driver, int pageNumber) {
        log.info("Fetching records from Calabasas page {}", pageNumber);
        try {
            WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);
            List<WebElement> records = wait.until(ExpectedConditions
                    .presenceOfAllElementsLocatedBy(By.className(SEARCH_ITEMS_TAG)));
            log.info("Found {} records on Calabasas page {}", records.size(), pageNumber);
            return records;
        } catch (Exception e) {
            log.error("Failed to fetch records on Calabasas page {}: {}",
                    pageNumber, e.getMessage(), e);
            return null;
        }
    }

    List<String> openLinksFromRecords(List<WebElement> records, WebDriver driver, int pageNumber) {
        log.info("Opening links from Calabasas records");
        List<String> processedRecords = Collections.synchronizedList(new ArrayList<>());
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        for (WebElement record : records) {
            String tail = fetchLinksTailsFromRecords(record);
            if (tail != null) {
                String fullUrl = PERMIT_DETAILS_LINK + tail;
                jsExecutor.executeScript("window.open('" + fullUrl + "', '_blank');");
            }
        }

        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        log.info("Scraping data from tabs");
        for (int i = 1; i < tabs.size(); i++) {
            String tabHandle = tabs.get(i);
            driver.switchTo().window(tabHandle);

            try {
                WebDriverUtils.waitForPageToLoad(driver, WebDriverUtils.TIMEOUT);

                String data = dataExtractor.extractRecords(null, driver, null);
                processedRecords.add(data);
            } catch (Exception e) {
                log.error("Error scraping tab: {}", e.getMessage(), e);
            } finally {
                jsExecutor.executeScript("window.close();");
            }
        }
        driver.switchTo().window(tabs.get(0));
        log.info("Opened {} links from page {}", processedRecords.size(), pageNumber);
        return processedRecords;
    }

    private String fetchLinksTailsFromRecords(WebElement record) {
        try {
            if (!record.isDisplayed() || !record.isEnabled()) {
                log.warn("Record element is not valid: {}", record);
                return null;
            }

            WebElement linkElement = record.findElement(By.tagName("a"));
            String onclickValue = linkElement.getAttribute("onclick");

            if (onclickValue != null && onclickValue.contains(LINK_TAIL)) {
                String detailPart = onclickValue.split(LINK_TAIL)[1];
                detailPart = detailPart.split("'")[0];
                return detailPart;
            } else {
                log.warn("Link was not found in record: {}", record);
                return null;
            }
        } catch (StaleElementReferenceException e) {
            log.error("Stale element reference error while fetching link tail: {}",
                    e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Error while fetching link tail: {}", e.getMessage(), e);
            return null;
        }
    }
}
