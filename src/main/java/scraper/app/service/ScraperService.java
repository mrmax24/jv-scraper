package scraper.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScraperService {
    private final PageScraper pageScraper;
    private ThreadPoolManager threadPoolManager;

    public ScraperService(PageScraper pageScraper, int threadPoolSize) {
        this.pageScraper = pageScraper;
        this.threadPoolManager = new ThreadPoolManager(threadPoolSize);
    }

    public List<String> scrape(String url, int pages,
                               String fromDate, String toDate) {
        ConcurrentLinkedQueue<String> allProcessedPermits = new ConcurrentLinkedQueue<>();
        List<Callable<Void>> tasks = getCallables(url, pages, allProcessedPermits,
                fromDate, toDate);

        threadPoolManager.submitTasks(tasks);
        threadPoolManager.shutdown();

        System.out.println("Total records found: " + allProcessedPermits.size());
        return new ArrayList<>(allProcessedPermits);
    }

    private List<Callable<Void>> getCallables(
            String url, int pages, ConcurrentLinkedQueue<String> allProcessedPermits,
            String fromDate, String toDate) {
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            tasks.add(() -> {
                List<String> result = pageScraper.scrapePage(url, pageNumber, fromDate, toDate);
                System.out.println("Finished scrape for page: " + pageNumber
                        + ", found: " + result.size() + " records");
                allProcessedPermits.addAll(result);
                return null;
            });
        }
        return tasks;
    }
}
