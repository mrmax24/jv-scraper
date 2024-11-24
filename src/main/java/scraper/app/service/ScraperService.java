package scraper.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface ScraperService {

    default List<String> scrapeWithDates(String url, int pages, String fromDate,
                                         String toDate, ThreadPoolManager threadPoolManager) {
        return scrapeInternal(threadPoolManager, allProcessedPermits
                -> getCallablesWithDates(url, pages, allProcessedPermits, fromDate, toDate)
        );
    }

    default List<String> scrapeWithIssuedDate(String url, int pages, String issueDate,
                                            ThreadPoolManager threadPoolManager) {
        return scrapeInternal(threadPoolManager, allProcessedPermits
                        -> getCallablesWithIssuedDate(url, pages, issueDate, allProcessedPermits)
        );
    }

    private List<String> scrapeInternal(
            ThreadPoolManager threadPoolManager,
            TaskSupplier taskSupplier
    ) {
        ConcurrentLinkedQueue<String> allProcessedPermits = new ConcurrentLinkedQueue<>();
        List<Callable<Void>> tasks = taskSupplier.getTasks(allProcessedPermits);
        threadPoolManager.submitTasks(tasks);
        threadPoolManager.shutdown();
        System.out.println("Total records found: " + allProcessedPermits.size());
        return new ArrayList<>(allProcessedPermits);
    }

    List<Callable<Void>> getCallablesWithDates(
            String url, int pages, ConcurrentLinkedQueue<String> allProcessedPermits,
            String fromDate, String toDate
    );

    List<Callable<Void>> getCallablesWithIssuedDate(String url, int pages, String issuedDate,
            ConcurrentLinkedQueue<String> allProcessedPermits
    );
}
