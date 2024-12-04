package scraper.app.util;

import scraper.app.Main;
import scraper.app.config.*;
import scraper.app.model.Components;
import scraper.app.controller.ScraperController;
import scraper.app.controller.ScraperControllerFactory;

public class ComponentsInitializer {
    public static Components initializeComponents() {
        BrowserDriver browserDriver = new BrowserDriver();
        WebDriverProvider hendersonDriverProvider = new HendersonDriverProvider();
        WebDriverProvider calabasasDriverProvider = new CalabasasDriverProvider();
        ThreadPoolManager threadPoolManager
                = new ThreadPoolManager(Main.PAGES_NUMBER_AND_THREAD_POOL_SIZE);

        ScraperController scraperControllerForHenderson = ScraperControllerFactory
                .createScraperController(hendersonDriverProvider);
        ScraperController scraperControllerForCalabasas = ScraperControllerFactory
                .createScraperController(calabasasDriverProvider);

        return new Components(browserDriver, scraperControllerForHenderson,
                scraperControllerForCalabasas, threadPoolManager);
    }
}
