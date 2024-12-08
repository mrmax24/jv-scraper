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
        List<String> processedRecords = Collections.synchronizedList(new ArrayList<>());
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        // Відкриваємо всі посилання в нових вкладках
        for (WebElement record : records) {
            String tail = fetchLinksTailsFromRecords(record);
            if (tail != null) {
                String fullURL = PERMIT_DETAILS_LINK + tail;
                jsExecutor.executeScript("window.open('" + fullURL + "', '_blank');");
            }
        }

        // Отримуємо всі вкладки, включаючи нові
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());

        // Обробляємо кожну вкладку, крім головної
        for (int i = 1; i < tabs.size(); i++) { // Пропускаємо першу вкладку (головну)
            String tabHandle = tabs.get(i);
            driver.switchTo().window(tabHandle);

            try {
                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body"))); // Очікуємо завантаження сторінки

                // Скрапимо дані з поточної вкладки
                String data = dataExtractor.extractRecords(null, driver, null); // Передаємо null, якщо record не потрібний
                processedRecords.add(data);
            } catch (Exception e) {
                System.out.println("Error scraping tab: " + e.getMessage());
            } finally {
                jsExecutor.executeScript("window.close();"); // Закриваємо вкладку після обробки
            }
        }

        // Повертаємось на головну вкладку
        driver.switchTo().window(tabs.get(0));
        return processedRecords;
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