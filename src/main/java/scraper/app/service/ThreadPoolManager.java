package scraper.app.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    public static final int TIMEOUT = 60;
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

    public void submitRunnables(List<Runnable> tasks) {
        for (Runnable task : tasks) {
            executorService.submit(task);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
