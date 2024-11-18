package scraper.app.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class ScraperService {
    private static final int THREAD_POOL_SIZE = 5;
    public static final String OVERLAY = "overlay";
    private static final String RECORD_PATH = "//div[contains(@id, 'entityRecordDiv')]";
    private static final String PERMIT_DETAILS_LINK = ".//a[contains(@href, '#/permit/')]";
    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private final WebDriverProvider webDriverProvider;
    private final RecordNavigator recordNavigator;
    private final DataExtractor dataExtractor;

    public List<String> scrape(String url, int pages) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<List<String>>> futures = new ArrayList<>();
        ConcurrentLinkedQueue<String> allProcessedPermits = new ConcurrentLinkedQueue<>();

        try {
            for (int i = 0; i < pages; i++) {
                int pageNumber = i + 1;
                Callable<List<String>> task = () -> {
                    List<String> result = scrapePage(url, pageNumber);
                    System.out.println("Finished scrape for page: " + pageNumber
                            + ", found: " + result.size() + " records");
                    allProcessedPermits.addAll(result);
                    return result;
                };
                futures.add(executorService.submit(task));
            }

            for (Future<List<String>> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error during thread execution: " + e.getMessage());
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.out.println("Forcing shutdown...");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Total records found: " + allProcessedPermits.size());
        return new ArrayList<>(allProcessedPermits);
    }

    private List<String> scrapePage(String url, int pageNumber) {
        WebDriver driver = null;
        List<String> processedPermits = new ArrayList<>();
        int attempts = 3;

        while (attempts > 0) {
            try {
                driver = webDriverProvider.setupWebDriver();
                String pageUrl = url + "?m=1&fm=1&ps=10&pn=" + pageNumber + "&em=true";
                driver.get(pageUrl);

                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY)));
                recordNavigator.clickSearchButton(wait);

                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(RECORD_PATH)));

                List<WebElement> records = driver.findElements(By.xpath(RECORD_PATH));
                if (records.isEmpty()) {
                    System.out.println("No records found on page " + pageNumber);
                    driver.navigate().refresh();
                    attempts--;
                    continue;
                }

                String originalWindow = driver.getWindowHandle();

                for (WebElement record : records) {
                    try {
                        if (!record.findElements(By.xpath(PERMIT_DETAILS_LINK)).isEmpty()) {
                            WebElement link = record.findElement(By.xpath(PERMIT_DETAILS_LINK));
                            String result = dataExtractor.extractRecordData(record, driver, link);
                            processedPermits.add(result);
                        } else {
                            System.out.println("Permit link not found in record on page " + pageNumber);
                        }
                    } catch (Exception e) {
                        System.out.println("Error extracting record on page " + pageNumber
                                + ": " + e.getMessage());
                    } finally {
                        for (String handle : driver.getWindowHandles()) {
                            if (!handle.equals(originalWindow)) {
                                driver.switchTo().window(handle).close();
                            }
                        }
                        driver.switchTo().window(originalWindow);
                    }
                }
                break;
            } catch (Exception e) {
                System.out.println("Error scraping page " + pageNumber + ": " + e.getMessage());
                attempts--;
            } finally {
                if (driver != null) {
                    driver.quit();
                }
            }
        }
        return processedPermits;
    }
}
