package scraper.app.util;

import java.time.Duration;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

@UtilityClass
public class WebDriverUtils {

    public static final Duration TIMEOUT = Duration.ofSeconds(60);

    public void waitForPageToLoad(WebDriver driver, Duration timeout) {
        new WebDriverWait(driver, timeout).until(
                webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete")
        );
    }

    public void clickOnArgument(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
    }

    public void scrollIntoView(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public WebElement refreshIfStale(WebDriver driver, WebElement element, By locator) {
        try {
            element.isDisplayed();
            return element;
        } catch (StaleElementReferenceException e) {
            return driver.findElement(locator);
        }
    }

    public boolean isElementStale(WebElement element) {
        try {
            element.isDisplayed();
            return false;
        } catch (StaleElementReferenceException e) {
            return true;
        }
    }
}
