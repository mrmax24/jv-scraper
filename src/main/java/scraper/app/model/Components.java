package scraper.app.model;

import scraper.app.config.BrowserDriver;
import scraper.app.config.ThreadPoolManager;
import scraper.app.controller.ScraperController;

public record Components(BrowserDriver browserDriver, ScraperController scraperController1,
                         ScraperController scraperController2,
                         ThreadPoolManager threadPoolManager) {
}
