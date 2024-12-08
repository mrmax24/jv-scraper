package scraper.app.service.calabasas;

import lombok.RequiredArgsConstructor;
import scraper.app.model.FilterDate;
import scraper.app.service.PageScraper;
import scraper.app.service.ScraperService;
import scraper.app.storage.DataStorage;
import java.util.List;

@RequiredArgsConstructor
public class CalabasasScraperService {
    private final PageScraper pageScraper;

    public List<String> scrape(String url, int pageNumber, FilterDate filterDate) {
        List<String> strings = pageScraper.scrapeResource(url, pageNumber, filterDate);
        new DataStorage().saveLogToCsv(
                "Total records found for the city website: "
                        + strings.size());
        return strings;
    }
}