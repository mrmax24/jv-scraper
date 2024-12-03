package scraper.app.util;

public class ScraperTimer {
    public static long measureExecutionTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();
        return (end - start) / 1000;
    }
}
