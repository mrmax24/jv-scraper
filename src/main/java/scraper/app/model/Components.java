package scraper.app.model;

import scraper.app.config.BrowserDriver;
import scraper.app.controller.ScraperController;
import scraper.app.service.ThreadPoolManager;

public record Components(BrowserDriver browserDriver, ScraperController scraperController,
                         ThreadPoolManager threadPoolManager) {
}
