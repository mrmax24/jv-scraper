package scraper.app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.config.LogConfig;
import scraper.app.controller.ScraperController;
import scraper.app.model.Components;
import scraper.app.util.ComponentsInitializer;
import scraper.app.util.ScraperTimer;

public class Main {
    public static final int PAGE_NUMBER = 2;
    public static final int THREAD_POOL_SIZE = 2;
    public static final String FROM_DATE = "09/01/2024";
    public static final String TO_DATE = "09/30/2024";
    public static final String ISSUED_DATE = "Last 30 Days";
    public static final String FILE_PATH_FOR_HENDERSON = "src/main/resources/output_1.csv";
    public static final String FILE_PATH_FOR_CALABASAS = "src/main/resources/output_2.csv";
    public static final String HENDERSON_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";
    public static final String CALABASAS_URL = "https://ci-calabasas-ca.smartgovcommunity.com/"
            + "ApplicationPublic/ApplicationSearchAdvanced/Search";
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LogConfig.configureLogging();
        Components components = ComponentsInitializer.initializeComponents();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        log.debug("Starting scraping tasks");

        long totalScrapingTime = ScraperTimer.measureExecutionTime(() -> {
            Future<?> hendersonTask = executorService.submit(()
                    -> runHendersonScrapingTask(components));
            Future<?> calabasasTask = executorService.submit(()
                    -> runCalabasasScrapingTask(components));

            handleTaskCompletion(hendersonTask);
            handleTaskCompletion(calabasasTask);
        });
        executorService.shutdown();
        log.info("{}Total scraping time: {}", System.lineSeparator(), totalScrapingTime);
        components.browserDriver().close();
    }

    private static void runHendersonScrapingTask(Components components) {
        ScraperController controller = components.scraperController1();
        controller.startScrapingHendersonPage(HENDERSON_URL, PAGE_NUMBER,
                FILE_PATH_FOR_HENDERSON, FROM_DATE, TO_DATE);
        log.info("Henderson scraping task completed successfully");
    }

    private static void runCalabasasScrapingTask(Components components) {
        log.info("Starting Calabasas scraping task");
        try {
            ScraperController controller = components.scraperController2();
            controller.startScrapingCalabasasPage(CALABASAS_URL, PAGE_NUMBER,
                    FILE_PATH_FOR_CALABASAS, ISSUED_DATE);
            log.info("Calabasas scraping task completed successfully");
        } catch (Exception e) {
            log.error("Error during Calabasas scraping task: {}", e.getMessage(), e);
        }
    }

    private static void handleTaskCompletion(Future<?> task) {
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during task execution", e);
        }
    }
}
