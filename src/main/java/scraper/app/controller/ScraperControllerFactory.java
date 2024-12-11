package scraper.app.controller;

import scraper.app.Main;
import scraper.app.config.HendersonDriverProvider;
import scraper.app.config.ThreadPoolManager;
import scraper.app.config.WebDriverProvider;
import scraper.app.service.DataExtractor;
import scraper.app.service.PageScraper;
import scraper.app.service.calabasas.CalabasasDataExtractor;
import scraper.app.service.calabasas.CalabasasPageScraper;
import scraper.app.service.calabasas.CalabasasScraperService;
import scraper.app.service.henderson.HandersonScraperService;
import scraper.app.service.henderson.HendersonDataExtractor;
import scraper.app.service.henderson.HendersonPageNavigator;
import scraper.app.service.calabasas.CalabasasPageNavigator;
import scraper.app.service.henderson.HendersonPageScraper;
import scraper.app.storage.DataStorage;

public class ScraperControllerFactory {
    public static ScraperController createScraperController(WebDriverProvider driverProvider) {
        if (driverProvider == null) {
            throw new IllegalArgumentException("Driver provider cannot be null");
        }

        if (driverProvider instanceof HendersonDriverProvider) {
            DataExtractor dataExtractor = new HendersonDataExtractor(new HendersonPageNavigator());

            PageScraper pageScraper = new HendersonPageScraper(new HendersonDriverProvider(),
                    new HendersonPageNavigator(), dataExtractor);

            return new ScraperController(new HandersonScraperService(pageScraper),
                    new DataStorage(),
                    new ThreadPoolManager(Main.THREAD_POOL_SIZE));
        } else {
            DataExtractor dataExtractor = new CalabasasDataExtractor();
            CalabasasPageNavigator recordNavigator = new CalabasasPageNavigator();

            return new ScraperController(new CalabasasScraperService(
                    new CalabasasPageScraper(recordNavigator, dataExtractor), recordNavigator),
                    new DataStorage(),
                    new ThreadPoolManager(Main.THREAD_POOL_SIZE));
        }
    }
}
