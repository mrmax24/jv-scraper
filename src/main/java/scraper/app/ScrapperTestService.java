package scraper.app;

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

public class ScrapperTestService {
    private static final String NEW_LINE = "\n";
    private static final String SEARCH_BUTTON = "button-Search";
    private static final String CONTACTS_BUTTON = "button-TabButton-Contacts";
    private static final String RECORD_PATH = "//div[contains(@id, 'entityRecordDiv')]";
    private static final String PERMIT_DETAILS_LINK = ".//a[contains(@href, '#/permit/')]";
    private static final String DISTRICT = "label-PermitDetail-District";
    private static final String CONTACTS_TABLE = "selfServiceTable-Contacts";

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
                        extractedData.addAll(scrapeMainData(record, driver, link));
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

    private void clickContactsButton(WebDriverWait wait) {
        try {
            WebElement contactsButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(CONTACTS_BUTTON)));
            if (contactsButton.isDisplayed() && contactsButton.isEnabled()) {
                contactsButton.click();
                System.out.println("Contacts button clicked successfully.");
            } else {
                System.out.println("Contacts button is not clickable or visible.");
            }
        } catch (Exception e) {
            System.out.println("Error finding or clicking contacts button: " + e.getMessage());
        }
    }

    private List<String> scrapeMainData(WebElement record, WebDriver driver, WebElement link) {
        List<String> result = new ArrayList<>();
        try {
            List<WebElement> labels = record.findElements(By.tagName("label"));
            List<WebElement> values = record.findElements(By.xpath(".//span | .//a[@href]"));

            for (int i = 0; i < labels.size(); i++) {
                WebElement label = labels.get(i);
                WebElement value = (i < values.size()) ? values.get(i) : null;

                String labelText = label.getText().trim();
                if (labelText.isEmpty()) {
                    continue;
                }

                String valueText = (value != null) ? value.getText().trim() : "";

                if (valueText.equals("Next")
                        || valueText.equals("Top")
                        || valueText.equals("Paging Options")
                        || valueText.equals("Main Menu")
                        || valueText.isEmpty()) {
                    continue;
                }

                String output = labelText + ": " + valueText;
                result.add(output);
                System.out.println(output);
            }

            findAndOpenLinkFromRecord(driver, link);

            WebElement district = new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.id(DISTRICT)))
                    .findElement(By.xpath(".//p"));
            String districtText = district.getText();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
            clickContactsButton(wait);
            List<String> additionalData = scrapeAdditionalData(driver);
            driver.close();
            driver.switchTo().window(driver.getWindowHandles().iterator().next());

            // Print district info
            String districtOutput = "District: " + districtText;
            result.add(districtOutput);
            System.out.println(districtOutput);

            // Print additional data sequentially for this record
            for (String data : additionalData) {
                result.add(data);
                System.out.println(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<String> scrapeAdditionalData(WebDriver driver) {
        List<String> result = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id(CONTACTS_TABLE)));

            WebElement table = driver.findElement(By.id(CONTACTS_TABLE));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            List<String> columnHeaders = new ArrayList<>();
            List<WebElement> headers = rows.get(0).findElements(By.tagName("th"));
            for (WebElement header : headers) {
                columnHeaders.add(header.getText());
            }

            for (int i = 1; i < rows.size(); i++) {
                List<WebElement> columns = rows.get(i).findElements(By.tagName("td"));
                if (!columns.isEmpty()) {
                    for (int j = 0; j < columns.size(); j++) {
                        String columnText = columns.get(j).getText();
                        String header = columnHeaders.size() > j ? columnHeaders.get(j)
                                : "Column " + (j + 1);
                        result.add(header + ": " + columnText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.add(NEW_LINE);
        return result;
    }

    private void findAndOpenLinkFromRecord(WebDriver driver, WebElement link) {
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
