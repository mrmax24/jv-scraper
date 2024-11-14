package scraper.app.controller;

import lombok.RequiredArgsConstructor;
import scraper.app.service.ScraperService;
import scraper.app.storage.DataStorage;
import java.util.List;

@RequiredArgsConstructor
public class ScraperController {
    private final ScraperService scraperService;
    private final DataStorage dataStorage;

    public void startScraping(String url, int pages, String filePath) {
        List<String> data = scraperService.scrape(url, pages);
        dataStorage.saveToCsv(data, filePath);
    }
}
