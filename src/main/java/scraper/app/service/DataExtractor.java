package scraper.app.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@RequiredArgsConstructor
public class DataExtractor {
    private static final String NEW_LINE = "\n";
    private static final String PARAGRAPH = ".//p";
    private static final String RAW = "tr";
    private static final String COLUMN = "td";
    private static final String HEADER = "th";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String PERMIT_NUMBER = ".//div[@name='label-CaseNumber']//span";
    private static final String PERMIT_TYPE = ".//div[@name='label-CaseType']//span";
    private static final String PROJECT_NAME = ".//div[@name='label-Project']//span";
    private static final String MAIN_PARCEL = ".//div[@name='label-MainParcel']//span";
    private static final String STATUS = ".//div[@name='label-Status']//span";
    private static final String ADDRESS = ".//div[@name='label-Address']//span";
    private static final String DESCRIPTION = ".//div[@name='label-Description']//span";
    private static final String APPLIED_DATE = ".//div[@name='label-ApplyDate']//span";
    private static final String ISSUED_DATE = ".//div[@name='label-IssuedDate']//span";
    private static final String EXPIRATION_DATE = ".//div[@name='label-ExpiredDate']//span";
    private static final String FINALIZED_DATE = ".//div[@name='label-FinalizedDate']//span";
    private static final String DISTRICT = "label-PermitDetail-District";
    private static final String CONTACTS_TABLE = "selfServiceTable-Contacts";
    private final RecordNavigator recordNavigator;

    public String extractRecordData(WebElement record, WebDriver driver, WebElement link) {
        StringBuilder result = new StringBuilder();
        try {
            result.append("Permit number: ").append(record.findElement(By.xpath(PERMIT_NUMBER))
                    .getText()).append(NEW_LINE);
            result.append("Type: ").append(record.findElement(By.xpath(PERMIT_TYPE))
                    .getText()).append(NEW_LINE);
            result.append("Project name: ").append(record.findElement(By.xpath(PROJECT_NAME))
                    .getText()).append(NEW_LINE);
            result.append("Status: ").append(record.findElement(By.xpath(STATUS))
                    .getText()).append(NEW_LINE);
            result.append("Main parcel: ").append(record.findElement(By.xpath(MAIN_PARCEL))
                    .getText()).append(NEW_LINE);
            result.append("Address: ").append(record.findElement(By.xpath(ADDRESS))
                    .getText()).append(NEW_LINE);
            result.append("Description: ").append(record.findElement(By.xpath(DESCRIPTION))
                    .getText()).append(NEW_LINE);
            result.append("Applied Date: ").append(record.findElement(By.xpath(APPLIED_DATE))
                    .getText()).append(NEW_LINE);
            result.append("Issued Date: ").append(record.findElement(By.xpath(ISSUED_DATE))
                    .getText()).append(NEW_LINE);
            result.append("Expiration Date: ").append(record.findElement(By.xpath(EXPIRATION_DATE))
                    .getText()).append(NEW_LINE);
            result.append("Finalized Date: ").append(record.findElement(By.xpath(FINALIZED_DATE))
                    .getText()).append(NEW_LINE);

            // Переходимо за посиланням і обробляємо вкладку
            recordNavigator.findAndOpenLinkFromRecord(driver, link);

            WebElement district = new WebDriverWait(driver, TIMEOUT)
                    .until(ExpectedConditions.presenceOfElementLocated(By.id(DISTRICT)))
                    .findElement(By.xpath(PARAGRAPH));
            result.append("District: ").append(district.getText()).append(NEW_LINE);

            recordNavigator.clickContactsButton(driver);

            List<String> additionalData = extractContactData(driver);
            for (String element : additionalData) {
                result.append(element);
            }
        } catch (Exception e) {
            System.out.println("Error extracting record data: " + e.getMessage());
        }
        return result.toString();
    }


    private List<String> extractContactData(WebDriver driver) {
        List<String> result = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id(CONTACTS_TABLE)));

            WebElement table = driver.findElement(By.id(CONTACTS_TABLE));

            List<WebElement> rows = table.findElements(By.tagName(RAW));

            List<String> columnHeaders = new ArrayList<>();

            List<WebElement> headers = rows.get(0).findElements(By.tagName(HEADER));
            for (WebElement header : headers) {
                columnHeaders.add(header.getText());
            }

            for (int i = 1; i < rows.size(); i++) {
                List<WebElement> columns = rows.get(i).findElements(By.tagName(COLUMN));

                if (!columns.isEmpty()) {
                    for (int j = 0; j < columns.size(); j++) {
                        String columnText = columns.get(j).getText();
                        String header = columnHeaders.size() > j ? columnHeaders.get(j)
                                : "Column " + (j + 1);
                        result.add(header + ": " + columnText + NEW_LINE);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Contact data cannot be extracted. "
                    + "Exception: " + e.getMessage());
        }
        return result;
    }
}
