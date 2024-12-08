package scraper.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import scraper.app.config.ThreadPoolManager;
import scraper.app.model.FilterDate;
import scraper.app.storage.DataStorage;

public interface ScraperService {

    default List<String> scrape(String url, int pages, FilterDate filterDate,
                                ThreadPoolManager threadPoolManager) {
        return scrapeInternal(threadPoolManager, allProcessedPermits
                -> getCallables(url, pages, filterDate, allProcessedPermits));
    }

    private List<String> scrapeInternal(ThreadPoolManager threadPoolManager,
                                        TaskSupplier taskSupplier) {
        ConcurrentLinkedQueue<String> allProcessedPermits = new ConcurrentLinkedQueue<>();
        List<Callable<Void>> tasks = taskSupplier.getTasks(allProcessedPermits);
        threadPoolManager.submitTasks(tasks);
        threadPoolManager.shutdown();
        return new ArrayList<>(allProcessedPermits);
    }

    default List<Callable<Void>> getCallables(String url, int pages, FilterDate filterDate,
                                              ConcurrentLinkedQueue<String> allProcessedPermits) {
        PageScraper pageScraper = (url1, pageNumber, filterDate1) -> List.of();
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            tasks.add(() -> {
                List<String> result = pageScraper
                        .scrapeResource(url, pageNumber, filterDate);

                new DataStorage().saveLogToCsv(
                        "Finished scrape for Calabasas' page #"
                                + pageNumber + ", found: " + result.size() + " records");

                allProcessedPermits.addAll(result);
                return null;
            });
        }
        return tasks;
    }
}
