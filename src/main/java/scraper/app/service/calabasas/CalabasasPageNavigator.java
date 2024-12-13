package scraper.app.service.calabasas;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.model.FilterDate;
import scraper.app.service.RecordNavigator;

public class CalabasasPageNavigator implements RecordNavigator {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Logger log = LoggerFactory.getLogger(CalabasasScraperService.class);
    private static final String OVERLAY_ID = "overlay";
    private static final String OVERLAY_CLASS = ".overlay-class";
    private static final String SEARCH_BUTTON_ID = "Search";
    private static final String SEARCH_SELECTOR_ID = "Module";
    private static final String PERMIT_OPTION_ID = "Permits Only";
    private static final String ISSUED_DATE_FIELD_ID = "IssuedOn.Display";
    private static final String DATE_OPTION_TAG
            = "//div[contains(@class, 'br-datepicker-presets-selections')]";
    private static final String NEXT_PAGE_BUTTON
            = "//li/a[contains(@onclick, 'ApplicationSearchAdvancedResults.gotoPage";

    @Override
    public void clickSearchButton(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            By searchButtonLocator = By.id(SEARCH_BUTTON_ID);

            WebElement searchButton = wait.until(
                    ExpectedConditions.presenceOfElementLocated(searchButtonLocator));
            searchButton = refreshIfStale(driver, searchButton, searchButtonLocator);

            scrollIntoView(driver, searchButton);
            clickOnArgument(driver, searchButton);
        } catch (StaleElementReferenceException e) {
            clickSearchButton(driver);
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
            refreshIfStale(driver, field, By.id(ISSUED_DATE_FIELD_ID));

            scrollIntoView(driver, field);
            wait.until(ExpectedConditions.elementToBeClickable(field)).click();

            wait.until(ExpectedConditions
                    .invisibilityOfElementLocated(By.cssSelector(OVERLAY_CLASS)));

            WebElement container = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath(DATE_OPTION_TAG)));

            if (container != null) {
                String dateXPath = ".//span[text()='" + issuedDate + "']";
                WebElement dateOption = container.findElement(By.xpath(dateXPath));

                refreshIfStale(driver, dateOption, By.xpath(dateXPath));
                scrollIntoView(driver, dateOption);

                clickOnArgument(driver, dateOption);

            }
            clickSearchButton(driver);

        } catch (Exception e) {
            log.error("Error applying filtration: {}", e.getMessage());
        }
    }

    @Override
    public void findAndOpenLinkFromRecord(WebDriver driver, WebElement link) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        try {
            WebElement permitLink = wait.until(ExpectedConditions.elementToBeClickable(link));
            scrollIntoView(driver, permitLink);
            permitLink.click();
        } catch (Exception e) {
            log.error("Error navigating to record link: {}", e.getMessage());
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
            log.error("Error selecting 'Permit' option: {}", e.getMessage());
        }
    }

    public void clickNextButton(WebDriver driver, int pageIndex) {
        boolean clicked = false;
        int attempts = 0;

        while (!clicked && attempts < 3) {
            try {
                String pageButtonXPath = NEXT_PAGE_BUTTON + "(" + pageIndex + ")')]";
                WebElement nextPageButton = new WebDriverWait(driver, TIMEOUT).until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath(pageButtonXPath)));

                if (isElementStale(nextPageButton)) {
                    nextPageButton = driver.findElement(By.xpath(pageButtonXPath));
                }
                scrollIntoView(driver, nextPageButton);
                Thread.sleep(100);
                nextPageButton.click();
                clicked = true;
                new WebDriverWait(driver, TIMEOUT)
                        .until(ExpectedConditions.stalenessOf(nextPageButton));
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                attempts++;
            } catch (Exception e) {
                log.error("Error clicking next page button: {}", e.getMessage());
                break;
            }
        }
        if (!clicked) {
            log.error("Failed to click next page button after 3 attempts");
        }
    }

    private boolean isElementStale(WebElement element) {
        try {
            element.isDisplayed();
            return false;
        } catch (StaleElementReferenceException e) {
            return true;
        }
    }

    private void clickOnArgument(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
    }

    private void scrollIntoView(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", element);
    }

    private WebElement refreshIfStale(WebDriver driver, WebElement element, By locator) {
        try {
            element.isDisplayed();
            return element;
        } catch (StaleElementReferenceException e) {
            return driver.findElement(locator);
        }
    }
}
