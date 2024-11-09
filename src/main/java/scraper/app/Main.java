package scraper.app;

import scraper.app.config.BrowserDriver;
import scraper.app.controller.ScraperController;
import scraper.app.service.DataExtractor;
import scraper.app.service.ScraperService;
import scraper.app.storage.DataStorage;

public class Main {
    public static final String RESOURCE_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";

    public static void main(String[] args) {
        BrowserDriver browserDriver = new BrowserDriver();
        DataExtractor dataExtractor = new DataExtractor();
        ScraperService scraperService = new ScraperService();
        DataStorage dataStorage = new DataStorage();

        ScraperController scraperController = new ScraperController(scraperService, dataStorage);
        scraperController.startScraping(RESOURCE_URL, 10, "output.csv");

        browserDriver.close();
    }
}
