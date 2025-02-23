package scraper.app.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@FunctionalInterface
public interface TaskSupplier {
    List<Callable<Void>> getTasks(ConcurrentLinkedQueue<String> allProcessedPermits);
}
