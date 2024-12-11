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
    public static final int PAGE_NUMBER = 3;
    public static final int THREAD_POOL_SIZE = 3;
    public static final String FROM_DATE = "09/01/2024";
    public static final String TO_DATE = "09/30/2024";
    public static final String ISSUED_DATE = "Last 30 Days";
    public static final String FILE_PATH_FOR_HENDERSON = "src/main/resources/output_1.csv";
    public static final String FILE_PATH_FOR_CALABASAS = "src/main/resources/output_2.csv";
    public static final String HENDERSON_URL
            = "https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search";
    public static final String CALABASAS_URL = "https://ci-calabasas-ca.smartgovcommunity.com/"
            + "ApplicationPublic/ApplicationSearchAdvanced/Search";

    public static void main(String[] args) {
        Components components = ComponentsInitializer.initializeComponents();
        DataStorage.clearLogFile();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        long totalScrapingTime = ScraperTimer.measureExecutionTime(() -> {
            Future<?> hendersonTask = executorService.submit(() -> runHendersonScrapingTask(components));
            Future<?> calabasasTask = executorService.submit(() -> runCalabasasScrapingTask(components));

            handleTaskCompletion(hendersonTask);
            handleTaskCompletion(calabasasTask);
        });
        executorService.shutdown();
        new DataStorage().saveLogToCsv(
                System.lineSeparator() + "Total scraping time: " + totalScrapingTime);
        components.browserDriver().close();
    }

    private static void runHendersonScrapingTask(Components components) {
        ScraperController controller = components.scraperController1();
        controller.startScrapingHendersonPage(HENDERSON_URL, PAGE_NUMBER,
                FILE_PATH_FOR_HENDERSON, FROM_DATE, TO_DATE);
    }

    private static void runCalabasasScrapingTask(Components components) {
        ScraperController controller = components.scraperController2();
        controller.startScrapingCalabasasPage(CALABASAS_URL, PAGE_NUMBER,
                FILE_PATH_FOR_CALABASAS, ISSUED_DATE);
    }

    private static void handleTaskCompletion(Future<?> task) {
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during task execution", e);
        }
    }
}
