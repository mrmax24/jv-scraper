package scraper.app.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ScraperService {
    private static final String NEW_LINE = "\n";
    private static final String SEARCH_BUTTON = "button-Search";
    private static final String RECORD_PATH = "//div[contains(@id, 'entityRecordDiv')]";
    private static final String PERMIT_NUMBER = ".//div[@name='label-CaseNumber']";
    private static final String PERMIT_TYPE = ".//div[@name='label-CaseType']";
    private static final String PROJECT_NAME = ".//div[@name='label-Project']";
    private static final String MAIN_PARCEL = ".//div[@name='label-MainParcel']";
    private static final String STATUS = ".//div[@name='label-Status']";
    private static final String ADDRESS = ".//div[@name='label-Address']";
    private static final String DESCRIPTION = ".//div[@name='label-Description']";
    private static final String APPLIED_DATE = ".//div[@name='label-ApplyDate']";
    private static final String ISSUED_DATE = ".//div[@name='label-IssuedDate']";
    private static final String EXPIRATION_DATE = ".//div[@name='label-ExpiredDate']";
    private static final String FINALIZED_DATE = ".//div[@name='label-FinalizedDate']";
    private static final String DISTRICT = "label-PermitDetail-District";
    private static final String PERMIT_DETAILS_LINK = ".//a[contains(@href, '#/permit/')]";

    public List<String> scrape(String url, int pages) {
        List<String> extractedData = new ArrayList<>();
        WebDriver driver = setupWebDriver();

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
            clickSearchButton(wait);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(RECORD_PATH)));

            int pageCount = 0;
            boolean hasNextPage = true;

            while (hasNextPage && pageCount < pages) {
                List<WebElement> records = driver.findElements(By.xpath(RECORD_PATH));
                System.out.println("Found records: " + records.size());

                for (WebElement record : records) {

                    try {
                        WebElement link = record.findElement(By.xpath(PERMIT_DETAILS_LINK));
                        String result = extractRecordData(record, driver, link);
                        extractedData.add(result);
                    } catch (Exception e) {
                        System.out.println("Error extracting record details: " + e.getMessage());
                    }
                }

                hasNextPage = navigateToNextPage(wait, pageCount);
                pageCount++;
            }
        } finally {
            driver.quit();
        }

        System.out.println("Extracted data: " + extractedData);
        return extractedData;
    }

    private WebDriver setupWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        return new ChromeDriver(options);
    }

    private void clickSearchButton(WebDriverWait wait) {
        try {
            WebElement searchButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(SEARCH_BUTTON)));
            if (searchButton.isDisplayed() && searchButton.isEnabled()) {
                searchButton.click();
                System.out.println("Search button clicked successfully.");
            } else {
                System.out.println("Search button is not clickable or visible.");
            }
        } catch (Exception e) {
            System.out.println("Error finding or clicking search button: " + e.getMessage());
        }
    }

    private String extractRecordData(WebElement record, WebDriver driver, WebElement link) {
        String permitNumber = record.findElement(By.xpath(PERMIT_NUMBER)).getText();
        String permitType = record.findElement(By.xpath(PERMIT_TYPE)).getText();
        String projectName = record.findElement(By.xpath(PROJECT_NAME)).getText();
        String status = record.findElement(By.xpath(STATUS)).getText();
        String parcel = record.findElement(By.xpath(MAIN_PARCEL)).getText();
        String address = record.findElement(By.xpath(ADDRESS)).getText();
        String description = record.findElement(By.xpath(DESCRIPTION)).getText();
        String appliedDate = record.findElement(By.xpath(APPLIED_DATE)).getText();
        String issuedDate = record.findElement(By.xpath(ISSUED_DATE)).getText();
        String expirationDate = record.findElement(By.xpath(EXPIRATION_DATE)).getText();
        String finalizedDate = record.findElement(By.xpath(FINALIZED_DATE)).getText();

        findAndOpenLinkFromRecord(record, driver, link);

        WebElement district = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id(DISTRICT)))
                .findElement(By.xpath(".//p"));
        String districtText = district.getText();

        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());

        return buildResultString(permitNumber, permitType, projectName,
                status, parcel, address, description,
                appliedDate, issuedDate, expirationDate,
                finalizedDate, districtText);
    }

    private String buildResultString(String permitNumber, String permitType,
                                     String projectName, String status, String parcel,
                                     String address, String description, String appliedDate,
                                     String issuedDate, String expirationDate,
                                     String finalizedDate, String districtText) {
        return "Permit number: " + permitNumber + NEW_LINE
                + "Type: " + permitType + NEW_LINE
                + "Project name: " + projectName + NEW_LINE
                + "Status: " + status + NEW_LINE
                + "Main parcel: " + parcel + NEW_LINE
                + "Address: " + address + NEW_LINE
                + "Description: " + description + NEW_LINE
                + "Applied Date: " + appliedDate + NEW_LINE
                + "Issued Date: " + issuedDate + NEW_LINE
                + "Expiration Date: " + expirationDate + NEW_LINE
                + "Finalized Date: " + finalizedDate + NEW_LINE
                + "District: " + districtText + NEW_LINE;
    }

    private void findAndOpenLinkFromRecord(WebElement record, WebDriver driver, WebElement link) {
        link.getAttribute("href");
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", link);
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
        }
    }

    private boolean navigateToNextPage(WebDriverWait wait, int pageCount) {
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
}
