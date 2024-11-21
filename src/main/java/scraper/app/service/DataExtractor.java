package scraper.app.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final RecordNavigator recordNavigator;

    public String extractRecordData(WebElement record, WebDriver driver, WebElement link) {
        StringBuilder result = new StringBuilder();
        try {
            appendRecordData(result, "Permit number", record, PERMIT_NUMBER);
            appendRecordData(result, "Type", record, PERMIT_TYPE);
            appendRecordData(result, "Project name", record, PROJECT_NAME);
            appendRecordData(result, "Status", record, STATUS);
            appendRecordData(result, "Main parcel", record, MAIN_PARCEL);
            appendRecordData(result, "Address", record, ADDRESS);
            appendRecordData(result, "Description", record, DESCRIPTION);
            appendRecordData(result, "Applied Date", record, APPLIED_DATE);
            appendRecordData(result, "Issued Date", record, ISSUED_DATE);
            appendRecordData(result, "Expiration Date", record, EXPIRATION_DATE);
            appendRecordData(result, "Finalized Date", record, FINALIZED_DATE);

            recordNavigator.findAndOpenLinkFromRecord(driver, link);

            WebElement district = extractDistrict(driver);
            result.append("District: ").append(district.getText()).append(NEW_LINE);

            recordNavigator.clickContactsButton((driver));

            List<String> additionalData = extractContactData(driver);
            additionalData.forEach(result::append);
        } catch (Exception e) {
            System.out.println("Error extracting record data: " + e.getMessage());
        }
        return result.toString();
    }

    private void appendRecordData(
            StringBuilder result, String label, WebElement record, String xpath) {
        try {
            String value = record.findElement(By.xpath(xpath)).getText();
            result.append(label).append(": ").append(value).append(NEW_LINE);
            if (label.isEmpty()) {
                result.append(label).append(": empty field").append(NEW_LINE);
            }
        } catch (NoSuchElementException e) {
            result.append(label).append(": N/A").append(NEW_LINE);
        }
    }

    private WebElement extractDistrict(WebDriver driver) {
        return new WebDriverWait(driver, TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(By.id(DISTRICT)))
                .findElement(By.xpath(PARAGRAPH));
    }

    private List<String> extractContactData(WebDriver driver) {
        List<String> result = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        try {
            WebElement table = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.id(CONTACTS_TABLE)));

            List<WebElement> rows = table.findElements(By.tagName(RAW));
            if (rows.isEmpty()) {
                throw new RuntimeException("No rows found in the contacts table.");
            }

            List<String> columnHeaders = extractColumnHeaders(rows.get(0));

            for (int i = 1; i < rows.size(); i++) {
                List<WebElement> columns = rows.get(i).findElements(By.tagName(COLUMN));
                if (!columns.isEmpty()) {
                    result.addAll(extractRowData(columns, columnHeaders));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Contact data cannot be extracted. "
                    + "Exception: " + e.getMessage());
        }
        return result;
    }

    private List<String> extractColumnHeaders(WebElement headerRow) {
        List<String> columnHeaders = new ArrayList<>();
        List<WebElement> headers = headerRow.findElements(By.tagName(HEADER));

        for (WebElement header : headers) {
            columnHeaders.add(header.getText());
        }
        return columnHeaders;
    }

    private List<String> extractRowData(List<WebElement> columns, List<String> columnHeaders) {
        List<String> rowData = new ArrayList<>();

        for (int j = 0; j < columns.size(); j++) {
            String columnText = columns.get(j).getText();
            String header = columnHeaders.size() > j ? columnHeaders.get(j) : "Column " + (j + 1);
            rowData.add(header + ": " + columnText + NEW_LINE);
        }
        return rowData;
    }
}
