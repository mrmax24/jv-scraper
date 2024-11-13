package scraper.app.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RecordNavigator {
    private static final String SEARCH_BUTTON = "button-Search";
    private static final String CONTACTS_BUTTON = "button-TabButton-Contacts";

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

    public void clickContactsButton(WebDriverWait wait) {
        try {
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

    public boolean navigateToNextPage(WebDriverWait wait, int pageCount) {
        try {
            WebElement nextPageLink = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(".link-NextPage")));
            nextPageLink.click();
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
