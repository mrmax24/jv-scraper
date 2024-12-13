package scraper.app.util;

import scraper.app.Main;
import scraper.app.config.BrowserDriver;
import scraper.app.config.CalabasasDriverProvider;
import scraper.app.config.HendersonDriverProvider;
import scraper.app.config.ThreadPoolManager;
import scraper.app.config.WebDriverProvider;
import scraper.app.controller.ScraperController;
import scraper.app.controller.ScraperControllerFactory;
import scraper.app.model.Components;

public class ComponentsInitializer {
    public static Components initializeComponents() {
        BrowserDriver browserDriver = new BrowserDriver();
        WebDriverProvider hendersonDriverProvider = new HendersonDriverProvider();
        WebDriverProvider calabasasDriverProvider = new CalabasasDriverProvider();
        ThreadPoolManager threadPoolManager
                = new ThreadPoolManager(Main.THREAD_POOL_SIZE);

        ScraperController scraperControllerForHenderson = ScraperControllerFactory
                .createScraperController(hendersonDriverProvider);
        ScraperController scraperControllerForCalabasas = ScraperControllerFactory
                .createScraperController(calabasasDriverProvider);

        return new Components(browserDriver, scraperControllerForHenderson,
                scraperControllerForCalabasas, threadPoolManager);
    }
}
