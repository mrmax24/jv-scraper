package scraper.app.service;

import java.util.List;
import scraper.app.model.FilterDate;

public interface PageScraper {
    List<String> scrapeResource(String url, int pageNumber, FilterDate filterDate);
}
