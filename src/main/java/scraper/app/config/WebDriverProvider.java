package scraper.app.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public abstract class WebDriverProvider {
    public WebDriver setupWebDriver() {
        ChromeOptions options = new ChromeOptions();
        return new ChromeDriver(options);
    }
}
