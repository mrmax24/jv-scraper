package scraper.app.service.henderson;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.app.service.DataExtractor;
import scraper.app.service.calabasas.CalabasasScraperService;
import scraper.app.util.WebDriverUtils;

@RequiredArgsConstructor
public class HendersonDataExtractor implements DataExtractor {
    private static final Logger log =
            LoggerFactory.getLogger(CalabasasScraperService.class);
    private static final String NEW_LINE = System.lineSeparator();
    private static final String RAW = "tr";
    private static final String COLUMN = "td";
    private static final String HEADER = "th";
    private static final String PARAGRAPH = ".//p";
    private static final String EMPTY_MESSAGE = "Empty field";
    private static final String NO_DATA_MESSAGE = "No data";
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
    private final HendersonPageNavigator hendersonPageNavigator;

    @Override
    public String extractRecords(WebElement record, WebDriver driver, WebElement link) {
        StringBuilder result = new StringBuilder();
        try {
            appendRecordData(result, "Permit number", record, PERMIT_NUMBER, driver);
            appendRecordData(result, "Type", record, PERMIT_TYPE, driver);
            appendRecordData(result, "Project name", record, PROJECT_NAME, driver);
            appendRecordData(result, "Status", record, STATUS, driver);
            appendRecordData(result, "Main parcel", record, MAIN_PARCEL, driver);
            appendRecordData(result, "Address", record, ADDRESS, driver);
            appendRecordData(result, "Description", record, DESCRIPTION, driver);
            appendRecordData(result, "Applied Date", record, APPLIED_DATE, driver);
            appendRecordData(result, "Issued Date", record, ISSUED_DATE, driver);
            appendRecordData(result, "Expiration Date", record, EXPIRATION_DATE, driver);
            appendRecordData(result, "Finalized Date", record, FINALIZED_DATE, driver);

            hendersonPageNavigator.findAndOpenLinkFromRecord(driver, link);

            WebElement district = extractDistrict(driver);
            result.append("District: ").append(district.getText()).append(NEW_LINE);

            hendersonPageNavigator.clickContactsButton((driver));

            List<String> additionalData = extractContactData(driver);
            additionalData.forEach(result::append);
        } catch (Exception e) {
            log.error("Error extracting record data: {}", e.getMessage());
        }
        return result.toString();
    }

    private void appendRecordData(
            StringBuilder result, String label, WebElement record, String xpath, WebDriver driver) {
        try {
            WebDriverUtils.waitForPageToLoad(driver, WebDriverUtils.TIMEOUT);
            String value = record.findElement(By.xpath(xpath)).getText();
            result.append(label).append(": ").append(value.isEmpty()
                    ? EMPTY_MESSAGE : value).append(NEW_LINE);
        } catch (NoSuchElementException e) {
            result.append(label).append(": " + NO_DATA_MESSAGE).append(NEW_LINE);
        }
    }

    private WebElement extractDistrict(WebDriver driver) {
        return new WebDriverWait(driver, WebDriverUtils.TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(By.id(DISTRICT)))
                .findElement(By.xpath(PARAGRAPH));
    }

    private List<String> extractContactData(WebDriver driver) {
        List<String> result = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, WebDriverUtils.TIMEOUT);

        WebElement table = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.id(CONTACTS_TABLE)));

        List<WebElement> rows = table.findElements(By.tagName(RAW));
        List<String> columnHeaders = extractColumnHeaders(rows.get(0));

        for (int i = 1; i < rows.size(); i++) {
            List<WebElement> columns = rows.get(i).findElements(By.tagName(COLUMN));
            if (!columns.isEmpty()) {
                result.addAll(extractRowData(columns, columnHeaders));
            }
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
