package scraper.app.service.calabasas;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.app.service.DataExtractor;

@RequiredArgsConstructor
public class CalabasasDataExtractor implements DataExtractor {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final String RECORD_TITLE_TAG
            = ".//td[contains(@aria-label, 'Record number')]"
            + "//span[contains(@class, 'm-r-sm')]";
    private static final String DESCRIPTION_XPATH
            = "//td[@class='project-header-title p-b']";
    private static final String RECORD_NUMBER_STATUS_XPATH = "//td[@class"
            + "='project-header-field-label' and text()='Record Number']"
            + "/following-sibling::td//div[contains(@class, 'project-header-status-badge')]";
    private static final String FEES_LABEL_XPATH
            = ".//span[normalize-space(text())='Current Fees']";
    private static final String DOLLARS_XPATH = ".//span[@class='dollars']";
    private static final String CENTS_XPATH = ".//span[@class='cents']";
    private static final String UNIT_XPATH = ".//span[@class='unit']";
    private static final String LOCATION_LABEL_XPATH
            = ".//td[@class='project-section-field-label']//span";
    private static final String FIRST_LOCATION_XPATH
            = ".//td[@class='project-section-field-value']//span";
    private static final String SECOND_LOCATION_XPATH
            = ".//td[@class='project-section-field-value']"
            + "/span[contains(translate(text(), 'abcdefghijklmnopqrstuvwxyz', "
            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'CALABASAS')]";
    private static final String PARCEL_LABEL_XPATH
            = ".//td[@class='project-section-field-value']"
            + "/span[contains(text(), 'Parcel')]";
    private static final String PARCEL_XPATH
            = ".//td[@class='project-section-field-value']/span//a";
    private static final String DATES_XPATH
            = "//table[@class='project-section-table-right']//tr";
    private static final String NEW_LINE = System.lineSeparator();

    @Override
    public String extractRecords(WebElement record, WebDriver driver, WebElement link) {
        waitForPageToLoad(driver);
        return extractMainData(driver)
                + extractLocationData(driver)
                + extractDatesData(driver);
    }

    private String extractMainData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();
        String title = getElementText(driver, By.xpath(RECORD_TITLE_TAG));
        String description = getElementText(driver, By.xpath(DESCRIPTION_XPATH));
        String status = getElementText(driver, By.xpath(RECORD_NUMBER_STATUS_XPATH));
        String feesLabel = getElementText(driver, By.xpath(FEES_LABEL_XPATH));
        String currencyUnit = getElementText(driver, By.xpath(UNIT_XPATH));
        String dollars = getElementText(driver, By.xpath(DOLLARS_XPATH));
        String cents = getElementText(driver, By.xpath(CENTS_XPATH));

        builder.append(title).append(NEW_LINE)
                .append(description).append(NEW_LINE)
                .append(status).append(NEW_LINE)
                .append(feesLabel).append(": ")
                .append(currencyUnit)
                .append(dollars).append(".").append(cents).append(NEW_LINE);
        return builder.toString();
    }

    private String extractLocationData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();
        String locationLabel = getElementText(driver, By.xpath(LOCATION_LABEL_XPATH));
        String firstLocation = getElementText(driver, By.xpath(FIRST_LOCATION_XPATH));
        String secondLocation = getElementText(driver, By.xpath(SECOND_LOCATION_XPATH));
        String parcelLabel = getElementText(driver, By.xpath(PARCEL_LABEL_XPATH));
        String parcelValue = getElementText(driver, By.xpath(PARCEL_XPATH));

        builder.append(locationLabel).append(": ")
                .append(firstLocation).append(", ")
                .append(secondLocation).append(NEW_LINE)
                .append(parcelLabel).append(": ")
                .append(parcelValue).append(NEW_LINE);
        return builder.toString();
    }

    private String extractDatesData(WebDriver driver) {
        StringBuilder builder = new StringBuilder();

        List<WebElement> rows = driver.findElements(By.xpath(DATES_XPATH));

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));

            if (cells.size() == 2) {
                String label = cells.get(0).getText().trim();
                String value = cells.get(1).getText().trim();

                if (!label.isEmpty()) {
                    builder.append(label).append(": ")
                            .append(value).append(NEW_LINE);
                }
            }
        }
        return builder.toString();
    }

    private String getElementText(WebDriver driver, By locator) {
        WebElement element = driver.findElement(locator);
        return element != null ? element.getText() : "N/A";
    }

    private void waitForPageToLoad(WebDriver driver) {
        new WebDriverWait(driver, TIMEOUT).until(
                webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete")
        );
    }

}
