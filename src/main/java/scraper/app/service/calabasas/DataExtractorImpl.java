package scraper.app.service.calabasas;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import scraper.app.service.DataExtractor;
import java.util.List;

@RequiredArgsConstructor
public class DataExtractorImpl implements DataExtractor {
    private static final String RECORD_TITLE_TAG = "search-result-title";
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
        try {
            WebElement titleElement = record.findElement(By.xpath(RECORD_TITLE_TAG));
            String title = titleElement.getText().trim() + NEW_LINE;
            result.append(title);

            recordNavigator.findAndOpenLinkFromRecord(driver, link);
            String detailedData = extractDetailedData(driver);
            String locationData = extractLocationData(driver);
            String datesData = extractDatesData(driver);
            result.append(detailedData).append(locationData).append(datesData);
        } catch (Exception e) {
            System.err.println("Error processing item: " + e.getMessage());
        }
        return result.toString();
    }

    private String extractDetailedData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();
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
        WebElement locationLabel = driver.findElement(By.xpath(LOCATION_LABEL_XPATH));
        WebElement firstLocation = driver.findElement(By.xpath(FIRST_LOCATION_XPATH));
        WebElement secondLocation = driver.findElement(By.xpath(SECOND_LOCATION_XPATH));
        WebElement parcelLabel = driver.findElement(By.xpath(PARCEL_LABEL_XPATH));
        WebElement parcelValue = driver.findElement(By.xpath(PARCEL_XPATH));

        return builder.append(locationLabel).append(": ")
                .append(firstLocation).append(", ")
                .append(secondLocation).append(NEW_LINE)
                .append(parcelLabel).append(": ")
                .append(parcelValue).append(NEW_LINE).toString();
    }

    private String extractDatesData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();

        List<WebElement> labels = driver.findElements(By.xpath(FIELD_LABEL_XPATH));
        List<WebElement> values = driver.findElements(By.xpath(FIELD_VALUE_XPATH));

        if (labels.size() != values.size()) {
            throw new IllegalStateException("Mismatch between labels and values!");
        }

        for (int i = 0; i < labels.size(); i++) {
            String labelText = labels.get(i).getText().trim();
            String valueText = values.get(i).getText().trim();

            builder.append(labelText).append(": ").append(valueText).append(NEW_LINE);
        }

        return builder.toString();
    }

}
