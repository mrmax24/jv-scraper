package scraper.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import scraper.app.service.PageScraper;
import scraper.app.service.ScraperService;
import scraper.app.service.ThreadPoolManager;

@RequiredArgsConstructor
public class ScraperServiceImpl implements ScraperService {
    private final PageScraper pageScraper;

    @Override
    public List<String> scrapeWithDates(String url, int pages,
                                        String fromDate, String toDate,
                                        ThreadPoolManager threadPoolManager) {
        return ScraperService.super.scrapeWithDates(
                url, pages, fromDate, toDate, threadPoolManager);
    }

    @Override
    public List<String> scrapeWithIssuedDate(String url, int pages, String issueDate,
                                             ThreadPoolManager threadPoolManager) {
        return ScraperService.super.scrapeWithIssuedDate(
                url, pages, issueDate, threadPoolManager);
    }

    @Override
    public List<Callable<Void>> getCallablesWithDates(
            String url, int pages, ConcurrentLinkedQueue<String> allProcessedPermits,
            String fromDate, String toDate
    ) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            tasks.add(() -> {
                List<String> result = pageScraper
                        .scrapeFirstPage(url, pageNumber, fromDate, toDate);
                System.out.println("Finished scrape for page (with dates): " + pageNumber
                        + ", found: " + result.size() + " records");
                allProcessedPermits.addAll(result);
                return null;
            });
        }
        return tasks;
    }

    @Override
    public List<Callable<Void>> getCallablesWithIssuedDate(
            String url, int pages, String issuedDate,
            ConcurrentLinkedQueue<String> allProcessedPermits
    ) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            tasks.add(() -> {
                List<String> result = pageScraper
                        .scrapeSecondPage(url, pageNumber, issuedDate);
                System.out.println("Finished scrape for page (without dates): "
                        + pageNumber + ", found: " + result.size() + " records");
                allProcessedPermits.addAll(result);
                return null;
            });
        }
        return tasks;
    }
}
