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

        DataStorage dataStorage = new DataStorage("output.csv");
        Thread dataStorageThread = new Thread(dataStorage);
        dataStorageThread.start();

        ScraperService scraperService = new ScraperService(driverProvider,
                recordNavigator, recordExtractor, dataStorage);
        ScraperController scraperController = new ScraperController(scraperService);

        scraperController.startScraping(RESOURCE_URL, 10);

        dataStorage.finish();
        try {
            dataStorageThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        browserDriver.close();
    }
}
