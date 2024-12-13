package scraper.app.service.henderson;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.model.FilterDate;
import scraper.app.service.RecordNavigator;
import scraper.app.service.calabasas.CalabasasScraperService;

public class HendersonPageNavigator implements RecordNavigator {
    public static final String OVERLAY_ID = "overlay";
    private static final String SEARCH_BUTTON_ID = "button-Search";
    private static final String CONTACTS_BUTTON_ID = "button-TabButton-Contacts";
    private static final String ADVANCED_BUTTON_ID = "button-Advanced";
    private static final String SEARCH_SELECTOR_ID = "SearchModule";
    private static final String PERMIT_OPTION_ID = "Permit";
    private static final String FROM_DATE_FIELD_ID = "IssueDateFrom";
    private static final String TO_DATE_FIELD_ID = "IssueDateTo";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final Logger log =
            LoggerFactory.getLogger(CalabasasScraperService.class);

    @Override
    public void clickSearchButton(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            WebElement searchButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(SEARCH_BUTTON_ID)));
            if (searchButton.isDisplayed() && searchButton.isEnabled()) {
                searchButton.click();
            } else {
                System.out.println("Search button is not clickable or visible.");
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error finding or clicking 'Search' button: " + e.getMessage());
        }
    }

    @Override
    public void applyFiltration(WebDriver driver, FilterDate filterDate) {
        try {
            String fromDate = filterDate.getFromDate();
            String toDate = filterDate.getToDate();

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            clickPermitOption(driver);

            wait.until(ExpectedConditions.elementToBeClickable(By.id(ADVANCED_BUTTON_ID))).click();

            WebElement fromDateInput = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(FROM_DATE_FIELD_ID)));
            WebElement toDateInput = driver.findElement(By.id(TO_DATE_FIELD_ID));

            fromDateInput.clear();
            fromDateInput.sendKeys(fromDate);
            toDateInput.clear();
            toDateInput.sendKeys(toDate);

            log.info("Date filtration applied by date : {} to {}", fromDate, toDate);

        } catch (Exception e) {
            System.out.println("Error applying filtration: " + e.getMessage());
        }
    }

    public void clickContactsButton(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY_ID)));
            WebElement contactsButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(CONTACTS_BUTTON_ID)));
            if (contactsButton.isDisplayed() && contactsButton.isEnabled()) {
                contactsButton.click();
            } else {
                System.out.println("Search button is not clickable or visible.");
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error finding or clicking 'Contact' button: " + e.getMessage());
        }
    }

    public void findAndOpenLinkFromRecord(WebDriver driver, WebElement link) {
        link.getAttribute("href");
        ((JavascriptExecutor) driver).executeScript(
                "window.open(arguments[0], '_blank');", link);
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
        }
    }

    private void clickPermitOption(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            WebElement dropdownElement = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(SEARCH_SELECTOR_ID)));
            Select dropdown = new Select(dropdownElement);
            dropdown.selectByVisibleText(PERMIT_OPTION_ID);
            dropdownElement.click();
        } catch (Exception e) {
            System.out.println("Error selecting 'Permit' option: " + e.getMessage());
        }
    }
}
