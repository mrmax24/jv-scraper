package scraper.app.service;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RecordNavigator {
    public static final String OVERLAY = "overlay";
    private static final String SEARCH_BUTTON = "button-Search";
    private static final String CONTACTS_BUTTON = "button-TabButton-Contacts";
    private static final String NEXT_PAGE_BUTTON = "link-NextPage";

    public void clickSearchButton(WebDriverWait wait) {
        try {
            WebElement searchButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(SEARCH_BUTTON)));
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

    public void clickContactsButton(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY)));
            WebElement contactsButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(CONTACTS_BUTTON)));
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

    public boolean navigateToNextPage(WebDriver driver, WebDriverWait wait, int pageCount) {
        try {
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement nextPageLink = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id(NEXT_PAGE_BUTTON)));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true);", nextPageLink);
            wait.until(ExpectedConditions.elementToBeClickable(nextPageLink)).click();
            wait.until(ExpectedConditions.stalenessOf(nextPageLink));
            System.out.println("Navigated to page " + (pageCount + 2));
            return true;
        } catch (Exception e) {
            System.out.println("Next page not found or error occurred: " + e.getMessage());
            return false;
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
}
