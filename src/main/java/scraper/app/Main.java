package scraper.app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import scraper.app.controller.ScraperController;
import scraper.app.model.Components;
import scraper.app.storage.DataStorage;
import scraper.app.util.ComponentsInitializer;
import scraper.app.util.ScraperTimer;

public class Main {
    public static final int THREAD_POOL_SIZE = 2;
    public static final int PAGES_AND_THREADS_AMOUNT = 2;
    public static final String FROM_DATE = "09/01/2024";
    public static final String TO_DATE = "09/30/2024";
    public static final String ISSUED_DATE = "Last 30 Days";
    public static final String FILE_PATH_FOR_HENDERSON = "src/main/resources/output_1.csv";
    public static final String FILE_PATH_FOR_CALABASAS = "src/main/resources/output_2.csv";
    public static final String HENDERSON_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";
    public static final String CALABASAS_URL = "https://ci-calabasas-ca.smartgovcommunity.com/"
            + "ApplicationPublic/ApplicationSearchAdvanced/Search";;

    public static void main(String[] args) {
        Components components = ComponentsInitializer.initializeComponents();
        DataStorage.clearLogFile();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        long totalScrapingTime = ScraperTimer.measureExecutionTime(() -> {
            //Future<?> task1 = executorService.submit(() -> runFirstScrapingTask(components));
            Future<?> task2 = executorService.submit(() -> runSecondScrapingTask(components));

            //handleTaskCompletion(task1);
            handleTaskCompletion(task2);
        });

        executorService.shutdown();
        new DataStorage().saveLogToCsv(
                System.lineSeparator() + "Total scraping time: " + totalScrapingTime);
        components.browserDriver().close();
    }

    private static void handleTaskCompletion(Future<?> task) {
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during task execution", e);
        }
    }

    private static void runFirstScrapingTask(Components components) {
        ScraperController controller = components.scraperController1();
        controller.startScraping(HENDERSON_URL, PAGES_AND_THREADS_AMOUNT,
                    FILE_PATH_FOR_HENDERSON, FROM_DATE, TO_DATE);
    }

    private static void runSecondScrapingTask(Components components) {
        ScraperController controller = components.scraperController2();
        controller.startScraping(CALABASAS_URL, PAGES_AND_THREADS_AMOUNT,
                FILE_PATH_FOR_CALABASAS, ISSUED_DATE);
    }
}
