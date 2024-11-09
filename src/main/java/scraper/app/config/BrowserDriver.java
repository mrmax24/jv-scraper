package scraper.app.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@Getter
@RequiredArgsConstructor
public class BrowserDriver {
    private static final String CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String CHROME_DRIVER_PATH
            = "C:\\Users\\Code&Care0157\\Drivers\\chromedriver-win64\\chromedriver.exe";
    private final WebDriver driver;

    public BrowserDriver() {
        System.setProperty(CHROME_DRIVER, CHROME_DRIVER_PATH);
        this.driver = new ChromeDriver();
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
