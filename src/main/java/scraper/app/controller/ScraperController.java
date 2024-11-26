package scraper.app.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import scraper.app.config.ThreadPoolManager;
import scraper.app.service.ScraperService;
import scraper.app.storage.DataStorage;

@RequiredArgsConstructor
public class ScraperController {
    private final ScraperService scraperService;
    private final DataStorage dataStorage;
    private final ThreadPoolManager threadPoolManager;

    public void startScraping(String url, int pages,
                              String filePath, String fromDate, String toDate) {
        List<String> data = scraperService
                .scrapeWithDates(url, pages, fromDate, toDate, threadPoolManager);
        dataStorage.saveToCsv(data, filePath);
    }

    public void startScraping(String url, int pages,
                              String filePath, String issuedDate) {
        List<String> data = scraperService
                .scrapeWithIssuedDate(url, pages, issuedDate, threadPoolManager);
        dataStorage.saveToCsv(data, filePath);
    }
}
