package scraper.app.service;

import scraper.app.model.FilterDate;

import java.util.List;

public interface PageScraper {
    List<String> scrapeResource(String url, int pageNumber, FilterDate filterDate);
}
