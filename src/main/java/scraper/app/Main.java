package scraper.app;

import java.util.Arrays;
import scraper.app.config.BrowserDriver;
import scraper.app.config.WebDriverProvider;
import scraper.app.controller.ScraperController;
import scraper.app.model.Components;
import scraper.app.service.DataExtractor;
import scraper.app.service.FirstPageRecordNavigator;
import scraper.app.service.PageScraper;
import scraper.app.service.ScraperService;
import scraper.app.service.SecondPageRecordNavigator;
import scraper.app.service.ThreadPoolManager;
import scraper.app.service.impl.DataExtractorImpl;
import scraper.app.service.impl.PageScraperImpl;
import scraper.app.service.impl.ScraperServiceImpl;
import scraper.app.storage.DataStorage;

public class Main {
    public static final int POOL_SIZE = 2;
    public static final int PAGES_AMOUNT = 2;
    public static final int DIVIDER = 1000;
    public static final String FROM_DATE = "09/01/2024";
    public static final String TO_DATE = "09/30/2024";
    public static final String ISSUED_DATE = "Last 30 Days";
    public static final String FIRST_FILE_PATH = "first-output.csv";
    public static final String SECOND_FILE_PATH = "second-output.csv";
    public static final String FIRST_RESOURCE_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";
    private static final String SECOND_RESOURCE_URL
            = "https://ci-calabasas-ca.smartgovcommunity.com/"
            + "ApplicationPublic/ApplicationSearchAdvanced/Search";

    public static void main(String[] args) {
        Components components = initializeComponents();
        long totalScrapingTime = measureExecutionTime(() -> runScrapingTasks(components));
        components.browserDriver().close();
        System.out.println("Total scraping time: " + totalScrapingTime);
    }

    private static Components initializeComponents() {
        BrowserDriver browserDriver = new BrowserDriver();
        WebDriverProvider driverProvider = new WebDriverProvider();
        ScraperController scraperController = getScraperController(driverProvider);
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(POOL_SIZE);
        return new Components(browserDriver, scraperController, threadPoolManager);
    }

    private static void runScrapingTasks(Components components) {
        components.threadPoolManager().submitRunnables(Arrays.asList(
                () -> components.scraperController().startScraping(FIRST_RESOURCE_URL,
                        PAGES_AMOUNT, FIRST_FILE_PATH, FROM_DATE, TO_DATE),
                () -> components.scraperController().startScraping(SECOND_RESOURCE_URL,
                        PAGES_AMOUNT, SECOND_FILE_PATH, ISSUED_DATE)
        ));
        components.threadPoolManager().shutdown();
    }

    private static long measureExecutionTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();
        return (end - start) / DIVIDER;
    }

    private static ScraperController getScraperController(WebDriverProvider driverProvider) {
        FirstPageRecordNavigator firstNavigator = new FirstPageRecordNavigator();
        SecondPageRecordNavigator secondNavigator = new SecondPageRecordNavigator();
        DataExtractor dataExtractor = new DataExtractorImpl(firstNavigator);
        DataStorage dataStorage = new DataStorage();
        PageScraper pageScraper =
                new PageScraperImpl(driverProvider, firstNavigator, secondNavigator, dataExtractor);

        ThreadPoolManager threadPoolManager = new ThreadPoolManager(POOL_SIZE);
        ScraperService scraperService = new ScraperServiceImpl(pageScraper);
        return new ScraperController(scraperService, dataStorage, threadPoolManager);
    }
}
