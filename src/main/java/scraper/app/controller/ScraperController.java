package scraper.app.controller;

import lombok.RequiredArgsConstructor;
import scraper.app.service.ScraperService;

@RequiredArgsConstructor
public class ScraperController {
    private final ScraperService scrapperTestService;

    public void startScraping(String url, int pages) {
        scrapperTestService.scrape(url, pages);
    }
}
