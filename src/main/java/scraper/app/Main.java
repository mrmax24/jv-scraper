package scraper.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import scraper.app.config.BrowserDriver;
import scraper.app.config.ThreadPoolManager;
import scraper.app.config.WebDriverProvider;
import scraper.app.controller.ScraperController;
import scraper.app.model.Components;
import scraper.app.service.DataExtractor;
import scraper.app.service.FirstPageRecordNavigator;
import scraper.app.service.PageScraper;
import scraper.app.service.ScraperService;
import scraper.app.service.SecondPageRecordNavigator;
import scraper.app.service.impl.DataExtractorImpl;
import scraper.app.service.impl.PageScraperImpl;
import scraper.app.service.impl.ScraperServiceImpl;
import scraper.app.storage.DataStorage;

public class Main {
    public static final int THREAD_POOL_SIZE = 2;
    public static final int PAGES_AND_THREADS_AMOUNT = 2;
    public static final int DIVIDER = 1000;
    public static final String FROM_DATE = "09/01/2024";
    public static final String TO_DATE = "09/30/2024";
    public static final String ISSUED_DATE = "Last 30 Days";
    public static final String FILE_PATH_1 = "src/main/resources/output_1.csv";
    public static final String FILE_PATH_2 = "src/main/resources/output_2.csv";
    public static final String RESOURCE_URL_1
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";
    private static final String RESOURCE_URL_2
            = "https://ci-calabasas-ca.smartgovcommunity.com/"
            + "ApplicationPublic/ApplicationSearchAdvanced/Search";

    public static void main(String[] args) {
        Components components = initializeComponents();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        long totalScrapingTime = measureExecutionTime(() -> {
            executorService.submit(() -> runScrapingTask1(components));
            executorService.submit(() -> runScrapingTask2(components));
        });

        executorService.shutdown();
        new DataStorage().saveLogToCsv("Total scraping time: " + totalScrapingTime);
        components.browserDriver().close();
    }

    private static Components initializeComponents() {
        BrowserDriver browserDriver = new BrowserDriver();
        WebDriverProvider driverProvider1 = new WebDriverProvider();
        WebDriverProvider driverProvider2 = new WebDriverProvider();
        ScraperController scraperController1 = getScraperController(driverProvider1);
        ScraperController scraperController2 = getScraperController(driverProvider2);
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(THREAD_POOL_SIZE);
        return new Components(browserDriver, scraperController1,
                scraperController2, threadPoolManager);
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

        ThreadPoolManager threadPoolManager = new ThreadPoolManager(THREAD_POOL_SIZE);
        ScraperService scraperService = new ScraperServiceImpl(pageScraper);
        return new ScraperController(scraperService, dataStorage, threadPoolManager);
    }

    private static void runScrapingTask1(Components components) {
        ScraperController controller = components.scraperController1();
        controller.startScraping(Main.RESOURCE_URL_1, PAGES_AND_THREADS_AMOUNT,
                    Main.FILE_PATH_1, FROM_DATE, TO_DATE);
    }

    private static void runScrapingTask2(Components components) {
        ScraperController controller = components.scraperController2();
        controller.startScraping(Main.RESOURCE_URL_2, PAGES_AND_THREADS_AMOUNT,
                Main.FILE_PATH_2, ISSUED_DATE);
    }
}
