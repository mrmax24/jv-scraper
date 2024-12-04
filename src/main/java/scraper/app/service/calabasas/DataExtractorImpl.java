package scraper.app.service.calabasas;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.service.DataExtractor;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class DataExtractorImpl implements DataExtractor {
    private static final String RECORD_TITLE_TAG = "//div[@class='search-result-title']/a[text()]";
    private static final String DESCRIPTION_XPATH = "//td[@class='project-header-title p-b']";
    private static final String RECORD_NUMBER_STATUS_XPATH = "//td[@class"
            + "='project-header-field-label' and text()='Record Number']"
            + "/following-sibling::td//div[contains(@class, 'project-header-status-badge')]";
    private static final String FEES_LABEL_XPATH = ".//span[normalize-space(text())='Current Fees']";
    private static final String DOLLARS_XPATH = ".//span[@class='dollars']";
    private static final String CENTS_XPATH = ".//span[@class='cents']";
    private static final String UNIT_XPATH = ".//span[@class='unit']";
    private static final String LOCATION_LABEL_XPATH = ".//td[@class='project-section-field-label']//span";
    private static final String FIRST_LOCATION_XPATH = ".//td[@class='project-section-field-value']//span";
    private static final String SECOND_LOCATION_XPATH = ".//td[@class='project-section-field-value']"
            + "/span[contains(text(), 'Calabasas')]";
    private static final String PARCEL_LABEL_XPATH = ".//td[@class='project-section-field-value']"
            + "/span[contains(text(), 'Parcel')]";
    private static final String PARCEL_XPATH = ".//td[@class='project-section-field-value']/span/a";
    private static final String FIELD_LABEL_XPATH = ".//td[@class='project-section-field-label']";
    private static final String FIELD_VALUE_XPATH = ".//td[@class='project-section-field-value']";
    private static final String NEW_LINE = System.lineSeparator();
    private final CalabasasPageRecordNavigator recordNavigator;

    @Override
    public String extractRecords(WebElement record, WebDriver driver, WebElement link) {
        StringBuilder result = new StringBuilder();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        String originalWindow = driver.getWindowHandle();

        try {
            System.out.println("Waiting for title element...");
            WebElement titleElement = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.xpath(RECORD_TITLE_TAG)));
            System.out.println("Title element found: " + titleElement.getText().trim());
            result.append(titleElement.getText().trim()).append(NEW_LINE);

            // Navigate to link
            System.out.println("Navigating to link...");
            recordNavigator.findAndOpenLinkFromRecord(driver, link);

            // Switch to new window
            System.out.println("Waiting for new window...");
            Set<String> windowHandles = driver.getWindowHandles();
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    System.out.println("Switching to new window...");
                    driver.switchTo().window(handle);
                    // Process the tab here if necessary
                    driver.close(); // Close the tab after processing
                }
            }
            driver.switchTo().window(originalWindow);

            // Wait for the page to load completely
            System.out.println("Waiting for page to load...");
            wait.until(d -> ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));

            // Extract details
            System.out.println("Extracting detailed data...");
            result.append(extractDetailedData(driver))
                    .append(extractLocationData(driver))
                    .append(extractDatesData(driver));

            // Close new tab and return to the original window
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            System.err.println("Error processing item: " + e.getMessage());
            e.printStackTrace();
        }

        return result.toString();
    }

    private String extractDetailedData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();
        System.out.println("Extracting detailed data...");
        String description = driver.findElement(By.xpath(DESCRIPTION_XPATH)).getText();
        String status = driver.findElement(By.xpath(RECORD_NUMBER_STATUS_XPATH)).getText();
        String feesLabel = driver.findElement(By.xpath(FEES_LABEL_XPATH)).getText();
        String currencyUnit = driver.findElement(By.xpath(UNIT_XPATH)).getText();
        String dollars = driver.findElement(By.xpath(DOLLARS_XPATH)).getText();
        String cents = driver.findElement(By.xpath(CENTS_XPATH)).getText();

        builder.append(description).append(NEW_LINE)
                .append(status).append(NEW_LINE)
                .append(feesLabel).append(": ")
                .append(currencyUnit)
                .append(dollars).append(".").append(cents).append(NEW_LINE);
        return builder.toString();
    }

    private String extractLocationData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();
        System.out.println("Extracting location data...");

        // Отримуємо елементи
        WebElement locationLabel = driver.findElement(By.xpath(LOCATION_LABEL_XPATH));
        WebElement firstLocation = driver.findElement(By.xpath(FIRST_LOCATION_XPATH));
        WebElement secondLocation = driver.findElement(By.xpath(SECOND_LOCATION_XPATH));
        WebElement parcelLabel = driver.findElement(By.xpath(PARCEL_LABEL_XPATH));
        WebElement parcelValue = driver.findElement(By.xpath(PARCEL_XPATH));

        // Додаємо текстові значення елементів до результату
        builder.append(locationLabel.getText()).append(": ")
                .append(firstLocation.getText()).append(", ")
                .append(secondLocation.getText()).append(NEW_LINE)
                .append(parcelLabel.getText()).append(": ")
                .append(parcelValue.getText()).append(NEW_LINE);

        return builder.toString();
    }


    private String extractDatesData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();
        System.out.println("Extracting date data...");
        List<WebElement> fieldElements = driver.findElements(By.xpath(".//td[@class='project-section-field-label'] | .//td[@class='project-section-field-value']"));

        for (int i = 0; i < fieldElements.size(); i += 2) {
            // Перевіряємо, чи є відповідний елемент для значення
            if (i + 1 < fieldElements.size()) {
                String labelText = fieldElements.get(i).getText().trim();
                String valueText = fieldElements.get(i + 1).getText().trim();
                builder.append(labelText).append(": ").append(valueText).append(NEW_LINE);
            }
        }

        return builder.toString();
    }
}
