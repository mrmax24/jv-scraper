package scraper.app.config;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.service.calabasas.CalabasasScraperService;

public class ThreadPoolManager {
    private static final Logger log =
            LoggerFactory.getLogger(CalabasasScraperService.class);
    private static final int TIMEOUT = 60;
    private final ExecutorService executorService;

    public ThreadPoolManager(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public <T> void submitTasks(List<Callable<T>> tasks) {
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error submitting tasks to executor", e);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                log.info("Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
