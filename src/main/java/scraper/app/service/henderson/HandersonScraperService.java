package scraper.app.service.henderson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.config.ThreadPoolManager;
import scraper.app.model.FilterDate;
import scraper.app.service.PageScraper;
import scraper.app.service.ScraperService;
import scraper.app.service.calabasas.CalabasasScraperService;

@RequiredArgsConstructor
public class HandersonScraperService implements ScraperService {
    private static final Logger log =
            LoggerFactory.getLogger(CalabasasScraperService.class);
    private final PageScraper pageScraper;

    @Override
    public List<String> scrape(String url, int pages,
                                        FilterDate filterDate,
                                        ThreadPoolManager threadPoolManager) {
        List<String> strings = ScraperService.super.scrape(
                url, pages, filterDate, threadPoolManager);
        log.info("Total records found for the Henderson city website: {}",
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
            tasks.add(() -> {
                List<String> result = pageScraper
                        .scrapeResource(url, pageNumber, filterDate);

                allProcessedPermits.addAll(result);
                return null;
            });
        }
        return tasks;
    }
}
