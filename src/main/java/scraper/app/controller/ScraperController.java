package scraper.app.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import scraper.app.service.ScraperService;
import scraper.app.storage.DataStorage;

@RequiredArgsConstructor
public class ScraperController {
    private final ScraperService scraperService;
    private final DataStorage dataStorage;

    public void startScraping(String url, int pages, String filePath, String fromDate,
                              String toDate) {
        List<String> data = scraperService.scrape(url, pages, fromDate, toDate);
        dataStorage.saveToCsv(data, filePath);
    }
}
