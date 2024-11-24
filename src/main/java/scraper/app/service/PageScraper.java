package scraper.app.service;

import java.util.List;

public interface PageScraper {
    List<String> scrapeFirstPage(String url, int pageNumber, String fromDate, String toDate);

    List<String> scrapeSecondPage(String url, int pageNumber, String issueDate);
}
