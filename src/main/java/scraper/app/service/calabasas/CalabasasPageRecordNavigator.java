package scraper.app.service.calabasas;

import java.time.Duration;

import org.openqa.selenium.*;
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
    public static final String NEXT_PAGE_BUTTON = "//li/a[contains(@onclick,"
            + " 'ApplicationSearchAdvancedResults.gotoPage";

    @Override
    public void clickSearchButton(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        try {
            // Очікуємо, поки зникне оверлей
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OVERLAY_ID)));

            // Повторно знаходимо кнопку після оновлення DOM
            WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(SEARCH_BUTTON_ID)));

            // Прокручуємо до кнопки
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", searchButton);

            // Очікуємо клікабельності
            wait.until(ExpectedConditions.elementToBeClickable(searchButton));

            // Натискаємо кнопку через JS (якщо стандартний клік недоступний)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);

            System.out.println("Search button clicked successfully.");

        } catch (StaleElementReferenceException e) {
            System.err.println("Stale element reference exception. Retrying...");
            clickSearchButton(driver); // Повторна спроба
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

    public void clickNextButton(WebDriver driver, int pageIndex) {
        boolean clicked = false;
        int attempts = 0;
        while (!clicked && attempts < 3) {
            try {
                String pageButtonXPath = NEXT_PAGE_BUTTON + "(" + pageIndex + ")')]";
                WebElement nextPageButton = new WebDriverWait(driver, TIMEOUT)
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(pageButtonXPath)));

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView(true);", nextPageButton);
                Thread.sleep(500);

                nextPageButton.click();
                clicked = true;
                System.out.println("Clicked on page button with index: " + pageIndex);

                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                wait.until(ExpectedConditions.stalenessOf(nextPageButton)); // Wait for the page content to reload

            } catch (ElementClickInterceptedException e) {
                System.err.println("Element click intercepted, retrying...");
                attempts++;
            } catch (StaleElementReferenceException e) {
                System.err.println("Stale element reference, retrying...");
                attempts++;
            } catch (Exception e) {
                System.err.println("Error clicking next page button: " + e.getMessage());
                break;
            }
        }
        if (!clicked) {
            throw new RuntimeException("Failed to click next page button after 3 attempts");
        }
    }

}
