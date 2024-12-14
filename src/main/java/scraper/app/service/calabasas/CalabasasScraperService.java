package scraper.app.service.calabasas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.config.ThreadPoolManager;
import scraper.app.model.FilterDate;
import scraper.app.service.ScraperService;
import scraper.app.util.WebDriverUtils;

@RequiredArgsConstructor
public class CalabasasScraperService implements ScraperService {
    private static final Logger log = LoggerFactory.getLogger(CalabasasScraperService.class);
    private final CalabasasPageScraper pageScraper;
    private final CalabasasPageNavigator pageRecordNavigator;

    @Override
    public List<String> scrape(String url, int pages, FilterDate filterDate,
                               ThreadPoolManager threadPoolManager) {
        List<String> strings = ScraperService.super
                .scrape(url, pages, filterDate, threadPoolManager);
        log.info("Total records fetched for the Calabasas city website: {}",
                strings.size());
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
                WebDriverUtils.waitForPageToLoad(driver, WebDriverUtils.TIMEOUT);
                List<WebElement> records = pageScraper.fetchRecords(driver, pageNumber);

                if (records.isEmpty()) {
                    log.info("No records were found on Calabasas website");
                }
                List<String> results = pageScraper
                        .openLinksFromRecords(records, driver, pageNumber);
                allProcessedPermits.addAll(results);
                log.info("Successfully scraped Calabasas page {}", pageNumber);
            } catch (Exception e) {
                log.error("Error scraping Calabasas page {}: {}", pageNumber, e.getMessage());
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
