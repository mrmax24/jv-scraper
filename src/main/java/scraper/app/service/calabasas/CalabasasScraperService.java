package scraper.app.service.calabasas;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import scraper.app.config.ThreadPoolManager;
import scraper.app.model.FilterDate;
import scraper.app.service.ScraperService;
import scraper.app.storage.DataStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class CalabasasScraperService implements ScraperService {
    private final CalabasasPageScraper pageScraper;
    private final CalabasasPageNavigator pageRecordNavigator;

    @Override
    public List<String> scrape(String url, int pages, FilterDate filterDate,
                               ThreadPoolManager threadPoolManager) {
        List<String> strings = ScraperService.super.scrape(url, pages, filterDate, threadPoolManager);
        new DataStorage().saveLogToCsv(
                "Total records found for the Calabasas city website: "
                        + strings.size());
        return strings;
    }

    @Override
    public List<Callable<Void>> getCallables(
            String url, int pages, FilterDate filterDate,
            ConcurrentLinkedQueue<String> allProcessedPermits) {

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            tasks.add(createTask(url, pageNumber, filterDate, allProcessedPermits));
        }
        return tasks;
    }

    private Callable<Void> createTask(String url, int pageNumber, FilterDate filterDate,
                                      ConcurrentLinkedQueue<String> allProcessedPermits) {
        return () -> {
            WebDriver driver = new ChromeDriver();
            try {
                driver.get(url);
                pageScraper.applyFilters(driver, filterDate);
                navigateToPage(driver, pageNumber);
                List<WebElement> records = pageScraper.fetchRecords(driver, pageNumber);

                if (records.isEmpty()) {
                    return null;
                }
                List<String> results = pageScraper.openLinksFromRecords(records, driver);
                allProcessedPermits.addAll(results);
                System.out.println("Successfully scraped page " + pageNumber);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error scraping page " + pageNumber + ": " + e.getMessage());
            } finally {
                driver.quit();
            }
            return null;
        };
    }

    private void navigateToPage(WebDriver driver, int pageNumber) {
        for (int j = 1; j < pageNumber; j++) {
            pageRecordNavigator.clickNextButton(driver, j);
        }
    }
}
