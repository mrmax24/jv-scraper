package scraper.app.service.calabasas;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
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
import scraper.app.util.WebDriverUtils;

public class CalabasasPageNavigator implements RecordNavigator {
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
            WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);
            By searchButtonLocator = By.id(SEARCH_BUTTON_ID);

            WebElement searchButton = wait.until(
                    ExpectedConditions.presenceOfElementLocated(searchButtonLocator));
            searchButton = WebDriverUtils.refreshIfStale(driver, searchButton, searchButtonLocator);

            WebDriverUtils.scrollIntoView(driver, searchButton);
            WebDriverUtils.clickOnArgument(driver, searchButton);
        } catch (StaleElementReferenceException e) {
            clickSearchButton(driver);
        } catch (Exception e) {
            log.error("Error clicking search button: {}", e.getMessage());
        }
    }

    @Override
    public void applyFiltration(WebDriver driver, FilterDate filterDate) {
        clickSearchOption(driver);

        try {
            WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);
            String issuedDate = filterDate.getIssueDate();

            WebElement field = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(ISSUED_DATE_FIELD_ID)));
            WebDriverUtils.refreshIfStale(driver, field, By.id(ISSUED_DATE_FIELD_ID));

            WebDriverUtils.scrollIntoView(driver, field);
            wait.until(ExpectedConditions.elementToBeClickable(field)).click();

            wait.until(ExpectedConditions
                    .invisibilityOfElementLocated(By.cssSelector(OVERLAY_CLASS)));

            WebElement container = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath(DATE_OPTION_TAG)));

            if (container != null) {
                String dateXPath = ".//span[text()='" + issuedDate + "']";
                WebElement dateOption = container.findElement(By.xpath(dateXPath));

                WebDriverUtils.refreshIfStale(driver, dateOption, By.xpath(dateXPath));
                WebDriverUtils.scrollIntoView(driver, dateOption);

                WebDriverUtils.clickOnArgument(driver, dateOption);

            }
            clickSearchButton(driver);

        } catch (Exception e) {
            log.error("Error applying filtration: {}", e.getMessage());
        }
    }

    @Override
    public void findAndOpenLinkFromRecord(WebDriver driver, WebElement link) {
        WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);
        try {
            WebElement permitLink = wait.until(ExpectedConditions.elementToBeClickable(link));
            WebDriverUtils.scrollIntoView(driver, permitLink);
            permitLink.click();
        } catch (Exception e) {
            log.error("Error navigating to record link: {}", e.getMessage());
        }
    }

    public void clickSearchOption(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);

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
                WebElement nextPageButton = new WebDriverWait(driver, WebDriverUtils.TIMEOUT).until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath(pageButtonXPath)));

                if (WebDriverUtils.isElementStale(nextPageButton)) {
                    nextPageButton = driver.findElement(By.xpath(pageButtonXPath));
                }
                WebDriverUtils.scrollIntoView(driver, nextPageButton);
                Thread.sleep(100);
                nextPageButton.click();
                clicked = true;
                new WebDriverWait(driver, WebDriverUtils.TIMEOUT)
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
}
