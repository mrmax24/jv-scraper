package scraper.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import scraper.app.config.ThreadPoolManager;
import scraper.app.model.FilterDate;

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

    List<Callable<Void>> getCallables(String url, int pages, FilterDate filterDate,
                                      ConcurrentLinkedQueue<String> allProcessedPermits);
}
