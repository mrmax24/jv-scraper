package scraper.app;

import scraper.app.config.BrowserDriver;
import scraper.app.controller.ScraperController;
import scraper.app.service.DataExtractor;
import scraper.app.service.PageScraper;
import scraper.app.service.RecordNavigator;
import scraper.app.service.ScraperService;
import scraper.app.service.WebDriverProvider;
import scraper.app.storage.DataStorage;

public class Main {
    public static final String RESOURCE_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";
    public static final int POOL_SIZE = 2;
    public static final int PAGES_AMOUNT = 2;
    public static final int DIVIDER = 1000;
    public static final String FROM_DATE = "09/01/2024";
    public static final String TO_DATE = "09/30/2024";
    public static final String FILE_PATH = "output.csv";

    public static void main(String[] args) {
        BrowserDriver browserDriver = new BrowserDriver();
        WebDriverProvider driverProvider = new WebDriverProvider();
        ScraperController scraperController = getScraperController(driverProvider);

        long start = System.currentTimeMillis();
        scraperController.startScraping(RESOURCE_URL, PAGES_AMOUNT, FILE_PATH,
                FROM_DATE, TO_DATE);
        long end = System.currentTimeMillis();
        long time = (end - start) / DIVIDER;
        browserDriver.close();
        System.out.println("Total scrapping time: " + time);
    }

    private static ScraperController getScraperController(WebDriverProvider driverProvider) {
        RecordNavigator recordNavigator = new RecordNavigator();
        DataExtractor dataExtractor = new DataExtractor(recordNavigator);
        DataStorage dataStorage = new DataStorage();
        PageScraper pageScraper = new PageScraper(driverProvider, recordNavigator, dataExtractor);

        ScraperService scraperService = new ScraperService(pageScraper, POOL_SIZE);
        return new ScraperController(scraperService, dataStorage);
    }
}
