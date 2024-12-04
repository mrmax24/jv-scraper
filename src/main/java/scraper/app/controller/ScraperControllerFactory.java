package scraper.app.controller;

import scraper.app.Main;
import scraper.app.config.CalabasasDriverProvider;
import scraper.app.config.HendersonDriverProvider;
import scraper.app.config.ThreadPoolManager;
import scraper.app.config.WebDriverProvider;
import scraper.app.service.DataExtractor;
import scraper.app.service.PageScraper;
import scraper.app.service.calabasas.CalabasasPageScraperImpl;
import scraper.app.service.calabasas.CalabasasScraperService;
import scraper.app.service.henderson.HandersonScraperService;
import scraper.app.service.henderson.HendersonPageRecordNavigator;
import scraper.app.service.calabasas.CalabasasPageRecordNavigator;
import scraper.app.service.henderson.HendersonPageScraperImpl;
import scraper.app.storage.DataStorage;

public class ScraperControllerFactory {
    public static ScraperController createScraperController(WebDriverProvider driverProvider) {
        if (driverProvider == null) {
            throw new IllegalArgumentException("Driver provider cannot be null");
        }

        if (driverProvider instanceof HendersonDriverProvider) {
            DataExtractor dataExtractor = new scraper.app.service
                    .henderson.DataExtractorImpl(new HendersonPageRecordNavigator());
            PageScraper pageScraper = new HendersonPageScraperImpl(new HendersonDriverProvider(),
                    new HendersonPageRecordNavigator(), dataExtractor);
            return new ScraperController(new HandersonScraperService(pageScraper),
                    new DataStorage(),
                    new ThreadPoolManager(Main.PAGES_NUMBER_AND_THREAD_POOL_SIZE));
        } else {
            DataExtractor dataExtractor = new scraper.app.service
                    .calabasas.DataExtractorImpl(new CalabasasPageRecordNavigator());
            PageScraper pageScraper = new CalabasasPageScraperImpl(new CalabasasDriverProvider(),
                    new CalabasasPageRecordNavigator(), dataExtractor);
            return new ScraperController(new CalabasasScraperService(pageScraper),
                    new DataStorage(),
                    new ThreadPoolManager(Main.PAGES_NUMBER_AND_THREAD_POOL_SIZE));
        }
    }
}
