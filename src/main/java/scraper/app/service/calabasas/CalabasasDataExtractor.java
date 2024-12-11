package scraper.app.service.calabasas;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.service.DataExtractor;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
public class CalabasasDataExtractor implements DataExtractor {
    private static final String RECORD_TITLE_TAG = ".//td[contains(@aria-label, 'Record number')]"
            + "//span[contains(@class, 'm-r-sm')]";
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

    @Override
    public String extractRecords(WebElement record, WebDriver driver, WebElement link) {
        StringBuilder result = new StringBuilder();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        result.append(extractMainData(wait))
                .append(extractLocationData(wait))
                .append(extractDatesData(wait));
        return result.toString();
    }

    private String extractMainData(WebDriverWait wait) {
        StringBuilder builder = new StringBuilder();
        String title = getElementText(wait, By.xpath(RECORD_TITLE_TAG));
        String description = getElementText(wait, By.xpath(DESCRIPTION_XPATH));
        String status = getElementText(wait, By.xpath(RECORD_NUMBER_STATUS_XPATH));
        String feesLabel = getElementText(wait, By.xpath(FEES_LABEL_XPATH));
        String currencyUnit = getElementText(wait, By.xpath(UNIT_XPATH));
        String dollars = getElementText(wait, By.xpath(DOLLARS_XPATH));
        String cents = getElementText(wait, By.xpath(CENTS_XPATH));

        builder.append(title).append(NEW_LINE)
                .append(description).append(NEW_LINE)
                .append(status).append(NEW_LINE)
                .append(feesLabel).append(": ")
                .append(currencyUnit)
                .append(dollars).append(".").append(cents).append(NEW_LINE);
        return builder.toString();
    }

    private String extractLocationData(WebDriverWait wait) {
        StringBuilder builder = new StringBuilder();
        String locationLabel = getElementText(wait, By.xpath(LOCATION_LABEL_XPATH));
        String firstLocation = getElementText(wait, By.xpath(FIRST_LOCATION_XPATH));
        String secondLocation = getElementText(wait, By.xpath(SECOND_LOCATION_XPATH));
        String parcelLabel = getElementText(wait, By.xpath(PARCEL_LABEL_XPATH));
        String parcelValue = getElementText(wait, By.xpath(PARCEL_XPATH));

        builder.append(locationLabel).append(": ")
                .append(firstLocation).append(", ")
                .append(secondLocation).append(NEW_LINE)
                .append(parcelLabel).append(": ")
                .append(parcelValue).append(NEW_LINE);
        return builder.toString();
    }

    private String extractDatesData(WebDriverWait wait) {
        StringBuilder builder = new StringBuilder();
        List<WebElement> fieldLabels = waitForElements(wait, By.xpath(FIELD_LABEL_XPATH));
        List<WebElement> fieldValues = waitForElements(wait, By.xpath(FIELD_VALUE_XPATH));

        int size = Math.min(fieldLabels.size(), fieldValues.size());

        for (int i = 0; i < size; i++) {
            String labelText = fieldLabels.get(i).getText().trim();
            String valueText = fieldValues.get(i).getText().trim();
            builder.append(labelText).append(": ").append(valueText).append(NEW_LINE);
        }
        return builder.toString();
    }

    private String getElementText(WebDriverWait wait, By locator) {
        WebElement element = waitForElement(wait, locator);
        return element != null ? element.getText() : "N/A";
    }

    private WebElement waitForElement(WebDriverWait wait, By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            return null;
        }
    }

    private List<WebElement> waitForElements(WebDriverWait wait, By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (Exception e) {
            throw new RuntimeException("Could not find elements for locator: " + locator);
        }
    }
}
