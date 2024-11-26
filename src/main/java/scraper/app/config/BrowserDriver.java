package scraper.app.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@Getter
public class BrowserDriver {
    private final WebDriver driver;

    public BrowserDriver() {
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
