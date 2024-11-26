package scraper.app.service;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.storage.DataStorage;

public class SecondPageRecordNavigator {
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final String SEARCH_BUTTON_ID = "Search";
    private static final String SEARCH_SELECTOR_ID = "Module";
    private static final String PERMIT_OPTION_ID = "Permits Only";
    private static final String ISSUED_DATE_FIELD_ID = "IssuedOn.Display";

    public void clickSearchButton(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement searchButton = driver.findElement(By.id(SEARCH_BUTTON_ID));
        js.executeScript("arguments[0].click();", searchButton);
    }

    private void clickSearchOption(WebDriver driver) {
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

    public void applyFiltration(WebDriver driver, String issueDate) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            clickSearchOption(driver);

            WebElement issuedDateInput = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(ISSUED_DATE_FIELD_ID)));

            issuedDateInput.clear();
            issuedDateInput.sendKeys(issueDate);

            new DataStorage().saveLogToCsv("Date filtration by date: "
                    + issueDate);

        } catch (Exception e) {
            System.out.println("Error applying filtration: " + e.getMessage());
        }
    }
}
