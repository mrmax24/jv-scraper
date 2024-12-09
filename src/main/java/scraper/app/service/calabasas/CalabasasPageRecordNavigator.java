package scraper.app.service.calabasas;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.model.FilterDate;
import scraper.app.service.RecordNavigator;

public class CalabasasPageRecordNavigator implements RecordNavigator {
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    public static final String OVERLAY_ID = "overlay";
    private static final String SEARCH_BUTTON_ID = "Search";
    private static final String SEARCH_SELECTOR_ID = "Module";
    private static final String PERMIT_OPTION_ID = "Permits Only";
    private static final String ISSUED_DATE_FIELD_ID = "IssuedOn.Display";
    private static final String DATE_OPTION_TAG = "//div[contains(@class,"
            + " 'br-datepicker-presets-selections')]";
    public static final String NEXT_PAGE_BUTTON = "//li/a[contains(@onclick, "
            + "'ApplicationSearchAdvancedResults.gotoPage')]";

    @Override
    public void clickSearchButton(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY_ID)));

            WebElement searchButton = driver.findElement(By.id(SEARCH_BUTTON_ID));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            Boolean isCovered = (Boolean) js.executeScript(
                    "var elem = arguments[0];" +
                            "var rect = elem.getBoundingClientRect();"
                            + "return (rect.top >= 0 && rect.left >= 0 && "
                            + "rect.bottom <= window.innerHeight && rect.right "
                            + "<= window.innerWidth);", searchButton);

            if (!isCovered) {
                System.out.println("Element is covered, waiting...");
                wait.until(ExpectedConditions.elementToBeClickable(searchButton));
            }

            js.executeScript("arguments[0].scrollIntoView(true);", searchButton);

            wait.until(ExpectedConditions.elementToBeClickable(searchButton));

            js.executeScript("arguments[0].click();", searchButton);

        } catch (Exception e) {
            throw new RuntimeException("Error clicking search button: " + e.getMessage());
        }
    }

    @Override
    public void applyFiltration(WebDriver driver, FilterDate filterDate) {
        clickSearchOption(driver);

        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            String issuedDate = filterDate.getIssueDate();

            WebElement field = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(ISSUED_DATE_FIELD_ID)));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true);", field);
            wait.until(ExpectedConditions.elementToBeClickable(field)).click();

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".overlay-class")));

            WebElement container = wait.until(ExpectedConditions
                    .elementToBeClickable(By.xpath(DATE_OPTION_TAG)));

            WebElement dateOption = container.findElement(By.xpath(
                    ".//span[text()='" + issuedDate + "']"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true);", dateOption);

            try {
                wait.until(ExpectedConditions.elementToBeClickable(dateOption)).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateOption);
            }

            clickSearchButton(driver);

        } catch (Exception e) {
            throw new RuntimeException("Error applying filtration: " + e.getMessage());
        }
    }

    @Override
    public void findAndOpenLinkFromRecord(WebDriver driver, WebElement link) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        try {
            WebElement permitLink = wait.until(ExpectedConditions.elementToBeClickable(link));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", permitLink);
            permitLink.click();
        } catch (Exception e) {
            throw new RuntimeException("Error navigating to record link: " + e.getMessage());
        }
    }

    public void clickSearchOption(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            WebElement dropdownElement = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(SEARCH_SELECTOR_ID)));

            Select dropdown = new Select(dropdownElement);
            dropdown.selectByVisibleText(PERMIT_OPTION_ID);

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY_ID)));
        } catch (Exception e) {
            throw new RuntimeException("Error selecting 'Permit' option: " + e.getMessage());
        }
    }

    public void clickNextButton(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        WebElement nextButton = driver.findElement(By.xpath(NEXT_PAGE_BUTTON));
        jsExecutor.executeScript("arguments[0].click();", nextButton);
    }
}
