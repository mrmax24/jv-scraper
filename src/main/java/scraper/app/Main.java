package scraper.app;

import scraper.app.config.BrowserDriver;
import scraper.app.controller.ScraperController;
import scraper.app.service.DataExtractor;
import scraper.app.service.RecordNavigator;
import scraper.app.service.ScraperService;
import scraper.app.service.WebDriverProvider;
import scraper.app.storage.DataStorage;

public class Main {
    public static final String RESOURCE_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";

    public static void main(String[] args) {
        BrowserDriver browserDriver = new BrowserDriver();
        WebDriverProvider driverProvider = new WebDriverProvider();
        RecordNavigator recordNavigator = new RecordNavigator();
        DataExtractor recordExtractor = new DataExtractor(recordNavigator);
        DataStorage dataStorage = new DataStorage();

        ScraperService scraperService = new ScraperService(driverProvider,
                recordNavigator, recordExtractor);
        ScraperController scraperController = new ScraperController(scraperService, dataStorage);

        long start = System.currentTimeMillis();
        scraperController.startScraping(RESOURCE_URL, 5, "output.csv");
        long end = System.currentTimeMillis();
        long time = (end - start) / 1000;
        browserDriver.close();
        System.out.println("Total scrapping time: " + time);
    }
}
